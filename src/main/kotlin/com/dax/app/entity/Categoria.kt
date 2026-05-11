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
    val icono: String, // Aquí guardas el nombre del icono (ej: "ic_food")

    @Column(nullable = false)
    val tipo: String, // "GASTO" o "INGRESO"

    @Column(name = "usuario_id", nullable = true)
    val usuarioId: UUID? = null // Null = Global, Con valor = Privada
){
    /**
     * Crea una copia de la categoría con un nuevo nombre.
     * Mantiene el ID y el usuarioId originales para que JPA realice un UPDATE.
     */
    fun copiarConNuevoNombre(nuevoNombre: String): Categoria {
        return Categoria(
            id = this.id, // Mismo UUID de Supabase
            nombre = nuevoNombre, // Único campo que cambia
            icono = this.icono,
            tipo = this.tipo,
            usuarioId = this.usuarioId
        )
    }
}