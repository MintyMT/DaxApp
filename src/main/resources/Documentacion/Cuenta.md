# Módulo de Cuentas

## Visión General
El módulo de Cuentas gestiona los activos financieros del usuario (efectivo, cuentas bancarias, ahorros, etc.). Implementa un sistema de **borrado lógico** para preservar el historial de transacciones y saldos, permitiendo la desactivación y reactivación de cuentas sin eliminarlas físicamente.

## Modelo de Persistencia (Entity)

### `Cuenta`
Representa una cuenta financiera del usuario.

| Campo | Tipo | Restricciones JPA / Jakarta | Descripción |
| :--- | :--- | :--- | :--- |
| `id` | UUID | `@Id`, `@GeneratedValue(strategy = GenerationType.AUTO)` | Identificador único de la cuenta. |
| `usuarioId` | UUID | `@Column(name = "usuario_id")` | Identificador del usuario propietario de la cuenta. |
| `nombre` | String | `@NotBlank`, `nullable = false` | Nombre descriptivo de la cuenta (ej. "Efectivo", "Banco Pichincha"). |
| `saldo` | BigDecimal | `@NotNull`, `@Column(precision = 19, scale = 2)` | Saldo actual de la cuenta. Es mutable (`var`) para permitir actualizaciones directas. |
| `tipo` | TipoCuenta | `@ManyToOne`, `@JoinColumn(name = "tipo_id")` | Tipo de cuenta (ej. "Efectivo", "Banco"). |
| `activa` | Boolean | `@Column(name = "activa", nullable = false)` | Indica si la cuenta está activa (`true`) o lógicamente borrada (`false`). Es mutable (`var`). |

## Lógica y Reglas de Negocio (Service)

### `CuentaService`
Contiene la lógica central para la gestión de cuentas.

#### `calcularBalanceGlobal(usuarioId: UUID)`
*   Calcula la suma total del `saldo` de todas las cuentas **activas** (`activa = true`) de un usuario.

#### `obtenerCuentasUsuario(usuarioId: UUID)`
*   Devuelve una lista de todas las cuentas **activas** (`activa = true`) que pertenecen a un usuario específico.

#### `agregarCuenta(usuarioId: UUID, nombre: String, saldoInicial: BigDecimal, tipoId: UUID)`
*   Crea una nueva cuenta para el usuario especificado.
*   La cuenta se inicializa con el `saldoInicial` proporcionado y se establece como `activa = true` por defecto.
*   Requiere un `tipoId` válido para asociar la cuenta a un `TipoCuenta` existente.

#### `desactivarCuenta(cuentaId: UUID)`
*   **Borrado Lógico:** Cambia el estado de la cuenta especificada a `activa = false`.
*   La cuenta no se elimina físicamente de la base de datos, preservando su historial de transacciones.
*   Una cuenta desactivada no aparecerá en los listados de cuentas activas ni permitirá nuevas operaciones.
*   Lanza `RuntimeException` si la cuenta no existe o ya está inactiva.

#### `eliminarCuentasPorUsuario(usuarioId: UUID)`
*   **Borrado Físico:** Elimina permanentemente todas las cuentas asociadas a un usuario de la base de datos.
*   Este método es invocado exclusivamente durante el proceso de eliminación de cuenta de usuario (`eliminarTodoLoRelacionado` en `UsuarioService`).
*   Se ejecuta después de eliminar las transacciones y antes de eliminar las categorías/presupuestos para mantener la integridad referencial.

## Contratos de Intercambio (DTOs)

### `NuevaCuentaRequest`
Utilizado para crear una nueva cuenta.
| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `usuarioId` | UUID | ID del usuario propietario. |
| `nombre` | String | Nombre de la cuenta. |
| `saldoInicial` | BigDecimal | Saldo con el que se inicializa la cuenta. |
| `tipoId` | UUID | ID del tipo de cuenta (ej. Efectivo, Banco). |

### `CuentaResponse`
Representa la información de una cuenta devuelta al cliente.
| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `id` | UUID | ID único de la cuenta. |
| `nombre` | String | Nombre de la cuenta. |
| `saldo` | BigDecimal | Saldo actual de la cuenta. |
| `tipoNombre` | String | Nombre del tipo de cuenta (ej. "Efectivo"). |
| `activa` | Boolean | `true` si la cuenta está activa, `false` si está desactivada. |

### `BalanceGlobalResponse`
Representa el balance total de las cuentas de un usuario.
| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `balanceTotal` | BigDecimal | Suma de los saldos de todas las cuentas activas del usuario. |

## Referencia de API (Controller)

### 1. Crear Nueva Cuenta
*   **Método:** `POST`
*   **URL:** `/api/cuentas`
*   **Descripción:** Registra una nueva cuenta para un usuario.
*   **Respuesta Exitosa:** `201 Created`
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-1234567890ef",
  "nombre": "Banco Pichincha",
  "saldo": 1500.00,
  "tipoNombre": "Banco",
  "activa": true
}
```

### 2. Listar Cuentas por Usuario
*   **Método:** `GET`
*   **URL:** `/api/cuentas/usuario/{usuarioId}`
*   **Descripción:** Obtiene todas las cuentas **activas** de un usuario.
*   **Respuesta Exitosa:** `200 OK`
```json
[
  {
    "id": "uuid-1",
    "nombre": "Efectivo",
    "saldo": 50.75,
    "tipoNombre": "Efectivo",
    "activa": true
  },
  {
    "id": "uuid-2",
    "nombre": "Banco Pichincha",
    "saldo": 1500.00,
    "tipoNombre": "Banco",
    "activa": true
  }
]
```

### 3. Obtener Balance Global
*   **Método:** `GET`
*   **URL:** `/api/cuentas/usuario/{usuarioId}/balance`
*   **Descripción:** Calcula y devuelve el balance total de las cuentas **activas** de un usuario.
*   **Respuesta Exitosa:** `200 OK`
```json
{
  "balanceTotal": 1550.75
}
```

### 4. Desactivar Cuenta (Borrado Lógico)
*   **Método:** `DELETE`
*   **URL:** `/api/cuentas/{id}`
*   **Descripción:** Desactiva una cuenta, marcándola como inactiva.
*   **Parámetros de Ruta:** `id` (UUID) - ID de la cuenta a desactivar.
*   **Respuesta Exitosa:** `200 OK`
```json
{
  "id": "uuid-cuenta-desactivada",
  "nombre": "Cuenta Inactiva",
  "saldo": 0.00,
  "tipoNombre": "Banco",
  "activa": false
}
```
