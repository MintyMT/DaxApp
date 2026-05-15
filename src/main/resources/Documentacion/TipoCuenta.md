# Módulo de Tipos de Cuenta

## Visión General
El módulo de Tipos de Cuenta proporciona un catálogo maestro de las clasificaciones predefinidas para las cuentas financieras (ej. "Efectivo", "Banco", "Tarjeta de Crédito"). Este módulo es de **solo lectura** para el frontend y su propósito principal es alimentar los selectores y listas desplegables en la interfaz de usuario al momento de crear o visualizar cuentas.

## Modelo de Datos (Entity)

### `TipoCuenta`
Representa una clasificación predefinida para las cuentas de los usuarios.

| Campo | Tipo | Restricciones JPA | Descripción |
| :--- | :--- | :--- | :--- |
| `id` | UUID | `@Id`, `@GeneratedValue` | Identificador único del tipo de cuenta. |
| `nombre` | String | `nullable = false` (implícito por Kotlin y JPA) | Nombre descriptivo del tipo de cuenta (ej. "Efectivo"). |

## Lógica de Negocio (Service)

### `TipoCuentaService`
Contiene la lógica para la recuperación y gestión de los tipos de cuenta.

#### `obtenerTodos()`
*   **Propósito:** Recupera una lista completa de todos los tipos de cuenta disponibles en el sistema.
*   **Regla:** Este método es fundamental para que el frontend pueda poblar los selectores cuando un usuario desea crear una nueva cuenta, ofreciendo opciones estandarizadas.

#### `obtenerPorId(id: UUID)`
*   **Propósito:** Busca y devuelve un `TipoCuenta` específico por su identificador único.
*   **Regla:** Útil para obtener detalles de un tipo de cuenta o para validaciones internas. Devuelve `null` si no se encuentra el tipo de cuenta.

#### `guardar(tipoCuenta: TipoCuenta)`
*   **Propósito:** Persiste un nuevo tipo de cuenta o actualiza uno existente.
*   **Regla:** Este método es principalmente para uso interno del sistema o para un módulo de administración, no se expone directamente a los usuarios finales a través de la API pública.

## Contratos (DTOs)

### `TipoCuentaResponse`
Representa la información de un tipo de cuenta devuelta al cliente.

| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `id` | UUID | ID único del tipo de cuenta. |
| `nombre` | String | Nombre del tipo de cuenta. |

**Ejemplo de JSON de Respuesta:**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-1234567890ef",
  "nombre": "Banco"
}
```

## Referencia de API (Controller)

### `TipoCuentaController`
Expone los endpoints para consultar los tipos de cuenta.

### 1. Listar Todos los Tipos de Cuenta
*   **Método:** `GET`
*   **URL:** `/api/tipos-cuenta`
*   **Descripción:** Obtiene una lista de todos los tipos de cuenta predefinidos por el sistema.
*   **Respuesta Exitosa:** `200 OK`
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
    "nombre": "Tarjeta de Crédito"
  }
]
```

### 2. Obtener Tipo de Cuenta por ID
*   **Método:** `GET`
*   **URL:** `/api/tipos-cuenta/{id}`
*   **Descripción:** Recupera los detalles de un tipo de cuenta específico por su ID.
*   **Parámetros de Ruta:** `id` (UUID) - ID del tipo de cuenta.
*   **Respuesta Exitosa:** `200 OK`
```json
{
  "id": "uuid-tipo-solicitado",
  "nombre": "Efectivo"
}
```
*   **Posibles Errores:** `404 Not Found` si el ID no corresponde a ningún tipo de cuenta.
