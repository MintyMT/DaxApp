package com.dax.app.controller

import com.dax.app.dto.CategoriaResumenDTO
import com.dax.app.dto.PresupuestoRequest
import com.dax.app.entity.Presupuesto
import com.dax.app.service.PresupuestoService
import com.dax.app.dto.ResumenPresupuestoDTO
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.OffsetDateTime
import java.util.UUID

@RestController
@RequestMapping("/api/presupuestos")
class PresupuestoController(
    private val presupuestoService: PresupuestoService
) {

    @PostMapping
    fun definirPresupuesto(@RequestBody request: PresupuestoRequest): ResponseEntity<Presupuesto> {
        // Llamamos al service pasando los datos del DTO
        val resultado = presupuestoService.definirPresupuesto(
            request.usuarioId,
            request.montoTotal
        )
        return ResponseEntity.ok(resultado)
    }

    @GetMapping("/{usuarioId}")
    fun obtenerPresupuesto(@PathVariable usuarioId: java.util.UUID): ResponseEntity<Presupuesto> {
        val presupuesto = presupuestoService.obtenerPresupuestoPorUsuario(usuarioId)
        return if (presupuesto != null) {
            ResponseEntity.ok(presupuesto)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/resumen-presupuesto")
    fun obtenerResumenPresupuesto(
        @RequestParam usuarioId: UUID,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) fechaInicio: OffsetDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) fechaFin: OffsetDateTime
    ): ResponseEntity<ResumenPresupuestoDTO> {

        val resumen = presupuestoService.obtenerResumenGastosPorCategoria(
            usuarioId, fechaInicio, fechaFin
        )

        return ResponseEntity.ok(resumen)
    }
}