package com.dax.app.dto

import java.util.UUID
import java.time.OffsetDateTime

data class RegistroRequest(
    val nombre: String,
    val email: String,
    val contra: String
)

data class LoginRequest(
    val email: String,
    val contra: String
)

data class UsuarioResponse(
    val id: UUID,
    val nombre: String,
    val email: String,
    val fechaSolicitudBorrado: OffsetDateTime?
)