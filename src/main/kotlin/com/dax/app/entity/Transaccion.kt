package com.dax.app.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "transacciones")
class Transaccion(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(nullable = false, precision = 12, scale = 2)
    val monto: BigDecimal,

    @Column(nullable = false, length = 15)
    val tipo: String, // "INGRESO", "GASTO", "TRANSFERENCIA"

    @Column(columnDefinition = "TEXT")
    val descripcion: String? = null,

    @Column(nullable = false)
    val fecha: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "notas", columnDefinition = "TEXT")
    val notas: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_origen_id")
    val cuentaOrigen: Cuenta? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_destino_id")
    val cuentaDestino: Cuenta? = null,

    @Column(name = "categoria_id")
    val categoriaId: UUID? = null,

    @Column(name = "usuario_id")
    val usuarioId: UUID? = null
) {

    fun copiarConNuevaCategoria(nuevaCategoriaId: UUID): Transaccion {
        return Transaccion(
            id = this.id,
            monto = this.monto,
            tipo = this.tipo,
            descripcion = this.descripcion,
            fecha = this.fecha,
            notas = this.notas,
            cuentaOrigen = this.cuentaOrigen,
            cuentaDestino = this.cuentaDestino,
            categoriaId = nuevaCategoriaId,
            usuarioId = this.usuarioId
        )
    }
}
