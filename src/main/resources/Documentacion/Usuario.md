# Módulo de Usuarios

## Visión General
El módulo de Usuarios gestiona la identidad, autenticación y el ciclo de vida de los usuarios dentro de DaxApp. Incluye funcionalidades para el registro, inicio de sesión y un sistema de borrado de cuenta con un período de gracia.

## Modelo de Persistencia (Entity)

### `Usuario`
Representa a un usuario registrado en el sistema.

| Campo | Tipo | Restricciones JPA | Descripción |
| :--- | :--- | :--- | :--- |
| `id` | UUID | `@Id`, `@GeneratedValue(strategy = GenerationType.AUTO)` | Identificador único del usuario. |
| `nombre` | String | `nullable = false`, `length = 100` | Nombre completo del usuario. |
| `email` | String | `nullable = false`, `unique = true`, `length = 150` | Correo electrónico del usuario, utilizado como identificador único. |
| `password` | String | `nullable = false`, `columnDefinition = "TEXT"` | Contraseña del usuario (almacenada de forma segura, idealmente hasheada). |
| `fechaCreacion` | OffsetDateTime | `nullable = false` | Fecha y hora de registro del usuario. |
| `fechaSolicitudBorrado` | OffsetDateTime | `nullable = true` | Marca de tiempo que indica cuándo se solicitó el borrado de la cuenta. `null` si no hay una solicitud pendiente. |

## Lógica y Reglas de Negocio (Service)

### `UsuarioService`
Contiene la lógica central para la gestión de usuarios.

#### Registro de Usuario (`registrarUsuario`)
*   Verifica que el correo electrónico no esté ya registrado.
*   Crea una nueva instancia de `Usuario` y la persiste.
*   **Consideración:** La contraseña se guarda en texto plano actualmente; en un entorno de producción, debería aplicarse un algoritmo de hashing robusto.

#### Inicio de Sesión (`iniciarSesion`)
*   Valida las credenciales (email y contraseña).
*   **Lógica Especial de Borrado:** Si el usuario tenía una `fechaSolicitudBorrado` activa, un inicio de sesión exitoso **cancela automáticamente el proceso de borrado**, estableciendo `fechaSolicitudBorrado` a `null` y guardando el cambio.

#### Solicitud de Borrado de Cuenta (`solicitarBorradoDeCuenta`)
*   Marca la cuenta del usuario con la fecha y hora actual en el campo `fechaSolicitudBorrado`.
*   **Flujo de Borrado Programado:** Esta acción inicia un "cronómetro" de 3 minutos. Si el usuario no inicia sesión dentro de este período, su cuenta será eliminada permanentemente por una tarea programada.

#### Ejecución de Limpieza de Cuentas (`ejecutarLimpiezaDeCuentas`)
*   Método anotado con `@Scheduled(fixedDelay = 60000)`, lo que significa que se ejecuta cada 60 segundos.
*   Busca usuarios cuya `fechaSolicitudBorrado` sea anterior a 3 minutos desde el momento actual.
*   Para cada usuario encontrado, invoca `eliminarTodoLoRelacionado`.

#### Eliminación en Cascada (`eliminarTodoLoRelacionado`)
*   **Orden Crítico:** Este método realiza la eliminación física de todos los datos asociados a un usuario en un orden específico para respetar las llaves foráneas en PostgreSQL:
    1.  Elimina todas las `Transacciones` del usuario.
    2.  Elimina todas las `Cuentas` del usuario.
    3.  Elimina todas las `Categorías` personalizadas del usuario.
    4.  Elimina todos los `Presupuestos` del usuario.
    5.  Finalmente, elimina el registro del `Usuario` mismo.
*   **Propósito:** Asegura la integridad referencial y la limpieza completa de los datos del usuario.

## Contratos de Intercambio (DTOs)

### `RegistroRequest`
| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `nombre` | String | Nombre completo del usuario. |
| `email` | String | Correo electrónico. |
| `contra` | String | Contraseña. |

### `LoginRequest`
| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `email` | String | Correo electrónico. |
| `contra` | String | Contraseña. |

### `UsuarioResponse`
| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `id` | UUID | Identificador único del usuario. |
| `nombre` | String | Nombre del usuario. |
| `email` | String | Correo electrónico del usuario. |
| `fechaSolicitudBorrado` | OffsetDateTime? | Fecha y hora de la solicitud de borrado, `null` si no hay solicitud. |

## Referencia de API (Controller)

### 1. Registrar Nuevo Usuario
*   **Método:** `POST`
*   **URL:** `/api/usuarios/registro`
*   **Descripción:** Crea una nueva cuenta de usuario.
*   **Respuesta Exitosa:** `201 Created`
```json
{
  "id": "5f8a2097-6dd3-4987-9668-8163dc11ecc8",
  "nombre": "Axel",
  "email": "axel@example.com",
  "fechaSolicitudBorrado": null
}
```

### 2. Iniciar Sesión (Login)
*   **Método:** `POST`
*   **URL:** `/api/usuarios/login`
*   **Descripción:** Valida las credenciales y devuelve los datos del usuario.
*   **Respuesta Exitosa:** `200 OK`
```json
{
  "id": "5f8a2097-6dd3-4987-9668-8163dc11ecc8",
  "nombre": "Axel",
  "email": "axel@example.com",
  "fechaSolicitudBorrado": null
}
```

### 3. Solicitar Borrado de Cuenta
*   **Método:** `POST`
*   **URL:** `/api/usuarios/{id}/solicitar-borrado`
*   **Descripción:** Inicia el proceso de borrado diferido para el usuario especificado.
*   **Respuesta Exitosa:** `200 OK`
```text
"Proceso de borrado iniciado. Tienes 3 minutos para arrepentirte e iniciar sesión."
```