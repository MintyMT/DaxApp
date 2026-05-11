# 📑 Documentación Técnica API - DaxApp

Bienvenido a la especificación técnica de la API de **DaxApp**. Este documento detalla los endpoints disponibles, los formatos de intercambio de datos y las reglas de negocio que rigen el sistema.

---

## 🔐 Módulo: Usuarios
Este módulo gestiona la identidad, el acceso y el ciclo de vida de las cuentas de usuario, incluyendo el proceso de seguridad de borrado diferido.

### 1. Registrar Nuevo Usuario
Permite dar de alta a un nuevo integrante en el sistema DaxApp.
*   **Método:** `POST`
*   **URL:** `/api/usuarios/registro`

**Cuerpo de la Petición (Request Body):**
```json
{
  "nombre": "Axel",
  "email": "axel@example.com",
  "contra": "Dax2024*"
}
```
| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| **nombre** | String | Nombre completo del usuario. |
| **email** | String | Correo electrónico único (usado como identidad). |
| **contra** | String | Contraseña de acceso (será procesada de forma segura). |

**Respuesta Exitosa (Success Response):**
*   **Código:** `201 Created`
```json
{
  "id": "5f8a2097-6dd3-4987-9668-8163dc11ecc8",
  "nombre": "Axel",
  "email": "axel@example.com",
  "fechaSolicitudBorrado": null
}
```

---

### 2. Iniciar Sesión (Login)
Valida las credenciales del usuario y otorga acceso al sistema.
*   **Método:** `POST`
*   **URL:** `/api/usuarios/login`

**Cuerpo de la Petición (Request Body):**
```json
{
  "email": "axel@example.com",
  "contra": "Dax2024*"
}
```

**Respuesta Exitosa (Success Response):**
*   **Código:** `200 OK`
```json
{
  "id": "5f8a2097-6dd3-4987-9668-8163dc11ecc8",
  "nombre": "Axel",
  "email": "axel@example.com",
  "fechaSolicitudBorrado": null
}
```
> **Lógica Especial:** Si el usuario tiene una solicitud de borrado activa, realizar un **Login exitoso cancela automáticamente el proceso**, estableciendo el campo `fechaSolicitudBorrado` nuevamente en `null`.

---

### 3. Solicitar Borrado de Cuenta
Inicia el protocolo de eliminación de cuenta con un periodo de gracia.
*   **Método:** `POST`
*   **URL:** `/api/usuarios/{id}/solicitar-borrado`

**Parámetros de Ruta:**
*   `id` (UUID): El identificador único del usuario que desea darse de baja.

**Respuesta Exitosa (Success Response):**
*   **Código:** `200 OK`
```text
"Proceso de borrado iniciado. Tienes 3 minutos para arrepentirte e iniciar sesión."
```

---

## 🏦 Módulo: Cuentas
Gestión de billeteras, bancos y activos financieros de los usuarios.

### 1. Crear Nueva Cuenta
Permite al usuario registrar una nueva fuente de dinero (Efectivo, Banco, Ahorros, etc.).
*   **Método:** `POST`
*   **URL:** `/api/cuentas`

**Cuerpo de la Petición (Request Body):**
```json
{
  "usuarioId": "5f8a2097-6dd3-4987-9668-8163dc11ecc8",
  "nombre": "Banco Pichincha",
  "saldoInicial": 1500.00,
  "tipoId": "d7e123-f456-7890-abcd-1234567890ef"
}
```

**Respuesta Exitosa (Success Response):**
*   **Código:** `201 Created`
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-1234567890ef",
  "nombre": "Banco Pichincha",
  "saldo": 1500.00,
  "tipoNombre": "Banco"
}
```

---

### 2. Listar Cuentas por Usuario
Obtiene todas las cuentas registradas de un usuario específico.
*   **Método:** `GET`
*   **URL:** `/api/cuentas/usuario/{usuarioId}`

**Parámetros de Ruta:**
*   `usuarioId` (UUID): Identificador del dueño de las cuentas.

**Respuesta Exitosa (Success Response):**
*   **Código:** `200 OK`
```json
[
  {
    "id": "uuid-1",
    "nombre": "Efectivo",
    "saldo": 50.75,
    "tipoNombre": "Efectivo"
  },
  {
    "id": "uuid-2",
    "nombre": "Banco Pichincha",
    "saldo": 1500.00,
    "tipoNombre": "Banco"
  }
]
```

---

### 3. Obtener Balance Global
Calcula la suma total del dinero disponible en todas las cuentas de un usuario.
*   **Método:** `GET`
*   **URL:** `/api/cuentas/usuario/{usuarioId}/balance`

**Respuesta Exitosa (Success Response):**
*   **Código:** `200 OK`
```json
{
  "balanceTotal": 1550.75
}
```

---

## 📋 Módulo: Tipos de Cuenta
Catálogo maestro de tipos de cuenta disponibles en el sistema.

### 1. Listar Tipos de Cuenta
Obtiene la lista completa de tipos de cuenta para alimentar selectores en el registro de nuevas cuentas.
*   **Método:** `GET`
*   **URL:** `/api/tipos-cuenta`

**Request Body:** No requiere.

**Respuesta Exitosa (Success Response):**
*   **Código:** `200 OK`
```json
[
  {
    "id": "uuid-tipo-1",
    "nombre": "Efectivo"
  },
  {
    "id": "uuid-tipo-2",
    "nombre": "Banco"
  },
  {
    "id": "uuid-tipo-3",
    "nombre": "Ahorros"
  }
]
```

---

### 2. Obtener Tipo de Cuenta por ID
Consulta la información de un tipo de cuenta específico.
*   **Método:** `GET`
*   **URL:** `/api/tipos-cuenta/{id}`

**Respuesta Exitosa (Success Response):**
*   **Código:** `200 OK`
```json
{
  "id": "uuid-tipo-solicitado",
  "nombre": "Tarjeta de Crédito"
}
```

> **Propósito del Módulo:** Este es un catálogo de **solo lectura** para el frontend. Está diseñado para alimentar la interfaz de usuario con las opciones de clasificación predefinidas por el sistema.

---

## 💸 Módulo: Transacciones
Gestión de movimientos financieros, historial y flujo de caja.

### 1. Registrar Gasto
Registra una salida de dinero. El sistema descuenta automáticamente el monto del saldo de la cuenta origen.
*   **Método:** `POST`
*   **URL:** `/api/transacciones/gasto`

**Cuerpo de la Petición (Request Body):**
```json
{
  "cuentaId": "uuid-cuenta-origen",
  "monto": 25.50,
  "categoriaId": "uuid-categoria-comida",
  "descripcion": "Cena con amigos",
  "fecha": "2024-05-20T20:30:00Z",
  "notas": "Pago con tarjeta"
}
```

**Respuesta Exitosa (Success Response):**
*   **Código:** `201 Created`
```text
"Gasto registrado y saldo actualizado"
```

---

### 2. Registrar Ingreso
Registra una entrada de dinero. El sistema suma el monto al saldo de la cuenta destino.
*   **Método:** `POST`
*   **URL:** `/api/transacciones/ingreso`

**Cuerpo de la Petición (Request Body):**
```json
{
  "cuentaId": "uuid-cuenta-destino",
  "monto": 1200.00,
  "categoriaId": "uuid-categoria-sueldo",
  "descripcion": "Pago quincena",
  "fecha": "2024-05-15T09:00:00Z",
  "notas": null
}
```

**Respuesta Exitosa (Success Response):**
*   **Código:** `201 Created`
```text
"Ingreso registrado y saldo actualizado"
```

---

### 3. Registrar Transferencia
Mueve dinero entre dos cuentas del mismo usuario. Descuenta del origen y suma al destino.
*   **Método:** `POST`
*   **URL:** `/api/transacciones/transferencia`

**Cuerpo de la Petición (Request Body):**
```json
{
  "cuentaOrigenId": "uuid-cuenta-A",
  "cuentaDestinoId": "uuid-cuenta-B",
  "monto": 100.00,
  "descripcion": "Ahorro programado",
  "fecha": "2024-05-20T10:00:00Z",
  "notas": "Traspaso mensual"
}
```

**Respuesta Exitosa (Success Response):**
*   **Código:** `201 Created`
```text
"Transferencia realizada con éxito"
```

---

### 4. Eliminar / Revertir Transacción
Borra un registro del historial y revierte automáticamente el efecto en los saldos de las cuentas involucradas.
*   **Método:** `DELETE`
*   **URL:** `/api/transacciones/{id}`

**Respuesta Exitosa (Success Response):**
*   **Código:** `200 OK`
```text
"Transacción eliminada y saldos revertidos"
```

---

### 5. Consulta Filtrada (Historial)
Obtiene una lista de movimientos basada en múltiples criterios de búsqueda.
*   **Método:** `GET`
*   **URL:** `/api/transacciones/filtradas`

**Parámetros de Consulta (Query Params):**
| Parámetro | Requerido | Descripción |
| :--- | :--- | :--- |
| **usuarioId** | Sí | UUID del dueño de las transacciones. |
| **fechaInicio** | Sí | Fecha inicial del rango (ISO 8601). |
| **fechaFin** | Sí | Fecha final del rango (ISO 8601). |
| **tipo** | No | Filtrar por "GASTO", "INGRESO" o "TRANSFERENCIA". |
| **limite** | No | Cantidad máxima de registros a devolver. |
| **cuentaId** | No | Filtrar movimientos de una cuenta específica. |

**Respuesta Exitosa (Success Response):**
*   **Código:** `200 OK`
```json
[
  {
    "id": "uuid-transaccion-1",
    "monto": 25.50,
    "tipo": "GASTO",
    "descripcion": "Cena con amigos",
    "fecha": "2024-05-20T20:30:00Z",
    "categoriaId": "uuid-categoria-id",
    "nombreCuentaOrigen": "Billetera",
    "nombreCuentaDestino": null,
    "notas": "Pago con tarjeta"
  }
]
```

---

### 6. Obtener Flujo de Caja
Calcula el neto (Ingresos - Gastos) de un periodo determinado.
*   **Método:** `GET`
*   **URL:** `/api/transacciones/flujo-caja`

**Respuesta Exitosa (Success Response):**
*   **Código:** `200 OK`
```json
1174.50
```

---

## 🏷️ Módulo: Categorías
Organización y clasificación de transacciones. Diferencia entre categorías del sistema y personalizadas.

### 1. Listar Categorías del Usuario
Obtiene todas las categorías disponibles para el usuario, incluyendo las globales predefinidas.
*   **Método:** `GET`
*   **URL:** `/api/categorias/usuario/{usuarioId}`

**Respuesta Exitosa (Success Response):**
*   **Código:** `200 OK`
```json
[
  {
    "id": "uuid-global-1",
    "nombre": "Alimentación",
    "tipo": "GASTO",
    "icono": "ic_food",
    "esGlobal": true
  },
  {
    "id": "uuid-personal-1",
    "nombre": "Gimnasio",
    "tipo": "GASTO",
    "icono": "ic_gym",
    "esGlobal": false
  }
]
```
> **Lógica Especial:** El campo `esGlobal` permite al frontend decidir si habilitar o no las opciones de edición y eliminación.

---

### 2. Crear Categoría Personalizada
Permite al usuario definir sus propias categorías de gasto o ingreso.
*   **Método:** `POST`
*   **URL:** `/api/categorias`

**Cuerpo de la Petición (Request Body):**
```json
{
  "nombre": "Gimnasio",
  "tipo": "GASTO",
  "icono": "ic_gym",
  "usuarioId": "uuid-usuario"
}
```

**Respuesta Exitosa (Success Response):**
*   **Código:** `201 Created`
```json
{
  "id": "uuid-nueva-categoria",
  "nombre": "Gimnasio",
  "tipo": "GASTO",
  "icono": "ic_gym",
  "esGlobal": false
}
```

---

### 3. Editar Categoría
Actualiza el nombre de una categoría personalizada existente.
*   **Método:** `PUT`
*   **URL:** `/api/categorias/{id}`

**Cuerpo de la Petición (Request Body):**
```json
{
  "nuevoNombre": "Crossfit",
  "usuarioId": "uuid-usuario"
}
```

**Respuesta Exitosa (Success Response):**
*   **Código:** `200 OK`
```json
{
  "id": "uuid-categoria-existente",
  "nombre": "Crossfit",
  "tipo": "GASTO",
  "icono": "ic_gym",
  "esGlobal": false
}
```

---

### 4. Eliminar Categoría Personalizada
Borra una categoría creada por el usuario y reasigna automáticamente sus transacciones.
*   **Método:** `DELETE`
*   **URL:** `/api/categorias/{id}?usuarioId={usuarioId}`

**Respuesta Exitosa (Success Response):**
*   **Código:** `200 OK`
```text
"Categoría eliminada. Las transacciones se movieron a categorías del sistema."
```
> **Lógica de Reasignación:** Al eliminar una categoría, el sistema busca todas las transacciones vinculadas y las mueve a la categoría global **"Otros Gastos"** u **"Otros Ingresos"** según corresponda, para evitar la pérdida de integridad financiera.
