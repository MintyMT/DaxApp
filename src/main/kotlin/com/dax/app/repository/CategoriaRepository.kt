package com.dax.app.repository

import com.dax.app.entity.Categoria
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
interface CategoriaRepository : JpaRepository<Categoria, UUID> {

    fun findByUsuarioIdIsNull(): List<Categoria>

    fun findByUsuarioId(usuarioId: UUID): List<Categoria>

    fun findByNombreAndUsuarioIdIsNull(nombre: String): Categoria?

    @Modifying
    @Transactional
    fun deleteByUsuarioId(usuarioId: UUID)
}