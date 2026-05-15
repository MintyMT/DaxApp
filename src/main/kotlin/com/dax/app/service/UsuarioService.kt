package com.dax.app.service

import com.dax.app.entity.Usuario
import com.dax.app.repository.UsuarioRepository
import com.dax.app.repository.TransaccionRepository
import com.dax.app.repository.CuentaRepository
import com.dax.app.repository.CategoriaRepository
import com.dax.app.repository.PresupuestoRepository
import org.hibernate.type.TrueFalseConverter
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import java.time.OffsetDateTime

@Service
class UsuarioService(
    private val usuarioRepository: UsuarioRepository,
    private val transaccionRepository: TransaccionRepository,
    private val cuentaRepository: CuentaRepository,
    private val categoriaRepository: CategoriaRepository,
    private val presupuestoRepository: PresupuestoRepository
    ) {

    @Transactional
    fun registrarUsuario(nombre: String, email: String, contra: String): Usuario {
        if (usuarioRepository.findByEmail(email) != null) {
            throw RuntimeException("El correo ya está registrado")
        }

        val nuevoUsuario = Usuario(
            nombre = nombre,
            email = email,
            password = contra 
        )

        return usuarioRepository.save(nuevoUsuario)
    }

    fun iniciarSesion(email: String, password: String): Usuario {
        val usuario = usuarioRepository.findByEmail(email)
            ?: throw RuntimeException("Correo no registrado")

        if (usuario.password != password) {
            throw RuntimeException("Contraseña incorrecta")
        }

        if (usuario.fechaSolicitudBorrado != null) {
            usuario.fechaSolicitudBorrado = null 
            usuarioRepository.save(usuario)
            println("¡Bienvenido de nuevo! Tu solicitud de borrado ha sido cancelada.")
        }

        return usuario
    }

    @Transactional
    fun solicitarBorradoDeCuenta(usuarioId: UUID) {
        val usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow { RuntimeException("Usuario no encontrado") }

        
        usuario.fechaSolicitudBorrado = OffsetDateTime.now()
        usuarioRepository.save(usuario)
        println("Borrado solicitado para ${usuario.id}. Se eliminará en 3 minutos.")
    }

    
    @Scheduled(fixedDelay = 60000) 
    @Transactional
    fun ejecutarLimpiezaDeCuentas() {
        val limiteTiempo = OffsetDateTime.now().minusMinutes(3)

        
        val usuariosParaEliminar = usuarioRepository.findByFechaSolicitudBorradoBefore(limiteTiempo)

        usuariosParaEliminar.forEach { usuario ->
            eliminarTodoLoRelacionado(usuario.id!!)
        }
    }

    @Transactional
    fun eliminarTodoLoRelacionado(usuarioId: UUID) {
        
        transaccionRepository.deleteByUsuarioId(usuarioId)
        cuentaRepository.deleteByUsuarioId(usuarioId)
        categoriaRepository.deleteByUsuarioId(usuarioId)
        presupuestoRepository.deleteByUsuarioId(usuarioId)

        usuarioRepository.deleteById(usuarioId)
        println("Cuenta $usuarioId eliminada definitivamente de Supabase.")
    }

}