package com.dax.app.service

import com.dax.app.entity.Cuenta
import com.dax.app.entity.Transaccion
import com.dax.app.repository.CategoriaRepository
import com.dax.app.repository.CuentaRepository
import com.dax.app.repository.TransaccionRepository
import com.dax.app.dto.TransaccionResponse
import com.dax.app.dto.CategoriaResumenDTO
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Service
class TransaccionService(
    private val transaccionRepository: TransaccionRepository,
    private val cuentaRepository: CuentaRepository,
    private val categoriaRepository: CategoriaRepository
) {
    @Transactional
    fun registrarGasto(cuentaId: UUID, monto: BigDecimal, categoriaId: UUID, descripcion: String, fecha: OffsetDateTime, notas: String?) {
        // 1. BUSCAR
        val cuenta = cuentaRepository.findById(cuentaId)
            .orElseThrow { RuntimeException("La cuenta no existe") }

        // 2. VALIDAR
        if (cuenta.saldo < monto) {
            throw RuntimeException("Saldo insuficiente en la cuenta: ${cuenta.nombre}")
        }

        // 3. EJECUTAR
        cuenta.saldo = cuenta.saldo.subtract(monto)
        cuentaRepository.save(cuenta)

        // 4. REGISTRAR: Crear el historial en la tabla transacciones
        val gasto = Transaccion(
            usuarioId = cuenta.usuarioId,
            cuentaOrigen = cuenta,
            cuentaDestino = null,
            categoriaId = categoriaId,
            monto = monto,
            tipo = "GASTO",
            descripcion = descripcion,
            fecha = fecha,
            notas = notas

        )
        transaccionRepository.save(gasto)
    }

    @Transactional
    fun registrarIngreso(cuentaId: UUID, monto: BigDecimal, categoriaId: UUID, descripcion: String, fecha: OffsetDateTime, notas: String?) {
        val cuenta = cuentaRepository.findById(cuentaId)
            .orElseThrow { RuntimeException("La cuenta no existe") }

        // Sumar el dinero
        cuenta.saldo = cuenta.saldo.add(monto)
        cuentaRepository.save(cuenta)

        // Crear el registro (cuentaOrigen es null porque el dinero viene de fuera)
        val ingreso = Transaccion(
            usuarioId = cuenta.usuarioId,
            cuentaOrigen = null,
            cuentaDestino = cuenta,
            categoriaId = categoriaId,
            monto = monto,
            tipo = "INGRESO",
            descripcion = descripcion,
            fecha = fecha,
            notas = notas
        )
        transaccionRepository.save(ingreso)
    }


    @Transactional
    fun registrarTransferencia(cuentaOrigenId: UUID, cuentaDestinoId: UUID, monto: BigDecimal, descripcion: String, fecha: OffsetDateTime, notas: String?) {
        val cuentaOrigen = cuentaRepository.findById(cuentaOrigenId)
            .orElseThrow { RuntimeException("La cuenta no existe") }
        val cuentaDestino = cuentaRepository.findById(cuentaDestinoId)
            .orElseThrow { RuntimeException("La cuenta no existe") }

        if (cuentaOrigen == cuentaDestino) {
            throw RuntimeException("Las cuentas no pueden ser la misma")
        }

        if (cuentaOrigen.saldo < monto) {
            throw RuntimeException("Saldo insuficiente en la cuenta: ${cuentaOrigen.nombre}")
        }

        cuentaOrigen.saldo = cuentaOrigen.saldo.subtract(monto)
        cuentaRepository.save(cuentaOrigen)

        cuentaDestino.saldo = cuentaDestino.saldo.add(monto)
        cuentaRepository.save(cuentaDestino)

        val movimiento = Transaccion(
            usuarioId = cuentaOrigen.usuarioId,
            cuentaOrigen = cuentaOrigen,
            cuentaDestino = cuentaDestino,
            monto = monto,
            tipo = "TRANSFERENCIA",
            descripcion = descripcion,
            fecha = fecha,
            notas = notas
        )
        transaccionRepository.save(movimiento)

    }

    @Transactional
    fun eliminarTransaccion(transaccionId: UUID) {
        // 1. Buscar la transacción
        val tx = transaccionRepository.findById(transaccionId)
            .orElseThrow { RuntimeException("La transacción no existe") }

        // 2. Reajustar saldos según el tipo
        when (tx.tipo) {
            "GASTO" -> {
                // El dinero regresa a la cuenta de origen
                tx.cuentaOrigen?.let { cuenta ->
                    cuenta.saldo = cuenta.saldo.add(tx.monto)
                    cuentaRepository.save(cuenta)
                }
            }
            "INGRESO" -> {
                // El dinero se resta de la cuenta donde ingresó (cuentaDestino)
                tx.cuentaDestino?.let { cuenta ->
                    cuenta.saldo = cuenta.saldo.subtract(tx.monto)
                    cuentaRepository.save(cuenta)
                }
            }
            "TRANSFERENCIA" -> {
                // El dinero sale del destino y regresa al origen
                tx.cuentaDestino?.let { destino ->
                    destino.saldo = destino.saldo.subtract(tx.monto)
                    cuentaRepository.save(destino)
                }
                tx.cuentaOrigen?.let { origen ->
                    origen.saldo = origen.saldo.add(tx.monto)
                    cuentaRepository.save(origen)
                }
            }
        }

        // 3. Borrar la transacción físicamente
        transaccionRepository.delete(tx)
    }

    @Transactional
    fun eliminarCuentaYReajustarSaldos(cuentaId: UUID) {
        val cuentaAEliminar = cuentaRepository.findById(cuentaId)
            .orElseThrow { RuntimeException("La cuenta no existe") }

        // 1. Buscar todas las transferencias donde esta cuenta estuvo involucrada
        // Para reajustar el saldo de la "otra" cuenta que NO se va a borrar.
        val transaccionesRelacionadas = transaccionRepository.findByCuentaOrigenIdOrCuentaDestinoId(cuentaId, cuentaId)

        transaccionesRelacionadas.forEach { tx ->
            if (tx.tipo == "TRANSFERENCIA") {
                if (tx.cuentaOrigen?.id == cuentaId) {
                    // Si la cuenta que borramos fue el ORIGEN, el dinero "vuelve" al destino
                    // Pero como la cuenta destino es la que se queda, debemos restarle lo que recibió
                    tx.cuentaDestino?.let { destino ->
                        destino.saldo = destino.saldo.subtract(tx.monto)
                        cuentaRepository.save(destino)
                    }
                } else if (tx.cuentaDestino?.id == cuentaId) {
                    // Si la cuenta que borramos fue el DESTINO, el dinero "regresa" al origen
                    tx.cuentaOrigen?.let { origen ->
                        origen.saldo = origen.saldo.add(tx.monto)
                        cuentaRepository.save(origen)
                    }
                }
            }
        }

        // 2. Eliminar todas las transacciones relacionadas
        // Esto limpia Gastos, Ingresos y Transferencias de esta cuenta
        transaccionRepository.deleteAll(transaccionesRelacionadas)

        // 3. Finalmente, eliminar la cuenta
        cuentaRepository.delete(cuentaAEliminar)
    }

    fun obtenerTransaccionesFiltradas(
        usuarioId: UUID,
        tipo: String?,
        fechaInicio: OffsetDateTime,
        fechaFin: OffsetDateTime,
        limite: Int? = null,
        cuentaId: UUID? = null
    ): List<Transaccion> {

        val lista = when {
            // CASO: Filtro por Cuenta específica (Detalle de cuenta)
            cuentaId != null -> {
                val comoOrigen = transaccionRepository.findByUsuarioIdAndCuentaOrigenIdAndFechaBetweenOrderByFechaDesc(
                    usuarioId, cuentaId, fechaInicio, fechaFin
                )
                val comoDestino = transaccionRepository.findByUsuarioIdAndCuentaDestinoIdAndFechaBetweenOrderByFechaDesc(
                    usuarioId, cuentaId, fechaInicio, fechaFin
                )
                // Unimos ambas listas y ordenamos por fecha (por si hay transferencias o ingresos)
                (comoOrigen + comoDestino).distinctBy { it.id }.sortedByDescending { it.fecha }
            }

            // CASO: Filtro por Tipo (Gasto/Ingreso)
            tipo != null && tipo != "ALL" -> {
                transaccionRepository.findByUsuarioIdAndTipoAndFechaBetweenOrderByFechaDesc(
                    usuarioId, tipo, fechaInicio, fechaFin
                )
            }

            // CASO: General (Dashboard)
            else -> {
                transaccionRepository.findByUsuarioIdAndFechaBetweenOrderByFechaDesc(
                    usuarioId, fechaInicio, fechaFin
                )
            }
        }

        return if (limite != null) lista.take(limite) else lista
    }

    fun obtenerTotalPorRango(
        usuarioId: UUID,
        tipo: String,
        fechaInicio: OffsetDateTime,
        fechaFin: OffsetDateTime
    ): BigDecimal {
        val transacciones = obtenerTransaccionesFiltradas(usuarioId, tipo, fechaInicio, fechaFin)

        return transacciones
            .map { it.monto }
            .fold(BigDecimal.ZERO, BigDecimal::add)
    }

    fun obtenerFlujoCaja(
        usuarioId: UUID,
        fechaInicio: OffsetDateTime,
        fechaFin: OffsetDateTime
    ): BigDecimal {
        val ingresos = obtenerTotalPorRango(usuarioId, "INGRESO", fechaInicio, fechaFin)
        val gastos = obtenerTotalPorRango(usuarioId, "GASTO", fechaInicio, fechaFin)

        return ingresos.subtract(gastos)
    }

}
