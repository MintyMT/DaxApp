package com.dax.app.dto

import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID


data class GastoIngresoRequest(
    val cuentaId: UUID,
    val monto: BigDecimal,
    val categoriaId: UUID,
    val descripcion: String,
    val fecha: OffsetDateTime,
    val notas: String?
)

data class TransferenciaRequest(
    val cuentaOrigenId: UUID,
    val cuentaDestinoId: UUID,
    val monto: BigDecimal,
    val descripcion: String,
    val fecha: OffsetDateTime,
    val notas: String?
)

data class TotalPorRangoResponse (
    val usuarioId: UUID,
    val tipo: String,
    val fechaInicio: OffsetDateTime,
    val fechaFin: OffsetDateTime
)

data class TransaccionResponse(
    val id: UUID,
    val monto: BigDecimal,
    val tipo: String, // "INGRESO", "GASTO", "TRANSFERENCIA"
    val descripcion: String?,
    val fecha: OffsetDateTime,
    val categoriaId: UUID?,
    val nombreCuentaOrigen: String?,
    val nombreCuentaDestino: String?,
    val notas: String?
)