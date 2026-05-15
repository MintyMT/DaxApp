package com.dax.app.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "categorias")
class Categoria(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(nullable = false)
    val nombre: String,

    @Column(nullable = false)
    val tipo: String, // "GASTO" o "INGRESO"

    @Column(name = "usuario_id", nullable = true)
    val usuarioId: UUID? = null
){

    fun copiarConNuevoNombre(nuevoNombre: String): Categoria {
        return Categoria(
            id = this.id,
            nombre = nuevoNombre,
            tipo = this.tipo,
            usuarioId = this.usuarioId
        )
    }
}