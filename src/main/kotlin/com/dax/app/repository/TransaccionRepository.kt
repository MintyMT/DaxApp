package com.dax.app.repository

import com.dax.app.entity.Cuenta
import com.dax.app.entity.Transaccion
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import java.time.OffsetDateTime

@Repository
interface TransaccionRepository : JpaRepository <Transaccion, UUID>{
    fun findByUsuarioIdAndFechaBetweenOrderByFechaDesc(
        usuarioId: UUID,
        inicio: OffsetDateTime,
        fin: OffsetDateTime
    ): List<Transaccion>

    fun findByUsuarioIdAndTipoAndFechaBetweenOrderByFechaDesc(
        usuarioId: UUID,
        tipo: String,
        inicio: OffsetDateTime,
        fin: OffsetDateTime
    ): List<Transaccion>

    fun findByUsuarioIdAndCuentaOrigenIdAndFechaBetweenOrderByFechaDesc(
        usuarioId: UUID, cuentaId: UUID, inicio: OffsetDateTime, fin: OffsetDateTime
    ): List<Transaccion>

    fun findByUsuarioIdAndCuentaDestinoIdAndFechaBetweenOrderByFechaDesc(
        usuarioId: UUID, cuentaId: UUID, inicio: OffsetDateTime, fin: OffsetDateTime
    ): List<Transaccion>

    fun findByCuentaOrigenIdOrCuentaDestinoId(
        origenId: UUID, destinoId: UUID
    ): List<Transaccion>

    fun findByCategoriaId(categoriaId: UUID): List<Transaccion>

    @Modifying
    @Transactional
    fun deleteByUsuarioId(usuarioId: UUID)
}