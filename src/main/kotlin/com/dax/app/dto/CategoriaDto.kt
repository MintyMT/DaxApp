package com.dax.app.dto

import java.util.UUID

// --- REQUESTS ---

data class NuevaCategoriaRequest(
    val nombre: String,
    val tipo: String, // "INGRESO" o "GASTO"
    val icono: String,
    val usuarioId: UUID
)

data class EditarCategoriaRequest(
    val nuevoNombre: String,
    val usuarioId: UUID
)

// --- RESPONSES ---

data class CategoriaResponse(
    val id: UUID,
    val nombre: String,
    val tipo: String,
    val icono: String,
    val esGlobal: Boolean // Para que el frontend sepa si mostrar el botón de editar/borrar
)
