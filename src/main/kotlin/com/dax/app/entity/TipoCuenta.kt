package com.dax.app.entity

import jakarta.persistence.*
import java.util.UUID


@Entity
@Table(name = "tipos_cuenta")
class TipoCuenta(
    @Id @GeneratedValue
    val id: UUID? = null,
    val nombre: String
)
