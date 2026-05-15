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

    
    @Transactional
    fun definirPresupuesto(usuarioId: UUID, monto: BigDecimal): Presupuesto {
        
        val existente = presupuestoRepository.findByUsuarioId(usuarioId)

        return if (existente != null) {
            
            existente.montoTotal = monto
            presupuestoRepository.save(existente)
        } else {
            
            val nuevo = Presupuesto(
                usuarioId = usuarioId,
                montoTotal = monto
            )
            presupuestoRepository.save(nuevo)
        }
    }

    
    fun obtenerPresupuestoPorUsuario(usuarioId: UUID): Presupuesto? {
        return presupuestoRepository.findByUsuarioId(usuarioId)
    }

    fun obtenerResumenGastosPorCategoria(
        usuarioId: UUID,
        fechaInicio: OffsetDateTime,
        fechaFin: OffsetDateTime
    ): ResumenPresupuestoDTO {

        
        val todosLosGastos = transaccionService.obtenerTransaccionesFiltradas(
            usuarioId = usuarioId,
            tipo = "GASTO",
            fechaInicio = fechaInicio,
            fechaFin = fechaFin
        )

        
        val presupuestoGlobal = presupuestoRepository.findByUsuarioId(usuarioId)?.montoTotal ?: BigDecimal.ZERO
        val totalGastadoMes = todosLosGastos.map { it.monto }.fold(BigDecimal.ZERO, BigDecimal::add)
        val restanteGlobal = presupuestoGlobal.subtract(totalGastadoMes)
        

        
        val gastosAgrupados = todosLosGastos.groupBy { it.categoriaId }

        
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

            
            CategoriaResumenDTO(
                categoriaId = catId,
                nombreCategoria = nombreCat,
                totalGastado = total,
                ultimasTres = ultimasTres
            )
        }

        
        return ResumenPresupuestoDTO(
            presupuestoRestante = restanteGlobal,
            categorias = listaCategorias
        )

    }
}