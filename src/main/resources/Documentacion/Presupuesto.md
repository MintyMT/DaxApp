# Módulo de Presupuestos

## Visión General
El módulo de Presupuestos permite a los usuarios establecer y gestionar límites de gasto para un período determinado. Facilita el seguimiento del consumo y proporciona un resumen detallado de los gastos por categoría, ayudando a mantener el control financiero.

## Modelo de Datos (Entity)

### `Presupuesto`
Representa el presupuesto global mensual definido por un usuario.

| Campo | Tipo | Restricciones JPA | Descripción |
| :--- | :--- | :--- | :--- |
| `id` | UUID | `@Id`, `@GeneratedValue(strategy = GenerationType.AUTO)` | Identificador único del presupuesto. |
| `montoTotal` | BigDecimal | `nullable = false`, `precision = 12, scale = 2` | Monto total asignado al presupuesto para el período. Es mutable (`var`). |
| `usuarioId` | UUID | `nullable = false`, `unique = true` | Identificador del usuario propietario del presupuesto. `unique = true` asegura que un usuario solo tenga un presupuesto global activo. |

## Lógica de Negocio (Service)

### `PresupuestoService`
Contiene la lógica para definir, obtener y resumir el estado del presupuesto.

#### `definirPresupuesto(usuarioId: UUID, monto: BigDecimal)`
*   **Propósito:** Establece o actualiza el presupuesto global para un usuario.
*   **Regla:** Si el usuario ya tiene un presupuesto (`findByUsuarioId`), se actualiza el `montoTotal` del presupuesto existente. Si no existe, se crea un nuevo registro de `Presupuesto`.
*   **Consideración:** Actualmente, el presupuesto es global y no está asociado a categorías específicas.

#### `obtenerPresupuestoPorUsuario(usuarioId: UUID)`
*   **Propósito:** Recupera el presupuesto global definido para un usuario.
*   **Regla:** Devuelve el objeto `Presupuesto` si existe, o `null` si el usuario no ha definido un presupuesto.

#### `obtenerResumenGastosPorCategoria(usuarioId: UUID, fechaInicio: OffsetDateTime, fechaFin: OffsetDateTime)`
*   **Propósito:** Genera un resumen detallado de los gastos de un usuario, agrupados por categoría, dentro de un rango de fechas.
*   **Reglas:**
    1.  Obtiene todos los gastos del usuario para el período especificado utilizando `TransaccionService.obtenerTransaccionesFiltradas`.
    2.  Calcula el `presupuestoRestante` restando el `totalGastadoMes` del `montoTotal` del presupuesto global del usuario.
    3.  Agrupa los gastos por `categoriaId`.
    4.  Para cada grupo de categoría, calcula el `totalGastado` en esa categoría y obtiene las `ultimasTres` transacciones (mapeadas a `TransaccionResponse`).
    5.  Mapea los resultados a una lista de `CategoriaResumenDTO`.
    6.  Devuelve un `ResumenPresupuestoDTO` que incluye el `presupuestoRestante` y la lista de `CategoriaResumenDTO`.
*   **Consideración:** Este método es clave para la visualización del progreso del presupuesto en el frontend.

## Contratos (DTOs)

### `PresupuestoRequest`
Utilizado para definir o actualizar el presupuesto global.
| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `usuarioId` | UUID | ID del usuario propietario del presupuesto. |
| `montoTotal` | BigDecimal | Monto total del presupuesto. |

### `ResumenPresupuestoDTO`
Representa el resumen del estado del presupuesto y los gastos por categoría.
| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `presupuestoRestante` | BigDecimal | Monto restante del presupuesto global. |
| `categorias` | List<`CategoriaResumenDTO`> | Lista de resúmenes de gastos por categoría. |

### `CategoriaResumenDTO`
Detalle de gastos para una categoría específica dentro del resumen del presupuesto.
| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `categoriaId` | UUID? | ID de la categoría. |
| `nombreCategoria` | String | Nombre de la categoría. |
| `totalGastado` | BigDecimal | Monto total gastado en esta categoría. |
| `ultimasTres` | List<`TransaccionResponse`> | Las últimas tres transacciones de esta categoría. |

## Referencia de API (Controller)

### `PresupuestoController`
Expone los endpoints para la gestión de presupuestos.

### 1. Definir Presupuesto Global
*   **Método:** `POST`
*   **URL:** `/api/presupuestos`
*   **Descripción:** Establece o actualiza el presupuesto mensual para un usuario.
*   **Request Body:** `PresupuestoRequest`
*   **Respuesta Exitosa:** `200 OK` (Devuelve el objeto `Presupuesto` creado/actualizado)
```json
{
  "id": "uuid-presupuesto",
  "montoTotal": 1000.00,
  "usuarioId": "uuid-del-usuario"
}
```

### 2. Obtener Presupuesto Global
*   **Método:** `GET`
*   **URL:** `/api/presupuestos/{usuarioId}`
*   **Descripción:** Recupera el presupuesto global definido para un usuario.
*   **Parámetros de Ruta:** `usuarioId` (UUID) - ID del usuario.
*   **Respuesta Exitosa:** `200 OK` (Devuelve el objeto `Presupuesto`)
```json
{
  "id": "uuid-presupuesto",
  "montoTotal": 1000.00,
  "usuarioId": "uuid-del-usuario"
}
```
*   **Posibles Errores:** `404 Not Found` si no existe un presupuesto para el usuario.

### 3. Obtener Resumen de Gastos por Categoría
*   **Método:** `GET`
*   **URL:** `/api/presupuestos/resumen-presupuesto`
*   **Descripción:** Proporciona un resumen detallado de los gastos por categoría dentro de un período, incluyendo el presupuesto restante.
*   **Parámetros de Consulta (Query Params):**
    *   `usuarioId` (UUID): ID del usuario.
    *   `fechaInicio` (OffsetDateTime): Fecha de inicio del período (ISO 8601).
    *   `fechaFin` (OffsetDateTime): Fecha de fin del período (ISO 8601).
*   **Respuesta Exitosa:** `200 OK` (Devuelve un `ResumenPresupuestoDTO`)
```json
{
  "presupuestoRestante": 750.00,
  "categorias": [
    {
      "categoriaId": "uuid-categoria-comida",
      "nombreCategoria": "Comida",
      "totalGastado": 150.00,
      "ultimasTres": [
        {
          "id": "uuid-transaccion-1",
          "monto": 50.00,
          "tipo": "GASTO",
          "descripcion": "Almuerzo",
          "fecha": "2024-05-20T13:00:00Z",
          "categoriaId": "uuid-categoria-comida",
          "nombreCuentaOrigen": "Efectivo",
          "nombreCuentaDestino": null,
          "notas": null
        }
      ]
    }
  ]
}
```