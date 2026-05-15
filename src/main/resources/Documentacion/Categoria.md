# Módulo de Categorías

## Visión General
El módulo de Categorías permite a los usuarios clasificar sus transacciones financieras como gastos o ingresos. El sistema soporta tanto categorías predefinidas (globales) como categorías personalizadas creadas por cada usuario.

## Modelo de Datos (Entity)

### `Categoria`
Representa una categoría para clasificar transacciones.

| Campo | Tipo | Restricciones JPA | Descripción |
| :--- | :--- | :--- | :--- |
| `id` | UUID | `@Id`, `@GeneratedValue(strategy = GenerationType.AUTO)` | Identificador único de la categoría. |
| `nombre` | String | `nullable = false` | Nombre de la categoría (ej. "Alimentación", "Sueldo"). |
| `tipo` | String | `nullable = false` | Tipo de categoría: "GASTO" o "INGRESO". |
| `usuarioId` | UUID | `@Column(name = "usuario_id", nullable = true)` | Identificador del usuario propietario. `null` para categorías globales del sistema. |

## Lógica de Negocio (Service)

### `CategoriaService`
Contiene la lógica para la gestión de categorías, incluyendo la diferenciación entre globales y personalizadas.

#### `obtenerTodoPorUsuario(usuarioId: UUID)`
*   **Propósito:** Recupera todas las categorías disponibles para un usuario.
*   **Regla:** Combina las categorías globales (donde `usuarioId` es `null`) con las categorías personalizadas creadas por el `usuarioId` especificado.

#### `crearCategoria(nombre: String, tipo: String, usuarioId: UUID)`
*   **Propósito:** Crea una nueva categoría personalizada para un usuario.
*   **Validaciones:**
    *   Verifica que el `tipo` sea "INGRESO" o "GASTO".
    *   Asegura que el `usuarioId` proporcionado corresponda a un usuario existente.
*   **Consideración:** Las categorías globales son creadas y gestionadas internamente por el sistema, no a través de este endpoint.

#### `editarNombreCategoria(categoriaId: UUID, nuevoNombre: String, usuarioId: UUID)`
*   **Propósito:** Actualiza el nombre de una categoría personalizada.
*   **Validaciones:**
    *   Verifica la existencia de la categoría.
    *   **Seguridad:** Impide la edición de categorías globales (`usuarioId` es `null`).
    *   **Ownership:** Valida que la categoría pertenezca al `usuarioId` que realiza la solicitud.
*   **Inmutabilidad:** Utiliza el método `copiarConNuevoNombre` de la entidad `Categoria` para crear una nueva instancia con el nombre actualizado, manteniendo el `id` original para que JPA realice un `UPDATE`.

#### `eliminarCategoriaPersonalizada(categoriaId: UUID, usuarioId: UUID)`
*   **Propósito:** Elimina una categoría personalizada creada por el usuario.
*   **Validaciones:**
    *   Verifica la existencia de la categoría.
    *   **Seguridad:** Impide la eliminación de categorías globales (`usuarioId` es `null`).
    *   **Ownership:** Valida que la categoría pertenezca al `usuarioId` que realiza la solicitud.
*   **Lógica de Reasignación:** Antes de eliminar la categoría:
    1.  Identifica todas las transacciones vinculadas a la categoría que se va a borrar.
    2.  Reasigna estas transacciones a una categoría global predefinida ("Otros Gastos" u "Otros Ingresos", según el `tipo` de la categoría eliminada). Esta reasignación se realiza creando nuevas instancias de `Transaccion` con la nueva `categoriaId` y guardándolas, respetando la inmutabilidad de `Transaccion`.
    3.  Finalmente, la categoría personalizada es eliminada físicamente.
*   **Impacto:** Mantiene la integridad de los datos financieros al no dejar transacciones sin categoría, incluso después de eliminar una categoría personalizada.

## Contratos (DTOs)

### `NuevaCategoriaRequest`
Utilizado para crear una nueva categoría personalizada.
| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `nombre` | String | Nombre de la nueva categoría. |
| `tipo` | String | Tipo de categoría ("GASTO" o "INGRESO"). |
| `usuarioId` | UUID | ID del usuario propietario de la categoría. |

### `EditarCategoriaRequest`
Utilizado para actualizar el nombre de una categoría.
| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `nuevoNombre` | String | El nuevo nombre para la categoría. |
| `usuarioId` | UUID | ID del usuario propietario de la categoría. |

### `CategoriaResponse`
Representa la información de una categoría devuelta al cliente.
| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `id` | UUID | ID único de la categoría. |
| `nombre` | String | Nombre de la categoría. |
| `tipo` | String | Tipo de categoría ("GASTO" o "INGRESO"). |
| `esGlobal` | Boolean | `true` si es una categoría del sistema, `false` si es personalizada. |

**Ejemplo de JSON de Respuesta (Lista de Categorías):**
```json
[
  {
    "id": "uuid-global-1",
    "nombre": "Alimentación",
    "tipo": "GASTO",
    "esGlobal": true
  },
  {
    "id": "uuid-personal-1",
    "nombre": "Gimnasio",
    "tipo": "GASTO",
    "esGlobal": false
  }
]
```

## Referencia de API (Controller)

### `CategoriaController`
Expone los endpoints para la gestión de categorías.

### 1. Listar Categorías por Usuario
*   **Método:** `GET`
*   **URL:** `/api/categorias/usuario/{usuarioId}`
*   **Descripción:** Obtiene todas las categorías (globales y personalizadas) disponibles para un usuario.
*   **Parámetros de Ruta:** `usuarioId` (UUID) - ID del usuario.
*   **Respuesta Exitosa:** `200 OK` (Lista de `CategoriaResponse`)

### 2. Crear Categoría Personalizada
*   **Método:** `POST`
*   **URL:** `/api/categorias`
*   **Descripción:** Crea una nueva categoría personalizada para el usuario especificado.
*   **Request Body:** `NuevaCategoriaRequest`
*   **Respuesta Exitosa:** `200 OK` (Devuelve la `CategoriaResponse` de la categoría creada)

### 3. Editar Categoría
*   **Método:** `PUT`
*   **URL:** `/api/categorias/{id}`
*   **Descripción:** Actualiza el nombre de una categoría personalizada existente.
*   **Parámetros de Ruta:** `id` (UUID) - ID de la categoría a editar.
*   **Request Body:** `EditarCategoriaRequest`
*   **Respuesta Exitosa:** `200 OK` (Devuelve la `CategoriaResponse` actualizada)

### 4. Eliminar Categoría Personalizada
*   **Método:** `DELETE`
*   **URL:** `/api/categorias/{id}?usuarioId={usuarioId}`
*   **Descripción:** Elimina una categoría personalizada y reasigna sus transacciones.
*   **Parámetros de Ruta:** `id` (UUID) - ID de la categoría a eliminar.
*   **Parámetros de Consulta:** `usuarioId` (UUID) - ID del usuario propietario de la categoría.
*   **Respuesta Exitosa:** `200 OK`
```text
"Categoría eliminada. Las transacciones se movieron a categorías del sistema."
```