# Registro de Cambios — Integración de Interfaces Estilizadas con CRUD MySQL (`SOPTEC-IT`)

Este documento resume todos los cambios realizados en `C:\Users\Fordiis\Documents\SOPTEC_IT Proyecto\SOPTEC-IT` para integrar el diseño visual de `Interfaces-SOPTEC_IT` con la capa real de negocio, DAOs, MySQL (`soptec_it`), manejo de imágenes y auditoría de logs, así como su publicación en el repositorio compartido (`https://github.com/victorsin-M/Oscorp.git`, commit `eed24b7` en `main`).

---

## 1. Estrategia de Integración (Sin Choques)

- **Backend intacto:** Se mantuvo sin colisiones el paquete `com.mycompany.soptec.it.{dao, servicio, modelo, excepciones, util}` y las vistas base originales en `/fxml/*.fxml`.
- **Nueva capa visual estilizada:** Se ubicó en `soptecit.controladores`, `soptecit.util` y recursos en `/soptecit/{vistas, css, fonts, img}`.
- **Respaldo:** La carpeta `Interfaces-SOPTEC_IT` permaneció intacta como respaldo.

---

## 2. Archivos Modificados

| Archivo | Cambio realizado |
| :--- | :--- |
| `src/main/java/module-info.java` | Se abrieron y exportaron los paquetes `soptecit.controladores` y `soptecit.util` para `javafx.fxml`. |
| `src/main/java/com/mycompany/soptec/it/App.java` | Carga de las 4 fuentes **Poppins**, ícono institucional (`/soptecit/img/icono.png`), arranque en `/soptecit/vistas/Login.fxml` y soporte `F11` para pantalla completa manteniendo `UncaughtExceptionHandler` con `AppLogger`. |
| `src/main/java/com/mycompany/soptec/it/util/AlertaUtil.java` | Aplicación de `/soptecit/css/estilos.css` a los diálogos `Alert` y vinculación `initOwner` a la ventana activa. |
| `src/test/java/com/mycompany/soptec/it/FxmlSmokeTest.java` | Ampliado para verificar la carga de las 12 vistas estilizadas (`/soptecit/vistas/*.fxml`) junto con las 10 vistas originales (`/fxml/*.fxml`). |
| `logs/soptec_errores.log` | Registro actualizado de auditoría de pruebas y operaciones ejecutadas. |

---

## 3. Archivos Nuevos Agregados

### 3.1 Recursos Visuales (`src/main/resources/soptecit/`)
- **Estilos:** `css/estilos.css` (extendido con estilos `.imagen-preview`, `.boton-compacto`, `.chip-conexion-ok`, `.chip-conexion-error`, `.log-consola`).
- **Fuentes:** `fonts/Poppins-Regular.ttf`, `Poppins-Medium.ttf`, `Poppins-SemiBold.ttf`, `Poppins-Bold.ttf`.
- **Imágenes:** `img/icono.png`, `img/logo.png`.
- **Vistas FXML (`vistas/`):** `Login.fxml`, `Principal.fxml`, `Inicio.fxml`, `Seguimiento.fxml`, `Ventas.fxml`, `Citas.fxml`, `Levantamientos.fxml`, `Administracion.fxml`, `ComprasAlmacen.fxml`, `Instalaciones.fxml`, `DetalleFactura.fxml`, `LogsPruebas.fxml`.

### 3.2 Utilidades y Controladores (`src/main/java/soptecit/`)
- **Utilidades (`soptecit.util`):**
  - `Tablas.java`: Soporte para tablas tipadas y `String[]`, insignias de estado (`col-estado`) y barras de progreso (`col-avance`).
  - `Mensajes.java`: Puente hacia `AlertaUtil` con el estilo del sistema.
- **Controladores (`soptecit.controladores`):**
  - `LoginController.java`: Acceso con usuario, contraseña, área y registro en `AppLogger`.
  - `PrincipalController.java`: Menú lateral filtrado por área, buscador global hacia Seguimiento, indicador en vivo `MySQL: Conectado / Sin conexión` y acceso a Logs.
  - `InicioController.java`: KPIs en tiempo real (`ProyectoService`, `CitaService`, `MaterialService`, `ClienteService`), proyectos con barra de avance, próximas citas y alertas de bajo stock.
  - `SeguimientoController.java`: Consulta de proyectos en MySQL por ID, nombre o cliente con línea de tiempo y datos de contacto.
  - `VentasController.java`: CRUD completo de **Clientes** (imagen + alta transaccional con teléfono), **Teléfonos de cliente** (`ComboBuscadorUtil`), **Proyectos y propuestas** y **Facturación** (con `DetalleFacturaController.java`).
  - `CitasController.java`: Agenda y CRUD completo de **Citas** conectado a `CitaService` y `ClienteService`.
  - `LevantamientosController.java`: CRUD completo de **Levantamientos** con carga de croquis en `uploads/croquis/` mediante `GestorImagenes`.
  - `AdministracionController.java`: CRUD completo de **Empleados** (con fotografía en `uploads/empleados/`), revisión de propuestas, cuentas por cobrar y cierre de proyectos en MySQL.
  - `ComprasAlmacenController.java`: KPIs en vivo, CRUD completo de **Materiales** y **Proveedores** (con logotipo en `uploads/proveedores/`), movimientos que actualizan existencias reales en MySQL y órdenes de compra.
  - `InstalacionesController.java`: Proyectos y técnicos reales desde MySQL con actualización de estado al guardar avances.
  - `LogsPruebasController.java`: Visor de `logs/soptec_errores.log` y simulaciones controladas (validación, rollback, integridad referencial y caída de conexión).

---

## 4. Verificación y Sincronización Git

- **Compilación y pruebas (`mvn -o test`):**
  - `CrudBaseIntegrationTest`: **3/3** aprobadas.
  - `FxmlSmokeTest`: **1/1** aprobada (22 vistas FXML verificadas).
  - Resultado: **`BUILD SUCCESS`** (`76` archivos fuente, `35` recursos).
- **Repositorio compartido (`origin/main`):**
  - Commit `eed24b7` subido con `git push -u origin main`.
  - Verificado con `git pull origin main` (`Fast-forward` sin conflictos sobre clon limpio) y re-ejecución exitosa de `mvn -o test`.
