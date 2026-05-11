package com.dax.app.service

import com.dax.app.entity.TipoCuenta
import com.dax.app.repository.TipoCuentaRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TipoCuentaService(private val tipoCuentaRepository: TipoCuentaRepository) {

    /**
     * Este es el catálogo que el frontend pedirá apenas el usuario
     * quiera crear una cuenta nueva para llenar el selector.
     */
    fun obtenerTodos(): List<TipoCuenta> {
        return tipoCuentaRepository.findAll()
    }

    fun obtenerPorId(id: UUID): TipoCuenta? {
        return tipoCuentaRepository.findById(id).orElse(null)
    }

    fun guardar(tipoCuenta: TipoCuenta): TipoCuenta {
        return tipoCuentaRepository.save(tipoCuenta)
    }
}