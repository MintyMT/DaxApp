package com.dax.app.dto

import java.math.BigDecimal
import java.util.UUID

data class PresupuestoRequest(
    val usuarioId: UUID,
    val montoTotal: BigDecimal
)

data class ResumenPresupuestoDTO(
    val presupuestoRestante: BigDecimal,
    val categorias: List<CategoriaResumenDTO>
)

data class CategoriaResumenDTO(
    val categoriaId: UUID?,
    val nombreCategoria: String,
    val totalGastado: BigDecimal,
    val ultimasTres: List<TransaccionResponse>
)