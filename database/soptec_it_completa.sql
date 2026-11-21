-- =====================================================================
-- SOPTEC-IT | Base de datos COMPLETA (crear desde cero - 14 tablas)
-- Incluye todas las actualizaciones de Actividad 4.6 y Actividad 4.8
-- Compatible con MariaDB 10.4+ / MySQL 8.0+ (XAMPP / phpMyAdmin)
-- =====================================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS soptec_it
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE soptec_it;

-- ---------------- 1. cliente ----------------
DROP TABLE IF EXISTS cliente;
CREATE TABLE cliente (
  id_cliente      INT(11) AUTO_INCREMENT PRIMARY KEY,
  rfc             VARCHAR(15)  UNIQUE,
  nombre_cliente  VARCHAR(100) NOT NULL,
  correo          VARCHAR(100),
  calle           VARCHAR(100),
  numero          VARCHAR(20),
  colonia         VARCHAR(100),
  estado          VARCHAR(50),
  imagen          VARCHAR(500) NULL,
  activo          TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

-- ---------------- 2. cliente_telefono (1:N multivaluado) ----------------
DROP TABLE IF EXISTS cliente_telefono;
CREATE TABLE cliente_telefono (
  id_cliente  INT(11) NOT NULL,
  telefono    VARCHAR(20) NOT NULL,
  activo      TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (id_cliente, telefono),
  CONSTRAINT cliente_telefono_ibfk_1 FOREIGN KEY (id_cliente)
    REFERENCES cliente(id_cliente) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ---------------- 3. cita (Cliente 1:N Cita) ----------------
DROP TABLE IF EXISTS cita;
CREATE TABLE cita (
  id_cita        INT(11) AUTO_INCREMENT PRIMARY KEY,
  fecha_cita     DATE NOT NULL,
  hora_cita      TIME NOT NULL,
  estado_cita    VARCHAR(30) DEFAULT 'Programada',
  observaciones  TEXT,
  id_cliente     INT(11) NOT NULL,
  activo         TINYINT(1) NOT NULL DEFAULT 1,
  CONSTRAINT fk_cita_cliente FOREIGN KEY (id_cliente)
    REFERENCES cliente(id_cliente) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ---------------- 4. proyecto (Cliente 1:N Proyecto) ----------------
DROP TABLE IF EXISTS proyecto;
CREATE TABLE proyecto (
  id_proyecto         INT(11) AUTO_INCREMENT PRIMARY KEY,
  nombre_proyecto     VARCHAR(150) NOT NULL,
  fecha_inicio        DATE,
  fecha_estimada_fin  DATE,
  estado              VARCHAR(30) DEFAULT 'Planeado',
  costo_total         DECIMAL(12,2) DEFAULT 0.00,
  id_cliente          INT(11) NOT NULL,
  activo              TINYINT(1) NOT NULL DEFAULT 1,
  CONSTRAINT proyecto_ibfk_cliente FOREIGN KEY (id_cliente)
    REFERENCES cliente(id_cliente) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ---------------- 5. levantamiento (Proyecto 1:N, Cita opcional) ----------------
DROP TABLE IF EXISTS levantamiento;
CREATE TABLE levantamiento (
  id_levantamiento        INT(11) AUTO_INCREMENT PRIMARY KEY,
  fecha_visita            DATE NOT NULL,
  croquis                 TEXT NULL,
  observaciones_tecnicas  TEXT,
  activo                  TINYINT(1) NOT NULL DEFAULT 1,
  id_cita                 INT(11) NULL,
  id_proyecto             INT(11) NOT NULL,
  CONSTRAINT levantamiento_ibfk_cita FOREIGN KEY (id_cita)
    REFERENCES cita(id_cita) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT levantamiento_ibfk_proyecto FOREIGN KEY (id_proyecto)
    REFERENCES proyecto(id_proyecto) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ---------------- 6. pago (Proyecto 1:N Pago) ----------------
DROP TABLE IF EXISTS pago;
CREATE TABLE pago (
  id_pago        INT(11) AUTO_INCREMENT PRIMARY KEY,
  id_proyecto    INT(11) NOT NULL,
  concepto       VARCHAR(150),
  monto          DECIMAL(12,2) NOT NULL,
  fecha_pago     DATE NOT NULL,
  estado_cuenta  VARCHAR(30) DEFAULT 'Pagado',
  metodo_pago    VARCHAR(40) NULL,
  activo         TINYINT(1) NOT NULL DEFAULT 1,
  CONSTRAINT fk_pago_proyecto FOREIGN KEY (id_proyecto)
    REFERENCES proyecto(id_proyecto) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ---------------- 7. empleado ----------------
DROP TABLE IF EXISTS empleado;
CREATE TABLE empleado (
  id_empleado  INT(11) AUTO_INCREMENT PRIMARY KEY,
  nombre       VARCHAR(100) NOT NULL,
  telefono     VARCHAR(20),
  puesto       VARCHAR(50),
  correo       VARCHAR(100),
  imagen       VARCHAR(500) NULL,
  activo       TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

-- ---------------- 8. proyecto_empleado (M:N) ----------------
DROP TABLE IF EXISTS proyecto_empleado;
CREATE TABLE proyecto_empleado (
  id_proyecto  INT(11) NOT NULL,
  id_empleado  INT(11) NOT NULL,
  PRIMARY KEY (id_proyecto, id_empleado),
  CONSTRAINT fk_pe_proyecto FOREIGN KEY (id_proyecto)
    REFERENCES proyecto(id_proyecto) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_pe_empleado FOREIGN KEY (id_empleado)
    REFERENCES empleado(id_empleado) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ---------------- 9. proveedor ----------------
DROP TABLE IF EXISTS proveedor;
CREATE TABLE proveedor (
  id_proveedor        INT(11) AUTO_INCREMENT PRIMARY KEY,
  nombre_proveedor    VARCHAR(100) NOT NULL,
  telefono_proveedor  VARCHAR(20),
  correo_proveedor    VARCHAR(100),
  imagen              VARCHAR(500) NULL,
  activo              TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

-- ---------------- 10. material ----------------
DROP TABLE IF EXISTS material;
CREATE TABLE material (
  id_material           INT(11) AUTO_INCREMENT PRIMARY KEY,
  descripcion_material  VARCHAR(255) NOT NULL,
  precio_costo          DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  existencias           INT NOT NULL DEFAULT 0,
  activo                TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

-- ---------------- 11. material_proveedor (M:N) ----------------
DROP TABLE IF EXISTS material_proveedor;
CREATE TABLE material_proveedor (
  id_material   INT(11) NOT NULL,
  id_proveedor  INT(11) NOT NULL,
  PRIMARY KEY (id_material, id_proveedor),
  CONSTRAINT fk_mp_material FOREIGN KEY (id_material)
    REFERENCES material(id_material) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_mp_proveedor FOREIGN KEY (id_proveedor)
    REFERENCES proveedor(id_proveedor) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ---------------- 12. proyecto_material (M:N) ----------------
DROP TABLE IF EXISTS proyecto_material;
CREATE TABLE proyecto_material (
  id_proyecto  INT(11) NOT NULL,
  id_material  INT(11) NOT NULL,
  cantidad     DECIMAL(10,2) NOT NULL DEFAULT 1,
  PRIMARY KEY (id_proyecto, id_material),
  CONSTRAINT fk_pm_proyecto FOREIGN KEY (id_proyecto)
    REFERENCES proyecto(id_proyecto) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_pm_material FOREIGN KEY (id_material)
    REFERENCES material(id_material) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ---------------- 13. orden_compra (Proyecto 1:N, Proveedor 1:N opc.) ----------------
DROP TABLE IF EXISTS orden_compra;
CREATE TABLE orden_compra (
  id_orden_compra  INT(11) AUTO_INCREMENT PRIMARY KEY,
  id_proyecto      INT(11) NOT NULL,
  id_proveedor     INT(11) NULL,
  fecha_orden      DATE NOT NULL,
  total_compra     DECIMAL(12,2) DEFAULT 0.00,
  tiempo_entrega   VARCHAR(50),
  estado_orden     VARCHAR(30) NOT NULL DEFAULT 'Solicitada',
  activo           TINYINT(1) NOT NULL DEFAULT 1,
  CONSTRAINT fk_oc_proyecto FOREIGN KEY (id_proyecto)
    REFERENCES proyecto(id_proyecto) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_oc_proveedor FOREIGN KEY (id_proveedor)
    REFERENCES proveedor(id_proveedor) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ---------------- 14. orden_compra_material (M:N) ----------------
DROP TABLE IF EXISTS orden_compra_material;
CREATE TABLE orden_compra_material (
  id_orden_compra  INT(11) NOT NULL,
  id_material      INT(11) NOT NULL,
  cantidad         DECIMAL(10,2) NOT NULL DEFAULT 1,
  PRIMARY KEY (id_orden_compra, id_material),
  CONSTRAINT fk_ocm_orden FOREIGN KEY (id_orden_compra)
    REFERENCES orden_compra(id_orden_compra) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_ocm_material FOREIGN KEY (id_material)
    REFERENCES material(id_material) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

SET FOREIGN_KEY_CHECKS = 1;
