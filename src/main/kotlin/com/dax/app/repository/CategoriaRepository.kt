package com.dax.app.repository

import com.dax.app.entity.Categoria
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CategoriaRepository : JpaRepository<Categoria, UUID> {

    // Busca las categorías que no tienen usuario asignado (Globales)
    fun findByUsuarioIdIsNull(): List<Categoria>

    // Busca las categorías creadas por un usuario específico
    fun findByUsuarioId(usuarioId: UUID): List<Categoria>

    fun findByNombreAndUsuarioIdIsNull(nombre: String): Categoria?

    fun deleteByUsuarioId(usuarioId: UUID)
}