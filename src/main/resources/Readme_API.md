# Documentación Técnica de la API - DaxApp

Este documento describe los endpoints disponibles en la API de DaxApp, diseñada para la gestión financiera personal. Está dirigido a desarrolladores frontend que necesiten interactuar con el backend.

## Tabla de Contenido

1.  [Usuarios](#usuarios)
2.  [Cuentas](#cuentas)
3.  [Tipos de Cuenta](#tipos-de-cuenta)
4.  [Categorías](#categorías)
5.  [Presupuestos](#presupuestos)
6.  [Transacciones](#transacciones)

---

## 1. Usuarios

Módulo para la gestión de usuarios, incluyendo registro, inicio de sesión y funcionalidades relacionadas con la cuenta de usuario.

### 1.1. Registrar Nuevo Usuario

Registra un nuevo usuario en el sistema.

*   **URL:** `/api/usuarios/registro`
*   **Método:** `POST`
*   **Descripción:** Crea una nueva cuenta de usuario con los datos proporcionados.
*   **Request Body:** `RegistroRequest`

    ```json
    {
      "nombre": "Juan Perez",
      "email": "juan.perez@example.com",
      "contra": "passwordSeguro123"
    }
    ```
*   **Response (201 Created):** `UsuarioResponse`

    ```json
    {
      "id": "c1d2e3f4-a5b6-7890-1234-567890abcdef",
      "nombre": "Juan Perez",
      "email": "juan.perez@example.com",
      "fechaSolicitudBorrado": null
    }
    ```

### 1.2. Iniciar Sesión

Autentica un usuario y devuelve sus datos.

*   **URL:** `/api/usuarios/login`
*   **Método:** `POST`
*   **Descripción:** Permite a un usuario iniciar sesión con su email y contraseña. Si había una solicitud de borrado pendiente, esta se cancela.
*   **Request Body:** `LoginRequest`

    ```json
    {
      "email": "juan.perez@example.com",
      "contra": "passwordSeguro123"
    }
    ```
*   **Response (200 OK):** `UsuarioResponse`

    ```json
    {
      "id": "c1d2e3f4-a5b6-7890-1234-567890abcdef",
      "nombre": "Juan Perez",
      "email": "juan.perez@example.com",
      "fechaSolicitudBorrado": null
    }
    ```
*   **Response (400 Bad Request):** Credenciales inválidas.

### 1.3. Solicitar Borrado de Cuenta

Inicia el proceso de borrado lógico de la cuenta de un usuario.

*   **URL:** `/api/usuarios/{id}/solicitar-borrado`
*   **Método:** `POST`
*   **Descripción:** Marca la cuenta de un usuario para borrado. El usuario tiene un período de gracia (ej. 3 minutos) para revertir la acción iniciando sesión nuevamente.
*   **Path Variables:**

| Nombre | Tipo | Descripción          |
| :----- | :--- | :------------------- |
| `id`   | UUID | ID del usuario       |

*   **Response (200 OK):** `String`

    ```
    Proceso de borrado iniciado. Tienes 3 minutos para arrepentirte e iniciar sesión.
    ```
*   **Response (404 Not Found):** Usuario no encontrado.

---

## 2. Cuentas

Módulo para la gestión de las cuentas financieras de un usuario.

### 2.1. Obtener Cuentas por Usuario

Obtiene todas las cuentas activas de un usuario.

*   **URL:** `/api/cuentas/usuario/{usuarioId}`
*   **Método:** `GET`
*   **Descripción:** Recupera una lista de todas las cuentas asociadas a un `usuarioId` específico.
*   **Path Variables:**

| Nombre      | Tipo | Descripción          |
| :---------- | :--- | :------------------- |
| `usuarioId` | UUID | ID del usuario       |

*   **Response (200 OK):** `List<CuentaResponse>`

    ```json
    [
      {
        "id": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
        "nombre": "Cuenta Principal",
        "saldo": 1500.75,
        "tipoNombre": "Ahorro",
        "activa": true
      },
      {
        "id": "f0e9d8c7-b6a5-4321-fedc-ba9876543210",
        "nombre": "Tarjeta de Crédito",
        "saldo": -500.00,
        "tipoNombre": "Crédito",
        "activa": true
      }
    ]
    ```
*   **Response (404 Not Found):** Usuario no encontrado.

### 2.2. Obtener Balance Global

Calcula el balance global de todas las cuentas de un usuario.

*   **URL:** `/api/cuentas/usuario/{usuarioId}/balance`
*   **Método:** `GET`
*   **Descripción:** Suma los saldos de todas las cuentas activas de un usuario para obtener un balance total.
*   **Path Variables:**

| Nombre      | Tipo | Descripción          |
| :---------- | :--- | :------------------- |
| `usuarioId` | UUID | ID del usuario       |

*   **Response (200 OK):** `BalanceGlobalResponse`

    ```json
    {
      "balanceTotal": 1000.75
    }
    ```
*   **Response (404 Not Found):** Usuario no encontrado.

### 2.3. Crear Nueva Cuenta

Crea una nueva cuenta para un usuario.

*   **URL:** `/api/cuentas`
*   **Método:** `POST`
*   **Descripción:** Registra una nueva cuenta financiera para un usuario, especificando su nombre, saldo inicial y tipo.
*   **Request Body:** `NuevaCuentaRequest`

    ```json
    {
      "usuarioId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
      "nombre": "Nueva Cuenta",
      "saldoInicial": 0.00,
      "tipoId": "1a2b3c4d-5e6f-7890-1234-567890abcdef"
    }
    ```
*   **Response (201 Created):** `CuentaResponse`

    ```json
    {
      "id": "b2c3d4e5-f6a7-8901-2345-67890abcdef0",
      "nombre": "Nueva Cuenta",
      "saldo": 0.00,
      "tipoNombre": "Corriente",
      "activa": true
    }
    ```
*   **Response (400 Bad Request):** Datos de entrada inválidos (ej. `usuarioId` o `tipoId` no existen).

### 2.4. Eliminar Cuenta (Borrado Lógico)

Desactiva una cuenta específica.

*   **URL:** `/api/cuentas/{id}`
*   **Método:** `DELETE`
*   **Descripción:** Realiza un borrado lógico de una cuenta, marcándola como inactiva. Las transacciones asociadas no se eliminan, pero la cuenta ya no aparecerá en las listas activas.
*   **Path Variables:**

| Nombre | Tipo | Descripción          |
| :----- | :--- | :------------------- |
| `id`   | UUID | ID de la cuenta      |

*   **Response (204 No Content):** La cuenta fue desactivada exitosamente.
*   **Response (404 Not Found):** Cuenta no encontrada.

---

## 3. Tipos de Cuenta

Módulo para consultar los tipos de cuenta predefinidos en el sistema.

### 3.1. Obtener Todos los Tipos de Cuenta

Obtiene todos los tipos de cuenta disponibles en el sistema.

*   **URL:** `/api/tipos-cuenta`
*   **Método:** `GET`
*   **Descripción:** Recupera una lista de todos los tipos de cuenta que pueden ser asignados a las cuentas de los usuarios (ej. "Ahorro", "Corriente", "Crédito").
*   **Response (200 OK):** `List<TipoCuentaResponse>`

    ```json
    [
      {
        "id": "1a2b3c4d-5e6f-7890-1234-567890abcdef",
        "nombre": "Ahorro"
      },
      {
        "id": "f0e9d8c7-b6a5-4321-fedc-ba9876543210",
        "nombre": "Corriente"
      },
      {
        "id": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
        "nombre": "Crédito"
      }
    ]
    ```

### 3.2. Obtener Tipo de Cuenta por ID

Obtiene un tipo de cuenta específico por su ID.

*   **URL:** `/api/tipos-cuenta/{id}`
*   **Método:** `GET`
*   **Descripción:** Recupera los detalles de un tipo de cuenta utilizando su identificador único.
*   **Path Variables:**

| Nombre | Tipo | Descripción             |
| :----- | :--- | :---------------------- |
| `id`   | UUID | ID del tipo de cuenta   |

*   **Response (200 OK):** `TipoCuentaResponse`

    ```json
    {
      "id": "1a2b3c4d-5e6f-7890-1234-567890abcdef",
      "nombre": "Ahorro"
    }
    ```
*   **Response (404 Not Found):** Tipo de cuenta no encontrado.

---

## 4. Categorías

Módulo para la gestión de categorías de ingresos y gastos.

### 4.1. Listar Categorías por Usuario

Obtiene todas las categorías (globales y personalizadas) de un usuario.

*   **URL:** `/api/categorias/usuario/{usuarioId}`
*   **Método:** `GET`
*   **Descripción:** Recupera una lista de categorías, incluyendo las predefinidas del sistema y las creadas por el usuario.
*   **Path Variables:**

| Nombre      | Tipo | Descripción          |
| :---------- | :--- | :------------------- |
| `usuarioId` | UUID | ID del usuario       |

*   **Response (200 OK):** `List<CategoriaResponse>`

    ```json
    [
      {
        "id": "d1e2f3a4-b5c6-7890-1234-567890abcdef",
        "nombre": "Comida",
        "tipo": "GASTO",
        "esGlobal": true
      },
      {
        "id": "e2f3a4b5-c6d7-8901-2345-67890abcdef0",
        "nombre": "Salario",
        "tipo": "INGRESO",
        "esGlobal": true
      },
      {
        "id": "f3a4b5c6-d7e8-9012-3456-7890abcdef12",
        "nombre": "Transporte Personal",
        "tipo": "GASTO",
        "esGlobal": false
      }
    ]
    ```
*   **Response (404 Not Found):** Usuario no encontrado.

### 4.2. Crear Nueva Categoría

Crea una nueva categoría personalizada para un usuario.

*   **URL:** `/api/categorias`
*   **Método:** `POST`
*   **Descripción:** Permite a un usuario crear una categoría personalizada para organizar sus transacciones.
*   **Request Body:** `NuevaCategoriaRequest`

    ```json
    {
      "nombre": "Entretenimiento",
      "tipo": "GASTO",
      "usuarioId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
    }
    ```
*   **Response (200 OK):** `CategoriaResponse`

    ```json
    {
      "id": "g1h2i3j4-k5l6-7890-1234-567890abcdef",
      "nombre": "Entretenimiento",
      "tipo": "GASTO",
      "esGlobal": false
    }
    ```
*   **Response (400 Bad Request):** Datos de entrada inválidos (ej. `usuarioId` no existe, tipo inválido).

### 4.3. Editar Categoría

Edita el nombre de una categoría personalizada.

*   **URL:** `/api/categorias/{id}`
*   **Método:** `PUT`
*   **Descripción:** Actualiza el nombre de una categoría personalizada existente.
*   **Path Variables:**

| Nombre | Tipo | Descripción             |
| :----- | :--- | :---------------------- |
| `id`   | UUID | ID de la categoría      |

*   **Request Body:** `EditarCategoriaRequest`

    ```json
    {
      "nuevoNombre": "Ocio",
      "usuarioId": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
    }
    ```
*   **Response (200 OK):** `CategoriaResponse`

    ```json
    {
      "id": "g1h2i3j4-k5l6-7890-1234-567890abcdef",
      "nombre": "Ocio",
      "tipo": "GASTO",
      "esGlobal": false
    }
    ```
*   **Response (400 Bad Request):** Datos de entrada inválidos (ej. `usuarioId` no coincide, categoría no es personalizada).
*   **Response (404 Not Found):** Categoría no encontrada.

### 4.4. Eliminar Categoría Personalizada

Elimina una categoría personalizada de un usuario.

*   **URL:** `/api/categorias/{id}`
*   **Método:** `DELETE`
*   **Descripción:** Elimina una categoría creada por el usuario. Las transacciones que estaban asociadas a esta categoría se reasignan automáticamente a categorías del sistema.
*   **Path Variables:**

| Nombre | Tipo | Descripción             |
| :----- | :--- | :---------------------- |
| `id`   | UUID | ID de la categoría      |

*   **Query Parameters:**

| Nombre      | Tipo | Descripción                               |
| :---------- | :--- | :---------------------------------------- |
| `usuarioId` | UUID | ID del usuario propietario de la categoría |

*   **Response (200 OK):** `String`

    ```
    Categoría eliminada. Las transacciones se movieron a categorías del sistema.
    ```
*   **Response (400 Bad Request):** La categoría es global y no puede ser eliminada.
*   **Response (404 Not Found):** Categoría o usuario no encontrado.

---

## 5. Presupuestos

Módulo para la gestión de presupuestos mensuales.

### 5.1. Definir/Actualizar Presupuesto

Define o actualiza el presupuesto mensual para un usuario.

*   **URL:** `/api/presupuestos`
*   **Método:** `POST`
*   **Descripción:** Establece o modifica el monto total del presupuesto para un usuario.
*   **Request Body:** `PresupuestoRequest`

    ```json
    {
      "usuarioId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
      "montoTotal": 1000.00
    }
    ```
*   **Response (200 OK):** `Presupuesto` (Entidad completa del presupuesto)

    ```json
    {
      "id": "p1r2e3s4-u5p6-7890-1234-567890abcdef",
      "usuarioId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
      "montoTotal": 1000.00,
      "fechaCreacion": "2023-10-27T10:00:00Z"
    }
    ```
*   **Response (400 Bad Request):** Datos de entrada inválidos.
*   **Response (404 Not Found):** Usuario no encontrado.

### 5.2. Obtener Presupuesto por Usuario

Obtiene el presupuesto actual de un usuario.

*   **URL:** `/api/presupuestos/{usuarioId}`
*   **Método:** `GET`
*   **Descripción:** Recupera el presupuesto activo para un usuario específico.
*   **Path Variables:**

| Nombre      | Tipo | Descripción          |
| :---------- | :--- | :------------------- |
| `usuarioId` | UUID | ID del usuario       |

*   **Response (200 OK):** `Presupuesto`

    ```json
    {
      "id": "p1r2e3s4-u5p6-7890-1234-567890abcdef",
      "usuarioId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
      "montoTotal": 1000.00,
      "fechaCreacion": "2023-10-27T10:00:00Z"
    }
    ```
*   **Response (404 Not Found):** Presupuesto o usuario no encontrado.

### 5.3. Obtener Resumen de Presupuesto

Obtiene un resumen del presupuesto y los gastos por categoría para un usuario en un rango de fechas.

*   **URL:** `/api/presupuestos/resumen-presupuesto`
*   **Método:** `GET`
*   **Descripción:** Proporciona un desglose de cuánto queda del presupuesto y cuánto se ha gastado en cada categoría dentro de un período.
*   **Query Parameters:**

| Nombre          | Tipo             | Descripción                                                               |
| :-------------- | :--------------- | :------------------------------------------------------------------------ |
| `usuarioId`     | UUID             | ID del usuario                                                            |
| `fechaInicio`   | OffsetDateTime   | Fecha y hora de inicio del rango (formato ISO 8601, ej. `2023-10-01T00:00:00Z`) |
| `fechaFin`      | OffsetDateTime   | Fecha y hora de fin del rango (formato ISO 8601, ej. `2023-10-31T23:59:59Z`)   |

*   **Response (200 OK):** `ResumenPresupuestoDTO`

    ```json
    {
      "presupuestoRestante": 250.50,
      "categorias": [
        {
          "categoriaId": "d1e2f3a4-b5c6-7890-1234-567890abcdef",
          "nombreCategoria": "Comida",
          "totalGastado": 300.00,
          "ultimasTres": [
            {
              "id": "t1r2a3n4-s5a6-7890-1234-567890abcdef",
              "monto": 50.00,
              "tipo": "GASTO",
              "descripcion": "Almuerzo",
              "fecha": "2023-10-26T13:00:00Z",
              "categoriaId": "d1e2f3a4-b5c6-7890-1234-567890abcdef",
              "nombreCuentaOrigen": "Cuenta Principal",
              "nombreCuentaDestino": null,
              "notas": "Restaurante"
            },
            {
              "id": "t2r3a4n5-s6a7-8901-2345-67890abcdef0",
              "monto": 25.00,
              "tipo": "GASTO",
              "descripcion": "Café",
              "fecha": "2023-10-26T09:30:00Z",
              "categoriaId": "d1e2f3a4-b5c6-7890-1234-567890abcdef",
              "nombreCuentaOrigen": "Cuenta Principal",
              "nombreCuentaDestino": null,
              "notas": null
            }
          ]
        },
        {
          "categoriaId": "f3a4b5c6-d7e8-9012-3456-7890abcdef12",
          "nombreCategoria": "Transporte Personal",
          "totalGastado": 150.00,
          "ultimasTres": []
        }
      ]
    }
    ```
*   **Response (400 Bad Request):** Parámetros de fecha inválidos o `usuarioId` no existe.

---

## 6. Transacciones

Módulo para la gestión de ingresos, gastos y transferencias entre cuentas.

### 6.1. Registrar Gasto

Registra un nuevo gasto y actualiza el saldo de la cuenta.

*   **URL:** `/api/transacciones/gasto`
*   **Método:** `POST`
*   **Descripción:** Crea una nueva transacción de tipo "GASTO" y deduce el monto del saldo de la cuenta especificada.
*   **Request Body:** `GastoIngresoRequest`

    ```json
    {
      "cuentaId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
      "monto": 75.50,
      "categoriaId": "d1e2f3a4-b5c6-7890-1234-567890abcdef",
      "descripcion": "Cena con amigos",
      "fecha": "2023-10-27T20:00:00Z",
      "notas": "Restaurante italiano"
    }
    ```
*   **Response (200 OK):** `String`

    ```
    Gasto registrado y saldo actualizado
    ```
*   **Response (400 Bad Request):** Datos de entrada inválidos (ej. `cuentaId` o `categoriaId` no existen, monto negativo).
*   **Response (404 Not Found):** Cuenta o categoría no encontrada.

### 6.2. Registrar Ingreso

Registra un nuevo ingreso y actualiza el saldo de la cuenta.

*   **URL:** `/api/transacciones/ingreso`
*   **Método:** `POST`
*   **Descripción:** Crea una nueva transacción de tipo "INGRESO" y añade el monto al saldo de la cuenta especificada.
*   **Request Body:** `GastoIngresoRequest`

    ```json
    {
      "cuentaId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
      "monto": 1200.00,
      "categoriaId": "e2f3a4b5-c6d7-8901-2345-67890abcdef0",
      "descripcion": "Salario mensual",
      "fecha": "2023-10-25T09:00:00Z",
      "notas": "Nómina Octubre"
    }
    ```
*   **Response (200 OK):** `String`

    ```
    Ingreso registrado y saldo actualizado
    ```
*   **Response (400 Bad Request):** Datos de entrada inválidos (ej. `cuentaId` o `categoriaId` no existen, monto negativo).
*   **Response (404 Not Found):** Cuenta o categoría no encontrada.

### 6.3. Registrar Transferencia

Registra una transferencia de dinero entre dos cuentas.

*   **URL:** `/api/transacciones/transferencia`
*   **Método:** `POST`
*   **Descripción:** Mueve un monto específico de una cuenta de origen a una cuenta de destino, actualizando ambos saldos.
*   **Request Body:** `TransferenciaRequest`

    ```json
    {
      "cuentaOrigenId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
      "cuentaDestinoId": "f0e9d8c7-b6a5-4321-fedc-ba9876543210",
      "monto": 200.00,
      "descripcion": "Transferencia a ahorro",
      "fecha": "2023-10-27T15:30:00Z",
      "notas": null
    }
    ```
*   **Response (200 OK):** `String`

    ```
    Transferencia realizada con éxito
    ```
*   **Response (400 Bad Request):** Datos de entrada inválidos (ej. `cuentaOrigenId` o `cuentaDestinoId` no existen, monto negativo, cuentas iguales).
*   **Response (404 Not Found):** Cuenta de origen o destino no encontrada.

### 6.4. Eliminar Transacción

Elimina una transacción y revierte los saldos de las cuentas afectadas.

*   **URL:** `/api/transacciones/{id}`
*   **Método:** `DELETE`
*   **Descripción:** Elimina una transacción existente y ajusta los saldos de las cuentas involucradas para revertir el efecto de la transacción.
*   **Path Variables:**

| Nombre | Tipo | Descripción             |
| :----- | :--- | :---------------------- |
| `id`   | UUID | ID de la transacción    |

*   **Response (200 OK):** `String`

    ```
    Transacción eliminada y saldos revertidos
    ```
*   **Response (404 Not Found):** Transacción no encontrada.

### 6.5. Obtener Transacciones Filtradas

Obtiene una lista de transacciones filtradas por usuario, tipo, rango de fechas, límite y cuenta.

*   **URL:** `/api/transacciones/filtradas`
*   **Método:** `GET`
*   **Descripción:** Permite buscar transacciones con varios criterios de filtrado para obtener un historial detallado.
*   **Query Parameters:**

| Nombre          | Tipo             | Descripción                                                               |
| :-------------- | :--------------- | :------------------------------------------------------------------------ |
| `usuarioId`     | UUID             | ID del usuario (obligatorio)                                              |
| `tipo`          | String           | Tipo de transacción ("INGRESO", "GASTO", "TRANSFERENCIA") (opcional)      |
| `fechaInicio`   | OffsetDateTime   | Fecha y hora de inicio del rango (formato ISO 8601, ej. `2023-10-01T00:00:00Z`) |
| `fechaFin`      | OffsetDateTime   | Fecha y hora de fin del rango (formato ISO 8601, ej. `2023-10-31T23:59:59Z`)   |
| `limite`        | Int              | Número máximo de transacciones a devolver (opcional)                      |
| `cuentaId`      | UUID             | ID de la cuenta a filtrar (opcional)                                      |

*   **Response (200 OK):** `List<TransaccionResponse>`

    ```json
    [
      {
        "id": "t1r2a3n4-s5a6-7890-1234-567890abcdef",
        "monto": 50.00,
        "tipo": "GASTO",
        "descripcion": "Almuerzo",
        "fecha": "2023-10-26T13:00:00Z",
        "categoriaId": "d1e2f3a4-b5c6-7890-1234-567890abcdef",
        "nombreCuentaOrigen": "Cuenta Principal",
        "nombreCuentaDestino": null,
        "notas": "Restaurante"
      },
      {
        "id": "t2r3a4n5-s6a7-8901-2345-67890abcdef0",
        "monto": 100.00,
        "tipo": "INGRESO",
        "descripcion": "Venta de artículo",
        "fecha": "2023-10-25T10:00:00Z",
        "categoriaId": "e2f3a4b5-c6d7-8901-2345-67890abcdef0",
        "nombreCuentaOrigen": null,
        "nombreCuentaDestino": "Cuenta Principal",
        "notas": "Artículo de segunda mano"
      }
    ]
    ```
*   **Response (400 Bad Request):** Parámetros de fecha o `usuarioId` inválidos.

### 6.6. Obtener Flujo de Caja

Calcula el flujo de caja (ingresos - gastos) para un usuario en un rango de fechas.

*   **URL:** `/api/transacciones/flujo-caja`
*   **Método:** `GET`
*   **Descripción:** Proporciona el balance neto de ingresos y gastos para un usuario dentro de un período específico.
*   **Query Parameters:**

| Nombre          | Tipo             | Descripción                                                               |
| :-------------- | :--------------- | :------------------------------------------------------------------------ |
| `usuarioId`     | UUID             | ID del usuario                                                            |
| `fechaInicio`   | OffsetDateTime   | Fecha y hora de inicio del rango (formato ISO 8601, ej. `2023-10-01T00:00:00Z`) |
| `fechaFin`      | OffsetDateTime   | Fecha y hora de fin del rango (formato ISO 8601, ej. `2023-10-31T23:59:59Z`)   |

*   **Response (200 OK):** `BigDecimal`

    ```json
    500.25
    ```
*   **Response (400 Bad Request):** Parámetros de fecha o `usuarioId` inválidos.

### 6.7. Obtener Total por Rango

Calcula el total de ingresos o gastos para un usuario en un rango de fechas.

*   **URL:** `/api/transacciones/total-por-rango`
*   **Método:** `GET`
*   **Descripción:** Suma todos los ingresos o todos los gastos de un usuario dentro de un rango de fechas.
*   **Query Parameters:**

| Nombre          | Tipo             | Descripción                                                               |
| :-------------- | :--------------- | :------------------------------------------------------------------------ |
| `usuarioId`     | UUID             | ID del usuario                                                            |
| `tipo`          | String           | Tipo de transacción a sumar ("INGRESO" o "GASTO")                         |
| `fechaInicio`   | OffsetDateTime   | Fecha y hora de inicio del rango (formato ISO 8601, ej. `2023-10-01T00:00:00Z`) |
| `fechaFin`      | OffsetDateTime   | Fecha y hora de fin del rango (formato ISO 8601, ej. `2023-10-31T23:59:59Z`)   |

*   **Response (200 OK):** `BigDecimal`

    ```json
    1500.00
    ```
*   **Response (400 Bad Request):** Parámetros de fecha, `usuarioId` o `tipo` inválidos.  