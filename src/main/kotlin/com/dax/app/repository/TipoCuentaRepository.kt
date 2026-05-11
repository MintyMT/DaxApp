package com.dax.app.repository

import com.dax.app.entity.TipoCuenta
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TipoCuentaRepository : JpaRepository<TipoCuenta, UUID> {
}