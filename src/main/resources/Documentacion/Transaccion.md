# Módulo de Transacciones

## Visión General
El módulo de Transacciones es el corazón del sistema financiero, encargado de registrar y gestionar todos los movimientos de dinero (gastos, ingresos y transferencias) entre las cuentas de un usuario. Asegura la consistencia contable mediante el ajuste automático de saldos y permite la consulta detallada del historial financiero.

## Modelo de Persistencia (Entity)

### `Transaccion`
Representa un movimiento de dinero registrado en el sistema.

| Campo | Tipo | Restricciones JPA | Descripción |
| :--- | :--- | :--- | :--- |
| `id` | UUID | `@Id`, `@GeneratedValue(strategy = GenerationType.AUTO)` | Identificador único de la transacción. |
| `monto` | BigDecimal | `nullable = false`, `precision = 12, scale = 2` | Cantidad de dinero involucrada en la transacción. |
| `tipo` | String | `nullable = false`, `length = 15` | Tipo de movimiento: "INGRESO", "GASTO", "TRANSFERENCIA". |
| `descripcion` | String | `columnDefinition = "TEXT"` | Descripción breve del movimiento. |
| `fecha` | OffsetDateTime | `nullable = false` | Fecha y hora en que se realizó la transacción. |
| `notas` | String | `columnDefinition = "TEXT"` | Notas adicionales sobre la transacción. |
| `cuentaOrigen` | Cuenta | `@ManyToOne(fetch = FetchType.LAZY)`, `@JoinColumn(name = "cuenta_origen_id")` | Cuenta de donde sale el dinero (para Gastos y Transferencias). `null` para Ingresos. |
| `cuentaDestino` | Cuenta | `@ManyToOne(fetch = FetchType.LAZY)`, `@JoinColumn(name = "cuenta_destino_id")` | Cuenta a donde llega el dinero (para Ingresos y Transferencias). `null` para Gastos. |
| `categoriaId` | UUID | `@Column(name = "categoria_id")` | Identificador de la categoría asociada a la transacción. |
| `usuarioId` | UUID | `@Column(name = "usuario_id")` | Identificador del usuario propietario de la transacción. |

## Lógica y Reglas de Negocio (Service)

### `TransaccionService`
Contiene la lógica para registrar, eliminar y consultar transacciones, asegurando la integridad de los saldos.

#### Registro de Transacciones (`registrarGasto`, `registrarIngreso`, `registrarTransferencia`)
*   **Ajuste de Saldos:** Cada tipo de transacción ajusta automáticamente los saldos de las cuentas involucradas:
    *   **Gasto:** Descuenta el `monto` de la `cuentaOrigen`.
    *   **Ingreso:** Suma el `monto` a la `cuentaDestino`.
    *   **Transferencia:** Descuenta el `monto` de la `cuentaOrigen` y lo suma a la `cuentaDestino`.
*   **Validaciones:**
    *   Verifica la existencia de las cuentas involucradas.
    *   Para gastos y transferencias, valida que haya `saldo` suficiente en la cuenta de origen.
    *   Para transferencias, asegura que las cuentas de origen y destino no sean la misma.
*   **Persistencia:** Crea un registro inmutable de la transacción en la base de datos.

#### Eliminación de Transacción (`eliminarTransaccion`)
*   **Reajuste de Saldos:** Al eliminar una transacción, el sistema revierte su efecto en los saldos de las cuentas:
    *   **Gasto:** El `monto` se devuelve a la `cuentaOrigen`.
    *   **Ingreso:** El `monto` se resta de la `cuentaDestino`.
    *   **Transferencia:** El `monto` se resta de la `cuentaDestino` y se devuelve a la `cuentaOrigen`.
*   **Borrado Físico:** La transacción se elimina permanentemente de la base de datos.

#### Consulta de Transacciones (`obtenerTransaccionesFiltradas`)
*   Permite filtrar transacciones por `usuarioId`, `tipo` (INGRESO, GASTO, TRANSFERENCIA, o "ALL"), rango de fechas (`fechaInicio`, `fechaFin`), `limite` de resultados y `cuentaId` específica.
*   Cuando se filtra por `cuentaId`, busca transacciones donde la cuenta sea tanto origen como destino.

#### Reportes (`obtenerTotalPorRango`, `obtenerFlujoCaja`)
*   `obtenerTotalPorRango`: Calcula la suma total de montos para un `tipo` de transacción específico dentro de un rango de fechas.
*   `obtenerFlujoCaja`: Calcula el balance neto (Ingresos - Gastos) para un usuario en un rango de fechas.

## Contratos (DTOs)

### `GastoIngresoRequest`
Utilizado para registrar gastos e ingresos.
| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `cuentaId` | UUID | ID de la cuenta afectada. |
| `monto` | BigDecimal | Cantidad de dinero. |
| `categoriaId` | UUID | ID de la categoría asociada. |
| `descripcion` | String | Descripción del movimiento. |
| `fecha` | OffsetDateTime | Fecha y hora del movimiento (ISO 8601). |
| `notas` | String? | Notas adicionales (opcional). |

### `TransferenciaRequest`
Utilizado para registrar transferencias entre cuentas.
| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `cuentaOrigenId` | UUID | ID de la cuenta de origen. |
| `cuentaDestinoId` | UUID | ID de la cuenta de destino. |
| `monto` | BigDecimal | Cantidad de dinero a transferir. |
| `descripcion` | String | Descripción del movimiento. |
| `fecha` | OffsetDateTime | Fecha y hora del movimiento (ISO 8601). |
| `notas` | String? | Notas adicionales (opcional). |

### `TransaccionResponse`
Representa la información de una transacción devuelta al cliente.
| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `id` | UUID | ID único de la transacción. |
| `monto` | BigDecimal | Monto de la transacción. |
| `tipo` | String | Tipo de movimiento ("INGRESO", "GASTO", "TRANSFERENCIA"). |
| `descripcion` | String? | Descripción del movimiento. |
| `fecha` | OffsetDateTime | Fecha y hora del movimiento. |
| `categoriaId` | UUID? | ID de la categoría asociada. |
| `nombreCuentaOrigen` | String? | Nombre de la cuenta de origen (si aplica). |
| `nombreCuentaDestino` | String? | Nombre de la cuenta de destino (si aplica). |
| `notas` | String? | Notas adicionales. |

**Ejemplo de JSON de Respuesta (Lista de Transacciones):**
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

## Referencia de API (Controller)

### `TransaccionController`
Expone los endpoints para la gestión de transacciones.

### 1. Registrar Gasto
*   **Método:** `POST`
*   **URL:** `/api/transacciones/gasto`
*   **Descripción:** Registra un gasto y descuenta el monto de la cuenta de origen.
*   **Request Body:** `GastoIngresoRequest`
*   **Respuesta Exitosa:** `201 Created`
```text
"Gasto registrado y saldo actualizado"
```

### 2. Registrar Ingreso
*   **Método:** `POST`
*   **URL:** `/api/transacciones/ingreso`
*   **Descripción:** Registra un ingreso y suma el monto a la cuenta de destino.
*   **Request Body:** `GastoIngresoRequest`
*   **Respuesta Exitosa:** `201 Created`
```text
"Ingreso registrado y saldo actualizado"
```

### 3. Registrar Transferencia
*   **Método:** `POST`
*   **URL:** `/api/transacciones/transferencia`
*   **Descripción:** Mueve dinero entre dos cuentas del mismo usuario.
*   **Request Body:** `TransferenciaRequest`
*   **Respuesta Exitosa:** `201 Created`
```text
"Transferencia realizada con éxito"
```

### 4. Eliminar / Revertir Transacción
*   **Método:** `DELETE`
*   **URL:** `/api/transacciones/{id}`
*   **Descripción:** Borra una transacción y revierte su impacto en los saldos de las cuentas.
*   **Parámetros de Ruta:** `id` (UUID) - ID de la transacción a eliminar.
*   **Respuesta Exitosa:** `200 OK`
```text
"Transacción eliminada y saldos revertidos"
```

### 5. Consulta Filtrada (Historial)
*   **Método:** `GET`
*   **URL:** `/api/transacciones/filtradas`
*   **Descripción:** Obtiene una lista de transacciones aplicando diversos filtros.
*   **Parámetros de Consulta (Query Params):**
    *   `usuarioId` (UUID): ID del usuario.
    *   `fechaInicio` (OffsetDateTime): Inicio del rango de fechas (ISO 8601).
    *   `fechaFin` (OffsetDateTime): Fin del rango de fechas (ISO 8601).
    *   `tipo` (String, opcional): "GASTO", "INGRESO", "TRANSFERENCIA" o "ALL".
    *   `limite` (Int, opcional): Cantidad máxima de resultados.
    *   `cuentaId` (UUID, opcional): ID de una cuenta específica.
*   **Respuesta Exitosa:** `200 OK` (Lista de `TransaccionResponse`)

### 6. Obtener Flujo de Caja
*   **Método:** `GET`
*   **URL:** `/api/transacciones/flujo-caja`
*   **Descripción:** Calcula el balance neto (Ingresos - Gastos) para un usuario en un rango de fechas.
*   **Parámetros de Consulta (Query Params):**
    *   `usuarioId` (UUID): ID del usuario.
    *   `fechaInicio` (OffsetDateTime): Inicio del rango de fechas (ISO 8601).
    *   `fechaFin` (OffsetDateTime): Fin del rango de fechas (ISO 8601).
*   **Respuesta Exitosa:** `200 OK` (BigDecimal)
```json
1174.50
```

### 7. Obtener Total por Rango
*   **Método:** `GET`
*   **URL:** `/api/transacciones/total-por-rango`
*   **Descripción:** Calcula el total de montos para un tipo de transacción específico en un rango de fechas.
*   **Parámetros de Consulta (Query Params):**
    *   `usuarioId` (UUID): ID del usuario.
    *   `tipo` (String): "GASTO", "INGRESO" o "TRANSFERENCIA".
    *   `fechaInicio` (OffsetDateTime): Inicio del rango de fechas (ISO 8601).
    *   `fechaFin` (OffsetDateTime): Fin del rango de fechas (ISO 8601).
*   **Respuesta Exitosa:** `200 OK` (BigDecimal)
```json
500.00
```