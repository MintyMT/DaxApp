package com.dax.app.dto

import java.math.BigDecimal
import java.util.UUID

// --- REQUESTS ---
data class NuevaCuentaRequest(
    val usuarioId: UUID,
    val nombre: String,
    val saldoInicial: BigDecimal,
    val tipoId: UUID
)

// --- RESPONSES ---
data class CuentaResponse(
    val id: UUID,
    val nombre: String,
    val saldo: BigDecimal,
    val tipoNombre: String // Solo nombre, ya que no hay icono en la entidad
)

data class BalanceGlobalResponse(
    val balanceTotal: BigDecimal
)