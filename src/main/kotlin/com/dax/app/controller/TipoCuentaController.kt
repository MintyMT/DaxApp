package com.dax.app.controller

import com.dax.app.dto.TipoCuentaResponse
import com.dax.app.service.TipoCuentaService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/tipos-cuenta")
class TipoCuentaController(private val tipoCuentaService: TipoCuentaService) {

    @GetMapping
    fun obtenerTodos(): ResponseEntity<List<TipoCuentaResponse>> {
        val tipos = tipoCuentaService.obtenerTodos()

        // Mapeamos a DTO para mantener el estándar de la API
        val respuesta = tipos.map { t ->
            TipoCuentaResponse(
                id = t.id!!,
                nombre = t.nombre
            )
        }
        return ResponseEntity.ok(respuesta)
    }

    @GetMapping("/{id}")
    fun obtenerPorId(@PathVariable id: UUID): ResponseEntity<TipoCuentaResponse> {
        val tipo = tipoCuentaService.obtenerPorId(id)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(TipoCuentaResponse(tipo.id!!, tipo.nombre))
    }
}