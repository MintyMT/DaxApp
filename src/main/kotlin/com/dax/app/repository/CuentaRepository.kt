package com.dax.app.repository

import com.dax.app.entity.Cuenta
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CuentaRepository : JpaRepository<Cuenta, UUID> {
    fun findByUsuarioId(usuarioId: UUID): List<Cuenta>

    fun deleteByUsuarioId(usuarioId: UUID)
}