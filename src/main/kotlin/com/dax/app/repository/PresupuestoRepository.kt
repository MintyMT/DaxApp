package com.dax.app.repository

import com.dax.app.entity.Presupuesto
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
interface PresupuestoRepository : JpaRepository<Presupuesto, UUID> {
    fun findByUsuarioId(usuarioId: UUID): Presupuesto?

    @Modifying
    @Transactional
    fun deleteByUsuarioId(usuarioId: UUID)
}