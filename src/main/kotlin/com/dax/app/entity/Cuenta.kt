package com.dax.app.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.util.UUID


@Entity
@Table(name = "cuentas")
class Cuenta(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "usuario_id")
    val usuarioId: UUID,

    @field:NotBlank(message = "El nombre de la cuenta no puede estar vacío")
    val nombre: String,

    @field:NotNull(message = "El saldo es obligatorio")
    @Column(precision = 19, scale = 2)
    var saldo: BigDecimal = BigDecimal.ZERO,

    @ManyToOne
    @JoinColumn(name = "tipo_id")
    val tipo: TipoCuenta? = null,

    @Column(name = "activa", nullable = false)
    var activa: Boolean = true

)