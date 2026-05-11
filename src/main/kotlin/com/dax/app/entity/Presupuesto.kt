package com.dax.app.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "presupuestos")
class Presupuesto(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "monto_total", nullable = false, precision = 12, scale = 2)
    var montoTotal: BigDecimal,

    @Column(name = "usuario_id", nullable = false, unique = true) // 'unique' para asegurar uno solo por usuario
    val usuarioId: UUID
)