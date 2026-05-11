package com.dax.app.service

import com.dax.app.entity.Cuenta
import com.dax.app.repository.CuentaRepository
import com.dax.app.repository.TipoCuentaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

@Service
class CuentaService(
    private val cuentaRepository: CuentaRepository,
    private val tipoCuentaRepository: TipoCuentaRepository) {

fun calcularBalanceGlobal(usuarioId: UUID): BigDecimal {
        val listadeCuentas = cuentaRepository.findByUsuarioId(usuarioId)

        return listadeCuentas
            .map { it.saldo }
            .fold(BigDecimal.ZERO, BigDecimal::add)
    }
    fun obtenerCuentasUsuario(usuarioId: UUID): List<Cuenta> {
        return cuentaRepository.findByUsuarioId(usuarioId)
    }

    @Transactional
    fun agregarCuenta(usuarioId: UUID, nombre: String, saldoInicial: BigDecimal, tipoId: UUID): Cuenta {
        val tipoCuenta = tipoCuentaRepository.findById(tipoId)
            .orElseThrow { RuntimeException("Tipo de cuenta seleccionado no es valido") }

        val nuevaCuenta = Cuenta(
            usuarioId = usuarioId,
            nombre = nombre,
            saldo = saldoInicial,
            tipo = tipoCuenta
        )

        return cuentaRepository.save(nuevaCuenta)
    }

}