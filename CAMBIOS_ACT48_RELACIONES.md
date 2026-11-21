# Registro de Cambios — Actividad 4.8: CRUD Tablas Relacionadas, M:N, Excepciones y Transacciones (`SOPTEC-IT`)

Este documento resume los módulos, transacciones, excepciones, vistas y pruebas agregados en `SOPTEC-IT` para cumplir al 100 % con la **Actividad 4.8 (Proyecto cuatrimestral, CRUD tablas relacionadas)**.

---

## 1. Base de Datos (`database/`)

- **`database/soptec_it_act48_actualizacion.sql`** (migración idempotente): asegura las 6 tablas relacionadas/M:N (`pago`, `proyecto_empleado`, `proyecto_material`, `material_proveedor`, `orden_compra`, `orden_compra_material`) y agrega columnas auxiliares (`pago.metodo_pago`, `pago.activo`, `orden_compra.id_proveedor` con FK `fk_oc_proveedor`, `orden_compra.estado_orden`, `orden_compra.activo`).
- **`database/soptec_it_completa.sql`**: script completo desde cero de las **14 tablas** del sistema para importación directa en XAMPP / phpMyAdmin.

---

## 2. Excepciones, Imágenes y Logs (`excepciones` y `util`)

Se diferencian explícitamente las **5 categorías de error** solicitadas:
1. **Validación:** `ValidacionException` (`VALIDACION`)
2. **Conexión a BD:** `ConexionBDException` (`CONEXION_BD`)
3. **Integridad referencial:** `IntegridadReferencialException` (`INTEGRIDAD_REFERENCIAL`)
4. **Almacenamiento de archivos/imágenes:** `ArchivoException` (`ARCHIVO`) — *nuevo*
5. **Operaciones de BD:** `OperacionBDException` (`OPERACION_BD`)

Mejoras adicionales:
- **`GestorImagenes.java`**: valida firma binaria real (PNG/JPG/GIF/BMP), tamaño máximo 10 MB, copia atómica temporal→destino, soporte para evidencias (`evidencias/`) y compatibilidad con rutas previas en `uploads/`.
- **`AlertaUtil.java`**: métodos `tituloPara()` y `mensajeUsuario()` con soporte para `ARCHIVO`, sin exponer SQL ni trazas técnicas.
- **`AppLogger.java`**: registra fecha/hora, módulo, proceso, operación, tipo de excepción y detalle técnico en `logs/soptec_errores.log` sanitizando credenciales y parámetros JDBC.
- Se eliminaron bloques `catch` silenciosos en `Conexion`, `VentasController` y `ComprasAlmacenController`.

---

## 3. Modelos, DAOs y Servicios Nuevos (6 Tablas Relacionadas y M:N)

| Tabla | Relación | Modelo / DAO / Service | Operaciones clave |
|---|---|---|---|
| `pago` | 1:N (`proyecto`) | `Pago`, `PagoDao`, `PagoService` | CRUD completo, consulta por proyecto, suma de pagos activos y cálculo de saldo |
| `proyecto_empleado` | M:N (`proyecto` ↔ `empleado`) | `ProyectoEmpleado`, `ProyectoEmpleadoDao`, `ProyectoEmpleadoService` | CRUD M:N, consulta/filtrado y sincronización transaccional de equipo |
| `proyecto_material` | M:N (`proyecto` ↔ `material`) | `ProyectoMaterial`, `ProyectoMaterialDao`, `ProyectoMaterialService` | CRUD M:N + descuento/reintegro atómico de existencias en `material` |
| `material_proveedor` | M:N (`material` ↔ `proveedor`) | `MaterialProveedor`, `MaterialProveedorDao`, `MaterialProveedorService` | CRUD M:N, filtrado por material/proveedor y sincronización transaccional |
| `orden_compra` + `orden_compra_material` | 1:N + M:N (`orden_compra` ↔ `material`) | `OrdenCompra`, `OrdenCompraMaterial`, `OrdenCompraDao`, `OrdenCompraMaterialDao`, `OrdenCompraService` | Alta/actualización conjunta atómica cabecera + partidas + total calculado; recepción atómica que incrementa `material.existencias` |

---

## 4. Operaciones Multi-Tabla con Transacción y `rollback`

1. **Cita + Levantamiento (`CitaService.crearConLevantamientoInicial`)**: si falla el levantamiento relacionado, la cita se revierte completamente.
2. **Proyecto/Venta + Materiales + Técnicos + Pago inicial (`ProyectoService.crearProyectoIntegral`)**: si falla el stock, un material, un técnico o el pago inicial, se revierte toda la operación.
3. **Orden de Compra + Partidas (`OrdenCompraService.crearConPartidas` / `actualizarConPartidas` / `recibirOrden`)**: cabecera, partidas y actualización de existencias en una sola transacción.
4. **Salida de Almacén a Proyecto (`ProyectoMaterialService.asignarConDescuentoStock`)**: descuento de inventario + registro en `proyecto_material` atómicos.
5. **Cliente + Teléfono inicial (`ClienteService.crearConTelefonoInicial`)**: alta atómica ya existente.

---

## 5. Vistas y Controladores Estilizados Actualizados (`soptecit`)

- **Ventas (`Ventas.fxml` / `VentasController.java`)**: registro integral opcional en Proyectos (material + técnico + anticipo) y Facturación conectada realmente a la tabla `pago` (eliminando datos en memoria).
- **Administración (`Administracion.fxml` / `AdministracionController.java`)**: pestaña *Cuentas por cobrar (Pagos)* conectada a `PagoService` con CRUD completo y KPIs reales de cartera, cobrado y saldo pendiente.
- **Compras-Almacén (`ComprasAlmacen.fxml` / `ComprasAlmacenController.java`)**: pestaña *Órdenes de compra* conectada a `orden_compra` + `orden_compra_material` (M:N), nueva pestaña *Materiales ↔ Proveedores* (`material_proveedor` M:N) y salidas vinculadas a `proyecto_material`.
- **Instalaciones (`Instalaciones.fxml` / `InstalacionesController.java`)**: pestañas *Avance de obra* (con evidencias en `GestorImagenes`), *Técnicos por proyecto (`proyecto_empleado`)* y *Materiales por proyecto (`proyecto_material`)*.
- **Citas (`Citas.fxml` / `CitasController.java`)**: opción para agendar cita junto con su levantamiento técnico inicial en una sola transacción.
- **Logs y Pruebas (`LogsPruebas.fxml` / `LogsPruebasController.java`)**: botones de simulación controlada para las 5 categorías de excepción y los 4 escenarios de rollback multi-tabla.

---

## 6. Pruebas Automatizadas

- **`CrudBaseIntegrationTest`**: CRUD de las 8 tablas base, integridad referencial y rollback de cliente + teléfono.
- **`CrudRelacionesIntegrationTest`**: CRUD de las 6 tablas relacionadas/M:N, rollbacks de Cita+Levantamiento, Proyecto+Material+Pago y Orden+Partidas, validación de las 5 categorías de excepción, validación binaria de imágenes y sanitización de logs.
- **`FxmlSmokeTest`**: carga automatizada de las 22 vistas FXML.
