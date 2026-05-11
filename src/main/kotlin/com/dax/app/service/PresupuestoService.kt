package com.dax.app.service

import com.dax.app.dto.CategoriaResumenDTO
import com.dax.app.entity.Presupuesto
import com.dax.app.repository.PresupuestoRepository
import com.dax.app.repository.CategoriaRepository
import com.dax.app.dto.ResumenPresupuestoDTO
import com.dax.app.dto.TransaccionResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.collections.component1
import kotlin.collections.component2

@Service
class PresupuestoService(
    private val presupuestoRepository: PresupuestoRepository, private val categoriaRepository: CategoriaRepository, private val transaccionService: TransaccionService
) {

    /**
     * Define el presupuesto global del usuario.
     * Si ya existe uno, lo actualiza. Si no, lo crea.
     */
    @Transactional
    fun definirPresupuesto(usuarioId: UUID, monto: BigDecimal): Presupuesto {
        // 1. Buscamos si el usuario ya tiene un presupuesto guardado
        val existente = presupuestoRepository.findByUsuarioId(usuarioId)

        return if (existente != null) {
            // 2. Si existe, actualizamos el monto total
            existente.montoTotal = monto
            presupuestoRepository.save(existente)
        } else {
            // 3. Si no existe, creamos el registro inicial
            val nuevo = Presupuesto(
                usuarioId = usuarioId,
                montoTotal = monto
            )
            presupuestoRepository.save(nuevo)
        }
    }

    /**
     * Obtiene el presupuesto actual del usuario.
     * Útil para mostrar el "Monto Objetivo" en la parte superior de la pantalla.
     */
    fun obtenerPresupuestoPorUsuario(usuarioId: UUID): Presupuesto? {
        return presupuestoRepository.findByUsuarioId(usuarioId)
    }

    fun obtenerResumenGastosPorCategoria(
        usuarioId: UUID,
        fechaInicio: OffsetDateTime,
        fechaFin: OffsetDateTime
    ): ResumenPresupuestoDTO {

        // 1. Obtenemos TODOS los gastos del mes
        val todosLosGastos = transaccionService.obtenerTransaccionesFiltradas(
            usuarioId = usuarioId,
            tipo = "GASTO",
            fechaInicio = fechaInicio,
            fechaFin = fechaFin
        )

        // --- CAMBIO: Lógica para calcular el presupuesto y restante ---
        val presupuestoGlobal = presupuestoRepository.findByUsuarioId(usuarioId)?.montoTotal ?: BigDecimal.ZERO
        val totalGastadoMes = todosLosGastos.map { it.monto }.fold(BigDecimal.ZERO, BigDecimal::add)
        val restanteGlobal = presupuestoGlobal.subtract(totalGastadoMes)
        // --------------------------------------------------------------

        // 2. Agrupamos por categoriaId
        val gastosAgrupados = todosLosGastos.groupBy { it.categoriaId }

        // 3. Transformamos ese mapa en nuestra lista de DTOs (ahora guardada en una variable)
        val listaCategorias = gastosAgrupados.map { (catId, transacciones) ->

            val total = transacciones
                .map { it.monto }
                .fold(BigDecimal.ZERO, BigDecimal::add)

            val ultimasTres = transacciones.take(3).map { tx ->
                TransaccionResponse(
                    id = tx.id!!,
                    monto = tx.monto,
                    tipo = tx.tipo,
                    descripcion = tx.descripcion,
                    fecha = tx.fecha,
                    categoriaId = tx.categoriaId,
                    nombreCuentaOrigen = tx.cuentaOrigen?.nombre,
                    nombreCuentaDestino = tx.cuentaDestino?.nombre,
                    notas = tx.notas
                )
            }

            val nombreCat = if (catId != null) {
                categoriaRepository.findById(catId).map { it.nombre }.orElse("Sin Categoría")
            } else {
                "Sin Categoría"
            }

            // Seguimos usando el data class CategoriaResumenDTO como molde para cada item
            CategoriaResumenDTO(
                categoriaId = catId,
                nombreCategoria = nombreCat,
                totalGastado = total,
                ultimasTres = ultimasTres
            )
        }

        // --- Retornamos el objeto maestro con el restante y las categorías ---
        return ResumenPresupuestoDTO(
            presupuestoRestante = restanteGlobal,
            categorias = listaCategorias
        )
        // -----------------------------------------------------------------------------
    }
}