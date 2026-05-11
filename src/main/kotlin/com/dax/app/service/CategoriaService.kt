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
    fun crearCategoria(nombre: String, tipo: String, icono: String, usuarioId: UUID): Categoria {
        // 1. Validar que el tipo sea válido (INGRESO o GASTO) para evitar errores del CHECK en DB
        if (tipo != "INGRESO" && tipo != "GASTO") {
            throw IllegalArgumentException("El tipo de categoría debe ser INGRESO o GASTO")
        }

        // 2. Obtener la referencia del usuario desde la base de datos
        val usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow { RuntimeException("Usuario con ID $usuarioId no encontrado") }

        // 3. Instanciar la nueva categoría
        val nuevaCategoria = Categoria(
            nombre = nombre,
            tipo = tipo,
            usuarioId = usuarioId
        )

        // 4. Guardar en Supabase usando el repositorio
        return categoriaRepository.save(nuevaCategoria)
    }

    @Transactional
    fun eliminarCategoriaPersonalizada(categoriaId: UUID, usuarioId: UUID) {
        // 1. Verificar que la categoría existe
        val categoriaABorrar = categoriaRepository.findById(categoriaId)
            .orElseThrow { RuntimeException("Categoría no encontrada") }

        // 2. Validar que pertenece al usuario (No permitir borrar globales ni de otros)
        if (categoriaABorrar.usuarioId != usuarioId) {
            throw RuntimeException("No tienes permiso para eliminar esta categoría o es una categoría del sistema")
        }

        // 3. Determinar la categoría de destino según el tipo
        val nombreDestino = if (categoriaABorrar.tipo == "GASTO") "Otros Gastos" else "Otros Ingresos"

        val categoriaDestino = categoriaRepository.findByNombreAndUsuarioIdIsNull(nombreDestino)
            ?: throw RuntimeException("Error crítico: No se encontró la categoría del sistema '$nombreDestino'")

        // 4. Reasignar todas las transacciones vinculadas (Manteniendo Inmutabilidad)
        val transaccionesAfectadas = transaccionRepository.findByCategoriaId(categoriaId)
        if (transaccionesAfectadas.isNotEmpty()) {
            val transaccionesActualizadas = transaccionesAfectadas.map { tx ->
                tx.copiarConNuevaCategoria(categoriaDestino.id!!)
            }
            transaccionRepository.saveAll(transaccionesActualizadas)
        }

        // 5. Borrar la categoría de forma segura en PostgreSQL
        categoriaRepository.delete(categoriaABorrar)
    }

    @Transactional
    fun editarNombreCategoria(categoriaId: UUID, nuevoNombre: String, usuarioId: UUID): Categoria {
        // 1. Buscar la categoría
        val categoriaExistente = categoriaRepository.findById(categoriaId)
            .orElseThrow { RuntimeException("Categoría no encontrada") }

        // 2. Validar que no sea una categoría global (usuarioId == null)
        if (categoriaExistente.usuarioId == null) {
            throw RuntimeException("No se pueden editar las categorías predeterminadas del sistema")
        }

        // 3. Validar que la categoría pertenece al usuario que hace la solicitud
        if (categoriaExistente.usuarioId != usuarioId) {
            throw RuntimeException("No tienes permisos para editar esta categoría")
        }

        // 4. Crear la copia con el nuevo nombre y guardarla
        // Al mantener el ID original, JPA ejecutará un UPDATE en PostgreSQL
        val categoriaActualizada = categoriaExistente.copiarConNuevoNombre(nuevoNombre)

        return categoriaRepository.save(categoriaActualizada)
    }

}
