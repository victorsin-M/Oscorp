# Registro de Cambios — Módulos CRUD Tablas Base (SOPTEC-IT)

Este documento resume todos los cambios realizados en `C:\Users\Fordiis\Documents\SOPTEC_IT Proyecto\SOPTEC-IT` para implementar la **Actividad 4.6 (CRUD tablas base, excepciones, transacciones, imágenes y logs)**.

> **Alcance:** Se implementaron las 8 entidades base (`cliente`, `cliente_telefono`, `empleado`, `material`, `proveedor`, `proyecto`, `cita`, `levantamiento`). No se implementaron CRUD en las tablas de relación muchos a muchos (`proyecto_empleado`, `orden_compra`, `orden_compra_material`, `proyecto_material`, `material_proveedor`) ni en `pago`, quedando listas las capas DAO/Servicio para uso posterior.

---

## 1. Base de Datos (`soptec_it`)

- **Archivo agregado:** `database/soptec_it_crud_actualizacion.sql` (migración idempotente aplicada sobre MySQL/MariaDB `soptec_it`).
- **Campos de imagen agregados (`VARCHAR(500) NULL`):**
  - `cliente.imagen`
  - `empleado.imagen`
  - `proveedor.imagen`
  - (`levantamiento.croquis` ya existía como `TEXT NULL`).
- **Campos de estado lógico (`activo TINYINT(1) NOT NULL DEFAULT 1`):**
  - Agregados en `cliente`, `cliente_telefono`, `empleado`, `material`, `proveedor`, `cita`, `proyecto` y `levantamiento` sin alterar columnas existentes de negocio/domicilio (`cliente.estado`, `proyecto.estado`, `cita.estado_cita`).
- **Ajustes de relaciones e integridad referencial:**
  - `cliente_telefono`: FK `cliente_telefono_ibfk_1` actualizada a `ON DELETE RESTRICT ON UPDATE CASCADE`.
  - `proyecto`: eliminada la dependencia obligatoria `id_levantamiento` (`proyecto_ibfk_1`) y agregada la columna `id_cliente INT(11) NOT NULL` con FK `proyecto_ibfk_cliente -> cliente(id_cliente) ON UPDATE CASCADE`.
  - `levantamiento`: agregada la columna `id_proyecto INT(11) NOT NULL` con FK `levantamiento_ibfk_proyecto -> proyecto(id_proyecto) ON UPDATE CASCADE`, e `id_cita INT(11) NULL` opcional con FK `levantamiento_ibfk_cita -> cita(id_cita) ON UPDATE CASCADE`.

---

## 2. Excepciones Personalizadas (`com.mycompany.soptec.it.excepciones`)

- `SoptecException.java`: Excepción base con `categoria`, `modulo`, `operacion` y mensaje legible para el usuario.
- `ValidacionException.java`: Errores de campos obligatorios, formato (RFC, correo, teléfono, fechas, montos) y registros duplicados.
- `ConexionBDException.java`: Fallos de conexión o pérdida de enlace con MySQL.
- `IntegridadReferencialException.java`: Bloqueo de eliminaciones o referencias inexistentes por llaves foráneas.
- `OperacionBDException.java`: Errores técnicos de SQL o transacciones.

---

## 3. Utilidades y Conexión (`com.mycompany.soptec.it.util` y `dao/Conexion.java`)

- `AppLogger.java`: Registra eventos y excepciones en `logs/soptec_errores.log` con fecha/hora, módulo, proceso, operación, tipo de excepción y descripción técnica, sanitizando credenciales (`password=***`, parámetros JDBC).
- `Validador.java`: Validaciones de RFC, correo electrónico, teléfono (mínimo 10 dígitos), obligatoriedad, longitudes máximas, enteros/decimales no negativos, rangos de fechas y formato de hora (`HH:mm`).
- `GestorImagenes.java`: Selector `FileChooser`, validación de extensiones (`.png`, `.jpg`, `.jpeg`, `.gif`, `.webp`), copia organizada a `uploads/{clientes,empleados,proveedores,croquis}/` y carga segura en `ImageView`.
- `AlertaUtil.java`: Diálogos claros sin exponer SQL ni trazas técnicas al usuario.
- `ComboBuscadorUtil.java`: Vinculación de `ComboBox` descriptivos con `TextField` de búsqueda en tiempo real para evitar captura manual de llaves foráneas.
- `Conexion.java`: Refactorizada con `conectarStrict(...)`, soporte de rollback (`revertirSilencioso`, `cerrarSilencioso`), verificación de conectividad y modo de simulación controlada de caída de conexión para pruebas.

---

## 4. Modelos (`com.mycompany.soptec.it.modelo`)

- **Actualizados:**
  - `Cliente.java`: Alineado a columnas de `cliente` (`idCliente`, `rfc`, `nombreCliente`, `correo`, `calle`, `numero`, `colonia`, `estado`, `imagen`, `activo`) + `toString()` descriptivo.
  - `Empleado.java`: Corregido constructor previo y alineado a `empleado` (`idEmpleado`, `nombre`, `telefono`, `puesto`, `correo`, `imagen`, `activo`).
  - `Material.java`: Alineado a `material` (`idMaterial`, `descripcionMaterial`, `precioCosto`, `existencias`, `activo`) conservando métodos previos.
  - `Proyecto.java`: Corregido import erróneo (`javafx.scene.paint.Material` -> modelo propio), alineado a `proyecto` con datos descriptivos de cliente (`idCliente`, `clienteNombre`, `clienteRfc`, `activo`).
- **Nuevos:**
  - `ClienteTelefono.java`
  - `Proveedor.java`
  - `Cita.java`
  - `Levantamiento.java`

---

## 5. Capa DAO y Servicios (`dao` y `servicio`)

- **DAOs implementados/actualizados en `com.mycompany.soptec.it.dao`:**
  - `DaoSupport.java`: Mapeo uniforme de códigos SQL (`1062` duplicado, `1451`/`1452` FK, `08*` conexión) y parámetros nulos.
  - `ClienteDao.java` (incluye transacción atómica `crearConTelefonoInicial` con `commit`/`rollback`), `ClienteTelefonoDao.java`, `EmpleadoDao.java`, `MaterialDao.java`, `ProveedorDao.java`, `ProyectoDao.java` (incluye transacción `crearConLevantamientoInicial`), `CitaDao.java`, `LevantamientoDao.java`.
  - Todos implementan: Crear, Consultar por ID/clave, Listar (con/sin inactivos), Buscar, Actualizar, Desactivar (baja lógica), Reactivar y Eliminar físico.
- **Servicios implementados en `com.mycompany.soptec.it.servicio`:**
  - `ClienteService.java`, `ClienteTelefonoService.java`, `EmpleadoService.java`, `MaterialService.java`, `ProveedorService.java`, `ProyectoService.java`, `CitaService.java`, `LevantamientoService.java`.
  - Validan reglas de negocio, unicidad, existencia/estado activo del registro padre e impiden eliminar clientes o proyectos con registros relacionados.

---

## 6. Interfaz Gráfica JavaFX, Configuración y Pruebas

- **Estilos:** `src/main/resources/styles/crud.css` (paleta institucional SOPTEC-IT).
- **Vistas FXML (`src/main/resources/fxml/`):**
  - `MainCrud.fxml`, `ClientesView.fxml`, `TelefonosClienteView.fxml`, `EmpleadosView.fxml`, `MaterialesView.fxml`, `ProveedoresView.fxml`, `ProyectosView.fxml`, `CitasView.fxml`, `LevantamientosView.fxml`, `LogsPruebasView.fxml`.
- **Controladores (`src/main/java/com/mycompany/soptec/it/controlador/`):**
  - `MainCrudController.java`, `ClientesController.java`, `TelefonosClienteController.java`, `EmpleadosController.java`, `MaterialesController.java`, `ProveedoresController.java`, `ProyectosController.java`, `CitasController.java`, `LevantamientosController.java`, `LogsPruebasController.java`.
- **Configuración:**
  - `pom.xml`: Agregadas dependencias de prueba `junit-jupiter-api` y `junit-jupiter-engine` (`5.10.3`) y `maven-surefire-plugin` (`3.2.5`).
  - `module-info.java`: Abiertos y exportados los nuevos paquetes (`controlador`, `modelo`, `dao`, `excepciones`, `servicio`, `util`).
  - `App.java`: Configurado para iniciar en `/fxml/MainCrud` maximizado con manejador global de excepciones no capturadas.
- **Pruebas automatizadas (`src/test/java/com/mycompany/soptec/it/`):**
  - `CrudBaseIntegrationTest.java`: Pruebas CRUD completas, integridad referencial, transacciones con rollback y log seguro contra `soptec_it`.
  - `FxmlSmokeTest.java`: Carga de las 10 vistas FXML y sus controladores con JavaFX Toolkit.
