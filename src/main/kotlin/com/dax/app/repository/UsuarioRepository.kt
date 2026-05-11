package com.dax.app.repository

import com.dax.app.entity.Usuario
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.util.UUID

@Repository
interface UsuarioRepository : JpaRepository<Usuario, UUID> {
    fun findByEmail(email: String): Usuario?

    fun findByFechaSolicitudBorradoBefore(fecha: OffsetDateTime): List<Usuario>

}