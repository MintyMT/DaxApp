package com.dax.app.dto

import java.util.UUID

data class NuevaCategoriaRequest(
    val nombre: String,
    val tipo: String, // "INGRESO" o "GASTO"
    val usuarioId: UUID
)

data class EditarCategoriaRequest(
    val nuevoNombre: String,
    val usuarioId: UUID
)

data class CategoriaResponse(
    val id: UUID,
    val nombre: String,
    val tipo: String,
    val esGlobal: Boolean
)
