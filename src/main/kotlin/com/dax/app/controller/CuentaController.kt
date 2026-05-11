package com.dax.app.controller

import com.dax.app.dto.*
import com.dax.app.service.CuentaService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/cuentas")
class CuentaController(private val cuentaService: CuentaService) {

    @GetMapping("/usuario/{usuarioId}")
    fun obtenerCuentas(@PathVariable usuarioId: UUID): ResponseEntity<List<CuentaResponse>> {
        val cuentas = cuentaService.obtenerCuentasUsuario(usuarioId)

        val respuesta = cuentas.map { c ->
            CuentaResponse(
                id = c.id!!,
                nombre = c.nombre,
                saldo = c.saldo,
                tipoNombre = c.tipo?.nombre ?: "Sin tipo",
                activa = c.activa
            )
        }
        return ResponseEntity.ok(respuesta)
    }

    @GetMapping("/usuario/{usuarioId}/balance")
    fun obtenerBalance(@PathVariable usuarioId: UUID): ResponseEntity<BalanceGlobalResponse> {
        val balance = cuentaService.calcularBalanceGlobal(usuarioId)
        return ResponseEntity.ok(BalanceGlobalResponse(balance))
    }

    @PostMapping
    fun crearCuenta(@RequestBody request: NuevaCuentaRequest): ResponseEntity<CuentaResponse> {
        val cuenta = cuentaService.agregarCuenta(
            request.usuarioId, request.nombre, request.saldoInicial, request.tipoId
        )

        return ResponseEntity.status(201).body(CuentaResponse(
            id = cuenta.id!!,
            nombre = cuenta.nombre,
            saldo = cuenta.saldo,
            tipoNombre = cuenta.tipo?.nombre ?: "Sin tipo",
            activa = cuenta.activa
        ))
    }

    @DeleteMapping("/{id}")
    fun eliminarCuenta(@PathVariable id: UUID): ResponseEntity<Void> {
        cuentaService.desactivarCuenta(id)
        return ResponseEntity.noContent().build()
    }
}