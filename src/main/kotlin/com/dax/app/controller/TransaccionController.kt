package com.dax.app.controller

import com.dax.app.dto.*
import com.dax.app.service.TransaccionService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

@RestController
@RequestMapping("/api/transacciones")
class TransaccionController(private val transaccionService: TransaccionService) {

    @PostMapping("/gasto")
    fun registrarGasto(@RequestBody request: GastoIngresoRequest): ResponseEntity<String> {
        transaccionService.registrarGasto(
            request.cuentaId, request.monto, request.categoriaId,
            request.descripcion, request.fecha, request.notas
        )
        return ResponseEntity.ok("Gasto registrado y saldo actualizado")
    }

    @PostMapping("/ingreso")
    fun registrarIngreso(@RequestBody request: GastoIngresoRequest): ResponseEntity<String> {
        transaccionService.registrarIngreso(
            request.cuentaId, request.monto, request.categoriaId,
            request.descripcion, request.fecha, request.notas
        )
        return ResponseEntity.ok("Ingreso registrado y saldo actualizado")
    }

    @PostMapping("/transferencia")
    fun registrarTransferencia(@RequestBody request: TransferenciaRequest): ResponseEntity<String> {
        transaccionService.registrarTransferencia(
            request.cuentaOrigenId, request.cuentaDestinoId, request.monto,
            request.descripcion, request.fecha, request.notas
        )
        return ResponseEntity.ok("Transferencia realizada con éxito")
    }

    @DeleteMapping("/{id}")
    fun eliminar(@PathVariable id: UUID): ResponseEntity<String> {
        transaccionService.eliminarTransaccion(id)
        return ResponseEntity.ok("Transacción eliminada y saldos revertidos")
    }

    @GetMapping("/filtradas")
    fun obtenerFiltradas(
        @RequestParam usuarioId: UUID,
        @RequestParam(required = false) tipo: String?,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) fechaInicio: OffsetDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) fechaFin: OffsetDateTime,
        @RequestParam(required = false) limite: Int?,
        @RequestParam(required = false) cuentaId: UUID?
    ): ResponseEntity<List<TransaccionResponse>> {

        val lista = transaccionService.obtenerTransaccionesFiltradas(
            usuarioId, tipo, fechaInicio, fechaFin, limite, cuentaId
        )

        // Mapeo de Entidad a DTO para la respuesta
        val respuesta = lista.map { tx ->
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

        return ResponseEntity.ok(respuesta)
    }

    @GetMapping("/flujo-caja")
    fun obtenerFlujoCaja(
        @RequestParam usuarioId: UUID,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) fechaInicio: OffsetDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) fechaFin: OffsetDateTime
    ): ResponseEntity<BigDecimal> {
        val flujo = transaccionService.obtenerFlujoCaja(usuarioId, fechaInicio, fechaFin)
        return ResponseEntity.ok(flujo)
    }
}