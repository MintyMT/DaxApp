package com.dax.app.service

import com.dax.app.entity.Categoria
import com.dax.app.entity.Usuario
import com.dax.app.repository.CategoriaRepository
import com.dax.app.repository.UsuarioRepository
import com.dax.app.repository.TransaccionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CategoriaService(
    private val categoriaRepository: CategoriaRepository,
    private val usuarioRepository: UsuarioRepository,
    private val transaccionRepository: TransaccionRepository) {

    fun obtenerTodoPorUsuario(usuarioId: UUID): List<Categoria> {
        val globales = categoriaRepository.findByUsuarioIdIsNull()
        val personalizadas = categoriaRepository.findByUsuarioId(usuarioId)

        return globales + personalizadas
    }

    fun guardar(categoria: Categoria): Categoria {
        return categoriaRepository.save(categoria)
    }

    @Transactional
    fun crearCategoria(nombre: String, tipo: String, usuarioId: UUID): Categoria {
        
        if (tipo != "INGRESO" && tipo != "GASTO") {
            throw IllegalArgumentException("El tipo de categoría debe ser INGRESO o GASTO")
        }

        
        val usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow { RuntimeException("Usuario con ID $usuarioId no encontrado") }

        
        val nuevaCategoria = Categoria(
            nombre = nombre,
            tipo = tipo,
            usuarioId = usuarioId
        )

        
        return categoriaRepository.save(nuevaCategoria)
    }

    @Transactional
    fun eliminarCategoriaPersonalizada(categoriaId: UUID, usuarioId: UUID) {
        
        val categoriaABorrar = categoriaRepository.findById(categoriaId)
            .orElseThrow { RuntimeException("Categoría no encontrada") }

        
        if (categoriaABorrar.usuarioId != usuarioId) {
            throw RuntimeException("No tienes permiso para eliminar esta categoría o es una categoría del sistema")
        }

        
        val nombreDestino = if (categoriaABorrar.tipo == "GASTO") "Otros Gastos" else "Otros Ingresos"

        val categoriaDestino = categoriaRepository.findByNombreAndUsuarioIdIsNull(nombreDestino)
            ?: throw RuntimeException("Error crítico: No se encontró la categoría del sistema '$nombreDestino'")

        
        val transaccionesAfectadas = transaccionRepository.findByCategoriaId(categoriaId)
        if (transaccionesAfectadas.isNotEmpty()) {
            val transaccionesActualizadas = transaccionesAfectadas.map { tx ->
                tx.copiarConNuevaCategoria(categoriaDestino.id!!)
            }
            transaccionRepository.saveAll(transaccionesActualizadas)
        }

        
        categoriaRepository.delete(categoriaABorrar)
    }

    @Transactional
    fun editarNombreCategoria(categoriaId: UUID, nuevoNombre: String, usuarioId: UUID): Categoria {
        
        val categoriaExistente = categoriaRepository.findById(categoriaId)
            .orElseThrow { RuntimeException("Categoría no encontrada") }

        
        if (categoriaExistente.usuarioId == null) {
            throw RuntimeException("No se pueden editar las categorías predeterminadas del sistema")
        }

        
        if (categoriaExistente.usuarioId != usuarioId) {
            throw RuntimeException("No tienes permisos para editar esta categoría")
        }

        
        
        val categoriaActualizada = categoriaExistente.copiarConNuevoNombre(nuevoNombre)

        return categoriaRepository.save(categoriaActualizada)
    }

}