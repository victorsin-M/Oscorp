# Guía de Pruebas, Verificación y Correcciones — SOPTEC-IT

Este documento detalla cómo probar todo lo implementado en `SOPTEC-IT`, cómo verificar los cambios directamente en MySQL (`soptec_it`) y en el archivo de log, y registra los errores detectados y corregidos durante las pruebas.

---

## 1. Ejecución de Pruebas Automatizadas

Desde `C:\Users\Fordiis\Documents\SOPTEC_IT Proyecto\SOPTEC-IT` (con MySQL de XAMPP encendido):

```powershell
& "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.1\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" -o test
```

### Resultado observado en la última ejecución
- `CrudBaseIntegrationTest`: `Tests run: 3, Failures: 0, Errors: 0, Skipped: 0`
  - Flujo relacional completo (`cliente`, `cliente_telefono`, `proyecto`, `cita`, `levantamiento`), validaciones de duplicados, bloqueo por integridad referencial, baja lógica y reactivación.
  - CRUD completo de `empleado`, `material` y `proveedor`.
  - Transacción con rollback (`crearConTelefonoInicial(..., simularFallo=true)`), simulación de caída de conexión MySQL, recuperación y auditoría en `logs/soptec_errores.log` sin exposición de contraseñas.
- `FxmlSmokeTest`: `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`
  - Carga exitosa de `MainCrud.fxml`, las 8 vistas CRUD y `LogsPruebasView.fxml`.

---

## 2. Cómo Ejecutar la Aplicación JavaFX

```powershell
& "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.1\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" -o javafx:run
```

---

## 3. Plan de Pruebas Manuales (Checklist para Video Demostrativo)

### Módulo 1: Clientes (`1. Clientes`)
1. Llenar **RFC** (`XAXX010101000`), **Nombre**, **Correo**, domicilio opcional y **Teléfono inicial** (`3312345678`).
2. Seleccionar una imagen con **Elegir imagen** y verificar la vista previa.
3. Clic en **Guardar** -> Debe crear cliente y teléfono en una misma transacción.
4. Probar **Mostrar inactivos**, **Buscar**, **Mostrar por ID** y **Actualizar**.
5. Probar **Desactivar (Lógico)** / **Eliminar Físico** sobre un cliente que ya tenga teléfono, cita o proyecto -> Debe bloquearse con mensaje claro de integridad referencial.

### Módulo 2: Teléfonos de Cliente (`2. Teléfonos Cliente`)
1. Escribir en **Filtrar cliente** (ej. nombre o RFC) y elegir el cliente del `ComboBox` descriptivo (sin ingresar ID manual).
2. Capturar teléfono de 10+ dígitos y guardar.
3. Intentar registrar el mismo número para el mismo cliente -> Debe rechazarlo por duplicado.
4. Probar actualización, desactivación lógica, reactivación y eliminación física.

### Módulo 3: Empleados (`3. Empleados`)
1. Registrar empleado con nombre, teléfono válido, puesto, correo único e imagen.
2. Intentar registrar otro empleado con el mismo correo -> Debe rechazar duplicado.
3. Probar consulta por ID, búsqueda, actualización, desactivación lógica y reactivación.

### Módulo 4: Materiales (`4. Materiales`)
1. Registrar material con descripción, precio costo y existencias (`>= 0`).
2. Intentar ingresar precio o existencias negativas -> Debe mostrar mensaje de validación.
3. Intentar duplicar descripción -> Debe rechazarlo.
4. Probar búsqueda, actualización, desactivación y eliminación física.

### Módulo 5: Proveedores (`5. Proveedores`)
1. Registrar proveedor con nombre, teléfono, correo e imagen/logotipo.
2. Probar duplicado por nombre o correo, búsqueda, consulta por ID, actualización, desactivación y eliminación física.

### Módulo 6: Proyectos (`6. Proyectos`)
1. Seleccionar cliente mediante **Filtrar cliente** + `ComboBox` descriptivo.
2. Ingresar nombre, fecha inicio, fecha estimada fin, estado y costo total.
3. Probar fecha fin anterior a fecha inicio -> Debe bloquearse por validación.
4. Guardar, actualizar, consultar por ID y verificar en la tabla que muestre `Cliente (Nombre y RFC)`.

### Módulo 7: Citas (`7. Citas`)
1. Seleccionar cliente mediante el buscador descriptivo.
2. Elegir fecha, hora (`HH:mm`), estado (`Programada` / `Confirmada` / `Atendida` / `Cancelada`) y observaciones.
3. Guardar, buscar, actualizar estado y probar desactivar/reactivar.

### Módulo 8: Levantamientos (`8. Levantamientos`)
1. Seleccionar **Proyecto** mediante el buscador descriptivo y opcionalmente una **Cita**.
2. Elegir fecha de visita, observaciones técnicas y cargar una imagen con **Elegir croquis**.
3. Guardar y volver al módulo **6. Proyectos** para intentar desactivar o eliminar físicamente ese proyecto -> Debe bloquearse por tener levantamientos asociados.
4. Desactivar/eliminar el levantamiento primero y luego eliminar los registros en orden inverso para comprobar la integridad.

### Módulo 9: Logs y Pruebas de Error (`Logs y Pruebas de Error`)
1. Clic en **1. Simular Error de Validación** -> Mensaje amigable + entrada `[ERROR] ... TipoExcepcion=VALIDACION` en el log.
2. Clic en **2. Simular Rollback Multi-tabla** -> Se revierte el `INSERT` en `cliente` y se registra en el log; comprobar en MySQL que el cliente simulado no existe.
3. Clic en **3. Simular Integridad Referencial** -> Mensaje claro + entrada `INTEGRIDAD_REFERENCIAL`.
4. Marcar **Simular caída de conexión MySQL**, cambiar de módulo para comprobar que la app **no se cierra**, desmarcar la casilla y pulsar **Verificar Conexión BD** para confirmar recuperación completa.

---

## 4. Consultas SQL de Comprobación Directa en MySQL (`soptec_it`)

```sql
USE soptec_it;

SELECT id_cliente, rfc, nombre_cliente, correo, imagen, activo FROM cliente;
SELECT ct.id_cliente, c.nombre_cliente, ct.telefono, ct.activo
  FROM cliente_telefono ct INNER JOIN cliente c ON c.id_cliente = ct.id_cliente;
SELECT id_empleado, nombre, puesto, correo, imagen, activo FROM empleado;
SELECT id_material, descripcion_material, precio_costo, existencias, activo FROM material;
SELECT id_proveedor, nombre_proveedor, correo_proveedor, imagen, activo FROM proveedor;
SELECT p.id_proyecto, p.nombre_proyecto, c.nombre_cliente, p.estado, p.costo_total, p.activo
  FROM proyecto p INNER JOIN cliente c ON c.id_cliente = p.id_cliente;
SELECT ci.id_cita, ci.fecha_cita, ci.hora_cita, c.nombre_cliente, ci.estado_cita, ci.activo
  FROM cita ci INNER JOIN cliente c ON c.id_cliente = ci.id_cliente;
SELECT l.id_levantamiento, l.fecha_visita, p.nombre_proyecto, l.croquis, l.id_cita, l.activo
  FROM levantamiento l INNER JOIN proyecto p ON p.id_proyecto = l.id_proyecto;
```

---

## 5. Errores Detectados y Corregidos Durante las Pruebas

| # | Hallazgo | Causa | Corrección aplicada |
|---|----------|-------|---------------------|
| 1 | Desalineación de claves foráneas en `soptec_it` | `proyecto` tenía FK única a `id_levantamiento` y `levantamiento` tenía FK única a `id_cita`, impidiendo asignar cliente a proyecto y proyecto a levantamiento como pide el documento. | Se creó y ejecutó `database/soptec_it_crud_actualizacion.sql` para mover la relación a `proyecto.id_cliente -> cliente.id_cliente` y `levantamiento.id_proyecto -> proyecto.id_proyecto` (con `id_cita` opcional), además de `imagen` y `activo`. |
| 2 | Bug en modelos previos (`Proyecto.java` y `Empleado.java`) | `Proyecto.java` importaba `javafx.scene.paint.Material` en vez del modelo propio; `Empleado.java` asignaba campos inexistentes en su constructor. | Se corrigieron ambos modelos y se alinearon todos los atributos y getters/setters con la BD. |
| 3 | Resolución de JUnit en modo offline en `pom.xml` | El artefacto agregador `org.junit.jupiter:junit-jupiter:5.10.3` no estaba en caché local `.m2`, pero sí `junit-jupiter-api` y `junit-jupiter-engine`. | Se declararon explícitamente `junit-jupiter-api` y `junit-jupiter-engine` (`5.10.3`) y `maven-surefire-plugin:3.2.5` en `pom.xml`. |
| 4 | Expresión regular de sanitización en `AppLogger.java` | El patrón `[^\\s,;]+` consumía el paréntesis de cierre `)` en mensajes con `(usuario=root, password=***)`. | Se ajustó el patrón a `[^\\s,;)]*` para preservar el formato del mensaje al ocultar contraseñas. |
| 5 | Ruido en el log durante la limpieza del test | `CrudBaseIntegrationTest` intentaba borrar el teléfono original ya renombrado durante el `finally`, generando un `ERROR` esperado de registro inexistente. | Se verificó existencia previa en la limpieza del test y se re-ejecutó `mvn -o test` confirmando los 4 casos en verde. |
