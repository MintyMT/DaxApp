package com.dax.app.entity

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "usuarios")
class Usuario(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(nullable = false, length = 100)
    val nombre: String,

    @Column(nullable = false, unique = true, length = 150)
    val email: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val password: String,

    @Column(name = "fecha_creacion", nullable = false)
    val fechaCreacion: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "fecha_solicitud_borrado")
    var fechaSolicitudBorrado: OffsetDateTime? = null
)