package com.dax.app.controller

import com.dax.app.dto.*
import com.dax.app.service.UsuarioService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/usuarios")
class UsuarioController(private val usuarioService: UsuarioService) {

    @PostMapping("/registro")
    fun registrar(@RequestBody request: RegistroRequest): ResponseEntity<UsuarioResponse> {
        val usuario = usuarioService.registrarUsuario(request.nombre, request.email, request.contra)
        return ResponseEntity.status(201).body(UsuarioResponse(usuario.id!!, usuario.nombre, usuario.email, null))
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<UsuarioResponse> {
        val usuario = usuarioService.iniciarSesion(request.email, request.contra)
        // Si el login canceló un borrado, el service ya puso fechaSolicitudBorrado en null
        return ResponseEntity.ok(UsuarioResponse(usuario.id!!, usuario.nombre, usuario.email, usuario.fechaSolicitudBorrado))
    }

    @PostMapping("/{id}/solicitar-borrado")
    fun solicitarBorrado(@PathVariable id: UUID): ResponseEntity<String> {
        usuarioService.solicitarBorradoDeCuenta(id)
        return ResponseEntity.ok("Proceso de borrado iniciado. Tienes 3 minutos para arrepentirte e iniciar sesión.")
    }

}