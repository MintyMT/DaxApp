//package com.dax.app
//
//import com.dax.app.service.*
//import org.springframework.boot.CommandLineRunner
//import org.springframework.stereotype.Component
//import com.dax.app.repository.UsuarioRepository
//import java.math.BigDecimal
//import java.time.OffsetDateTime
//import java.util.UUID
//
//@Component
//class DaxRunner(
//    private val usuarioService: UsuarioService,
//    private val categoriaService: CategoriaService,
//    private val cuentaService: CuentaService,
//    private val transaccionService: TransaccionService,
//    private val tipoCuentaService: TipoCuentaService,
//    private val usuarioRepository: UsuarioRepository
//) : CommandLineRunner {
//
//    /*override fun run(vararg args: String) {
//        println("\n--- INICIANDO PRUEBA INTEGRAL DE CREACIÓN DE DATOS PARA DAX ---")
//
//        try {
//            // --- 1. Crear 1 Usuario Nuevo ---
//            println("\n--- CREANDO USUARIO ---")
//            val nuevoUsuario = usuarioService.registrarUsuario(
//                nombre = "Usuario Prueba Dax",
//                email = "prueba.dax@example.com",
//                contra = "Password123" // En un entorno real, esto sería hasheado
//            )
//            val usuarioId = nuevoUsuario.id!!
//            println("✅ Usuario creado: ${nuevoUsuario.nombre} (ID: $usuarioId)")
//
//            // --- 2. Crear 3 Categorías Personalizadas ---
//            println("\n--- CREANDO CATEGORÍAS PERSONALIZADAS ---")
//            val catSueldo = categoriaService.crearCategoria(
//                nombre = "Sueldo",
//                tipo = "INGRESO",
//                icono = "ic_money", // Asegúrate de que este campo sea String y no String? en tu entidad
//                usuarioId = usuarioId
//            )
//            println("✅ Categoría creada: ${catSueldo.nombre} (ID: ${catSueldo.id})")
//
//            val catComida = categoriaService.crearCategoria(
//                nombre = "Comida",
//                tipo = "GASTO",
//                icono = "ic_food",
//                usuarioId = usuarioId
//            )
//            println("✅ Categoría creada: ${catComida.nombre} (ID: ${catComida.id})")
//
//            val catGimnasio = categoriaService.crearCategoria(
//                nombre = "Gimnasio",
//                tipo = "GASTO",
//                icono = "ic_gym",
//                usuarioId = usuarioId
//            )
//            println("✅ Categoría creada: ${catGimnasio.nombre} (ID: ${catGimnasio.id})")
//
//            // --- 3. Crear 3 Cuentas ---
//            println("\n--- CREANDO CUENTAS ---")
//            val tiposCuenta = tipoCuentaService.obtenerTodos()
//            if (tiposCuenta.isEmpty()) {
//                throw RuntimeException("No hay tipos de cuenta definidos. Por favor, asegúrate de que existan en la DB.")
//            }
//            // Intentamos encontrar tipos específicos, si no, usamos el primero disponible
//            val tipoCuentaEfectivo = tiposCuenta.firstOrNull { it.nombre.equals("Efectivo", ignoreCase = true) } ?: tiposCuenta.first()
//            val tipoCuentaBanco = tiposCuenta.firstOrNull { it.nombre.equals("Banco", ignoreCase = true) } ?: tiposCuenta.first()
//
//            val cuentaEfectivo = cuentaService.agregarCuenta(
//                usuarioId = usuarioId,
//                nombre = "Efectivo",
//                saldoInicial = BigDecimal("500.00"),
//                tipoId = tipoCuentaEfectivo.id!!
//            )
//            println("✅ Cuenta creada: ${cuentaEfectivo.nombre} (ID: ${cuentaEfectivo.id}) Saldo: ${cuentaEfectivo.saldo}")
//
//            val cuentaBanco = cuentaService.agregarCuenta(
//                usuarioId = usuarioId,
//                nombre = "Banco Pichincha",
//                saldoInicial = BigDecimal("1500.00"),
//                tipoId = tipoCuentaBanco.id!!
//            )
//            println("✅ Cuenta creada: ${cuentaBanco.nombre} (ID: ${cuentaBanco.id}) Saldo: ${cuentaBanco.saldo}")
//
//            val cuentaAhorros = cuentaService.agregarCuenta(
//                usuarioId = usuarioId,
//                nombre = "Ahorros",
//                saldoInicial = BigDecimal("2000.00"),
//                tipoId = tipoCuentaBanco.id!! // Usamos el mismo tipo de banco para ahorros
//            )
//            println("✅ Cuenta creada: ${cuentaAhorros.nombre} (ID: ${cuentaAhorros.id}) Saldo: ${cuentaAhorros.saldo}")
//
//            // --- 4. Crear 5 Transacciones Variadas ---
//            println("\n--- CREANDO TRANSACCIONES ---")
//
//            // Ingreso 1 (Sueldo)
//            transaccionService.registrarIngreso(
//                cuentaId = cuentaBanco.id!!,
//                monto = BigDecimal("1000.00"),
//                categoriaId = catSueldo.id!!,
//                descripcion = "Pago de nómina",
//                fecha = OffsetDateTime.now().minusDays(5),
//                notas = "Quincena"
//            )
//            println("✅ Transacción de Ingreso creada: Pago de nómina")
//
//            // Gasto 1 (Comida)
//            transaccionService.registrarGasto(
//                cuentaId = cuentaEfectivo.id!!,
//                monto = BigDecimal("15.50"),
//                categoriaId = catComida.id!!,
//                descripcion = "Almuerzo",
//                fecha = OffsetDateTime.now().minusDays(2),
//                notas = null
//            )
//            println("✅ Transacción de Gasto creada: Almuerzo")
//
//            // Gasto 2 (Gimnasio)
//            transaccionService.registrarGasto(
//                cuentaId = cuentaBanco.id!!,
//                monto = BigDecimal("30.00"),
//                categoriaId = catGimnasio.id!!,
//                descripcion = "Mensualidad gimnasio",
//                fecha = OffsetDateTime.now().minusDays(1),
//                notas = "Cuota de mayo"
//            )
//            println("✅ Transacción de Gasto creada: Mensualidad gimnasio")
//
//            // Ingreso 2 (Transferencia de Ahorros a Banco)
//            transaccionService.registrarTransferencia(
//                cuentaOrigenId = cuentaAhorros.id!!,
//                cuentaDestinoId = cuentaBanco.id!!,
//                monto = BigDecimal("200.00"),
//                descripcion = "Transferencia de ahorros",
//                fecha = OffsetDateTime.now().minusHours(3),
//                notas = "Para gastos del mes"
//            )
//            println("✅ Transacción de Transferencia creada: De Ahorros a Banco")
//
//            // Gasto 3 (Comida, desde Banco)
//            transaccionService.registrarGasto(
//                cuentaId = cuentaBanco.id!!,
//                monto = BigDecimal("25.00"),
//                categoriaId = catComida.id!!,
//                descripcion = "Cena con amigos",
//                fecha = OffsetDateTime.now(),
//                notas = "Pizza y bebidas"
//            )
//            println("✅ Transacción de Gasto creada: Cena con amigos")
//
//            println("\n--- VERIFICANDO BALANCE GLOBAL ---")
//            val balanceGlobal = cuentaService.calcularBalanceGlobal(usuarioId)
//            println("💰 Balance Global del usuario ${nuevoUsuario.nombre}: $$balanceGlobal")
//
//            println("\n✅ ¡POBLACIÓN DE DATOS INICIAL COMPLETADA EXITOSAMENTE!")
//
//        } catch (e: Exception) {
//            println("\n❌ ERROR DURANTE LA CREACIÓN DE DATOS: ${e.message}")
//            e.printStackTrace()
//        }
//
//        println("\n--- FIN DEL RUNNER DE CREACIÓN DE DATOS ---\n")
//    }
//
//    override fun run(vararg args: String) {
//        println("\n--- INICIANDO PRUEBA INTEGRAL DE DAX (CREACIÓN Y ELIMINACIÓN) ---")
//
//        try {
//            // --- PARTE 1: CREACIÓN DE DATOS (YA FUNCIONAL) ---
//            println("\n--- [1/2] FASE DE CREACIÓN DE DATOS ---")
//            val nuevoUsuario = usuarioService.registrarUsuario(
//                nombre = "Prueba Eliminacion",
//                email = "test.delete.${UUID.randomUUID().toString().take(5)}@example.com",
//                contra = "Dax2024*"
//            )
//            val usuarioId = UUID.fromString("27552ccc-7e49-4ba4-9bab-07b31c59e2e6")
//
//            val catGasto = categoriaService.crearCategoria("Cena Fuera", "GASTO", "ic_food", usuarioId)
//            val catIngreso = categoriaService.crearCategoria("Venta Garage", "INGRESO", "ic_sell", usuarioId)
//
//            val tipos = tipoCuentaService.obtenerTodos()
//            val c1 = cuentaService.agregarCuenta(usuarioId, "Efectivo2", BigDecimal("100.00"), tipos.first().id!!)
//            val c2 = cuentaService.agregarCuenta(usuarioId, "Banco2", BigDecimal("500.00"), tipos.first().id!!)
//
//            // Registramos algunas transacciones para tener qué borrar
//            transaccionService.registrarGasto(c1.id!!, BigDecimal("10.00"), catGasto.id!!, "Pizza", OffsetDateTime.now(), null)
//            transaccionService.registrarIngreso(c2.id!!, BigDecimal("50.00"), catIngreso.id!!, "Venta Silla", OffsetDateTime.now(), null)
//
//            println("✅ Datos iniciales creados exitosamente.")
//
//            // --- PARTE 2: FASE DE ELIMINACIÓN ---
//            println("\n--- [2/2] FASE DE PRUEBAS DE ELIMINACIÓN ---")
//
//            // 1. ELIMINAR 1 TRANSACCIÓN
//            try {
//                val txs = transaccionService.obtenerTransaccionesFiltradas(usuarioId, "ALL", OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(1))
//                val txABorrar = txs.first()
//                println("🗑️ Probando eliminación de transacción: [${txABorrar.descripcion} - $${txABorrar.monto}]...")
//                transaccionService.eliminarTransaccion(txABorrar.id!!)
//                println("   Resultado: ÉXITO (El saldo de la cuenta debería haberse reajustado)")
//            } catch (e: Exception) {
//                println("   Resultado: ERROR al eliminar transacción: ${e.message}")
//            }
//
//            // 2. ELIMINAR 1 CATEGORÍA PERSONALIZADA
//            try {
//                println("🗑️ Probando eliminación de categoría: [${catGasto.nombre}]...")
//                // Antes de borrar, verificamos cuántas transacciones la usan
//                val antes = transaccionService.obtenerTransaccionesFiltradas(usuarioId, "GASTO", OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(1))
//                println("   Info: Transacciones con esta categoría antes de borrar: ${antes.size}")
//
//                categoriaService.eliminarCategoriaPersonalizada(catGasto.id!!, usuarioId)
//
//                // Después de borrar, verificamos si las transacciones se movieron a "Otros Gastos"
//                val despues = transaccionService.obtenerTransaccionesFiltradas(usuarioId, "GASTO", OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(1))
//                println("   Resultado: ÉXITO. Las transacciones ahora apuntan a la categoría global.")
//            } catch (e: Exception) {
//                println("   Resultado: ERROR al eliminar categoría: ${e.message}")
//            }
//
//            // 3. ELIMINAR 1 CUENTA
//            try {
//                println("🗑️ Probando eliminación de cuenta: [${c1.nombre}]...")
//                // El service debería manejar el borrado de transacciones vinculadas o advertir
//                transaccionService.eliminarCuentaYReajustarSaldos(c1.id!!)
//                println("   Resultado: ÉXITO (Cuenta y sus transacciones eliminadas, saldos de transferencias reajustados)")
//            } catch (e: Exception) {
//                println("   Resultado: ERROR al eliminar cuenta: ${e.message}")
//            }
//
//            // BALANCE FINAL
//            val balanceFinal = cuentaService.calcularBalanceGlobal(usuarioId)
//            println("\n💰 Balance Global final tras eliminaciones: $$balanceFinal")
//            println("\n✅ FASE DE ELIMINACIÓN COMPLETADA")
//
//        } catch (e: Exception) {
//            println("\n❌ ERROR CRÍTICO EN EL RUNNER: ${e.message}")
//            e.printStackTrace()
//        }
//
//        println("\n--- FIN DEL RUNNER ---\n")
//    }
//
//    override fun run(vararg args: String) {
//
//        // --- CONFIGURACIÓN DE LA PRUEBA ---
//        // Reemplaza este UUID con el ID real del usuario que deseas borrar de tu Supabase
//        val usuarioPruebaId = UUID.fromString("INSERTAR-AQUI-TU-UUID")
//
//        println("\n--- ⏱️ INICIANDO PRUEBA DE BORRADO DIFERIDO (3 MINUTOS) ---")
//
//        try {
//            // 1. Verificar si el usuario existe antes de empezar
//            val usuarioExiste = usuarioRepository.existsById(usuarioPruebaId)
//            if (!usuarioExiste) {
//                println("❌ ERROR: El usuario con ID $usuarioPruebaId no existe en la base de datos.")
//                return
//            }
//
//            // 2. Solicitar el borrado
//            usuarioService.solicitarBorradoDeCuenta(usuarioPruebaId)
//            println("\n🚀 Solicitud de borrado iniciada para el usuario [$usuarioPruebaId].")
//            println("⏳ El sistema esperará 3 minutos para que el proceso automático de limpieza se ejecute.")
//
//            // 3. Monitoreo en tiempo real (Bucle de espera de 3.5 minutos para dar margen al @Scheduled)
//            val totalSegundos = 210 // 3 minutos y medio
//            val intervalo = 30 // Cada 30 segundos imprimimos progreso
//
//            for (segundosPasados in 0..totalSegundos step intervalo) {
//                if (segundosPasados > 0) {
//                    println("... Han pasado $segundosPasados segundos (${segundosPasados / 60}m ${segundosPasados % 60}s)")
//                }
//
//                // Dormimos el hilo del Runner, pero la App sigue viva
//                Thread.sleep(intervalo * 1000L)
//            }
//
//            // 4. Confirmación Final
//            println("\n--- 🏁 FINALIZANDO ESPERA. VERIFICANDO RESULTADO EN SUPABASE ---")
//            val usuarioFinal = usuarioRepository.existsById(usuarioPruebaId)
//
//            if (!usuarioFinal) {
//                println("\n✅ ÉXITO: El usuario y todos sus datos relacionados (transacciones, cuentas, categorías) han sido eliminados de Supabase.")
//            } else {
//                println("\n⏳ ESPERANDO: El usuario aún existe. Esto puede deberse a que el @Scheduled aún no ha corrido o el tiempo de gracia no se ha cumplido.")
//                val u = usuarioRepository.findById(usuarioPruebaId).get()
//                println("   Estado actual en DB: Solicitado el ${u.fechaSolicitudBorrado}")
//            }
//
//        } catch (e: Exception) {
//            println("\n❌ ERROR DURANTE LA PRUEBA: ${e.message}")
//            e.printStackTrace()
//        }
//
//        println("\n--- FIN DE LA PRUEBA DE BORRADO ---\n")
//    }*/
//
//    override fun run(vararg args: String) {
//
//
//        // --- CONFIGURACIÓN DE LA PRUEBA ---
//        // Reemplaza este UUID con el ID real del usuario que deseas borrar de tu Supabase
//        val usuarioPruebaId = UUID.fromString("27552ccc-7e49-4ba4-9bab-07b31c59e2e6")
//
//        println("\n--- ⏱️ INICIANDO PRUEBA DE BORRADO DIFERIDO (3 MINUTOS) ---")
//
//        try {
//            // 1. Verificar si el usuario existe antes de empezar
//            val usuarioExiste = usuarioRepository.existsById(usuarioPruebaId)
//            if (!usuarioExiste) {
//                println("❌ ERROR: El usuario con ID $usuarioPruebaId no existe en la base de datos.")
//                return
//            }
//
//            // 2. Solicitar el borrado
//            usuarioService.solicitarBorradoDeCuenta(usuarioPruebaId)
//            println("\n🚀 Solicitud de borrado iniciada para el usuario [$usuarioPruebaId].")
//            println("⏳ El sistema esperará 3 minutos para que el proceso automático de limpieza se ejecute.")
//
//            // 3. Monitoreo en tiempo real (Bucle de espera de 3.5 minutos para dar margen al @Scheduled)
//            val totalSegundos = 210 // 3 minutos y medio
//            val intervalo = 30 // Cada 30 segundos imprimimos progreso
//
//            for (segundosPasados in 0..totalSegundos step intervalo) {
//                if (segundosPasados > 0) {
//                    println("... Han pasado $segundosPasados segundos (${segundosPasados / 60}m ${segundosPasados % 60}s)")
//                }
//
//                // Dormimos el hilo del Runner, pero la App sigue viva
//                Thread.sleep(intervalo * 1000L)
//            }
//
//            // 4. Confirmación Final
//            println("\n--- 🏁 FINALIZANDO ESPERA. VERIFICANDO RESULTADO EN SUPABASE ---")
//            val usuarioFinal = usuarioRepository.existsById(usuarioPruebaId)
//
//            if (!usuarioFinal) {
//                println("\n✅ ÉXITO: El usuario y todos sus datos relacionados (transacciones, cuentas, categorías) han sido eliminados de Supabase.")
//            } else {
//                println("\n⏳ ESPERANDO: El usuario aún existe. Esto puede deberse a que el @Scheduled aún no ha corrido o el tiempo de gracia no se ha cumplido.")
//                val u = usuarioRepository.findById(usuarioPruebaId).get()
//                println("   Estado actual en DB: Solicitado el ${u.fechaSolicitudBorrado}")
//            }
//
//        } catch (e: Exception) {
//            println("\n❌ ERROR DURANTE LA PRUEBA: ${e.message}")
//            e.printStackTrace()
//        }
//
//        println("\n--- FIN DE LA PRUEBA DE BORRADO ---\n")
//    }
//}
