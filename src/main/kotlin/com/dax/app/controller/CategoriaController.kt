package com.dax.app.controller

import com.dax.app.dto.*
import com.dax.app.service.CategoriaService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/categorias")
class CategoriaController(private val categoriaService: CategoriaService) {

    @GetMapping("/usuario/{usuarioId}")
    fun listarPorUsuario(@PathVariable usuarioId: UUID): ResponseEntity<List<CategoriaResponse>> {
        val categorias = categoriaService.obtenerTodoPorUsuario(usuarioId)

        val respuesta = categorias.map { c ->
            CategoriaResponse(
                id = c.id!!,
                nombre = c.nombre,
                tipo = c.tipo,
                esGlobal = c.usuarioId == null
            )
        }
        return ResponseEntity.ok(respuesta)
    }

    @PostMapping
    fun crear(@RequestBody request: NuevaCategoriaRequest): ResponseEntity<CategoriaResponse> {
        val cat = categoriaService.crearCategoria(
            request.nombre, request.tipo, request.usuarioId
        )
        return ResponseEntity.ok(CategoriaResponse(cat.id!!, cat.nombre, cat.tipo, cat.icono, false))
    }

    @PutMapping("/{id}")
    fun editar(@PathVariable id: UUID, @RequestBody request: EditarCategoriaRequest): ResponseEntity<CategoriaResponse> {
        val cat = categoriaService.editarNombreCategoria(id, request.nuevoNombre, request.usuarioId)
        return ResponseEntity.ok(CategoriaResponse(cat.id!!, cat.nombre, cat.tipo, cat.icono, false))
    }

    @DeleteMapping("/{id}")
    fun eliminar(@PathVariable id: UUID, @RequestParam usuarioId: UUID): ResponseEntity<String> {
        categoriaService.eliminarCategoriaPersonalizada(id, usuarioId)
        return ResponseEntity.ok("Categoría eliminada. Las transacciones se movieron a categorías del sistema.")
    }
}