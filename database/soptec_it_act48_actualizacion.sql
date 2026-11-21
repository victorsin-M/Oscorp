-- =====================================================================
-- SOPTEC-IT | Actualizacion de Base de Datos para Actividad 4.8
-- CRUD Tablas Relacionadas, Relaciones M:N, Pagos y Ordenes de Compra
-- Compatible con MariaDB 10.4+ / MySQL 8.0+ (script idempotente)
-- =====================================================================

USE soptec_it;

-- 1. Asegurar existencia de tablas relacionadas y M:N
CREATE TABLE IF NOT EXISTS pago (
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

CREATE TABLE IF NOT EXISTS proyecto_empleado (
  id_proyecto  INT(11) NOT NULL,
  id_empleado  INT(11) NOT NULL,
  PRIMARY KEY (id_proyecto, id_empleado),
  CONSTRAINT fk_pe_proyecto FOREIGN KEY (id_proyecto)
    REFERENCES proyecto(id_proyecto) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_pe_empleado FOREIGN KEY (id_empleado)
    REFERENCES empleado(id_empleado) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS material_proveedor (
  id_material   INT(11) NOT NULL,
  id_proveedor  INT(11) NOT NULL,
  PRIMARY KEY (id_material, id_proveedor),
  CONSTRAINT fk_mp_material FOREIGN KEY (id_material)
    REFERENCES material(id_material) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_mp_proveedor FOREIGN KEY (id_proveedor)
    REFERENCES proveedor(id_proveedor) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS proyecto_material (
  id_proyecto  INT(11) NOT NULL,
  id_material  INT(11) NOT NULL,
  cantidad     DECIMAL(10,2) NOT NULL DEFAULT 1,
  PRIMARY KEY (id_proyecto, id_material),
  CONSTRAINT fk_pm_proyecto FOREIGN KEY (id_proyecto)
    REFERENCES proyecto(id_proyecto) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_pm_material FOREIGN KEY (id_material)
    REFERENCES material(id_material) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS orden_compra (
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

CREATE TABLE IF NOT EXISTS orden_compra_material (
  id_orden_compra  INT(11) NOT NULL,
  id_material      INT(11) NOT NULL,
  cantidad         DECIMAL(10,2) NOT NULL DEFAULT 1,
  PRIMARY KEY (id_orden_compra, id_material),
  CONSTRAINT fk_ocm_orden FOREIGN KEY (id_orden_compra)
    REFERENCES orden_compra(id_orden_compra) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_ocm_material FOREIGN KEY (id_material)
    REFERENCES material(id_material) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 2. Agregar columnas auxiliares de forma idempotente si las tablas ya existian
DROP PROCEDURE IF EXISTS sp_migrar_soptec_act48;

DELIMITER $$
CREATE PROCEDURE sp_migrar_soptec_act48()
BEGIN
    -- Columnas en pago
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pago' AND COLUMN_NAME = 'metodo_pago') THEN
        ALTER TABLE pago ADD COLUMN metodo_pago VARCHAR(40) NULL AFTER estado_cuenta;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pago' AND COLUMN_NAME = 'activo') THEN
        ALTER TABLE pago ADD COLUMN activo TINYINT(1) NOT NULL DEFAULT 1 AFTER metodo_pago;
    END IF;

    -- Columnas y FK en orden_compra
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orden_compra' AND COLUMN_NAME = 'id_proveedor') THEN
        ALTER TABLE orden_compra ADD COLUMN id_proveedor INT(11) NULL AFTER id_proyecto;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orden_compra' AND CONSTRAINT_NAME = 'fk_oc_proveedor') THEN
        ALTER TABLE orden_compra ADD CONSTRAINT fk_oc_proveedor FOREIGN KEY (id_proveedor) REFERENCES proveedor(id_proveedor) ON DELETE RESTRICT ON UPDATE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orden_compra' AND COLUMN_NAME = 'estado_orden') THEN
        ALTER TABLE orden_compra ADD COLUMN estado_orden VARCHAR(30) NOT NULL DEFAULT 'Solicitada' AFTER tiempo_entrega;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orden_compra' AND COLUMN_NAME = 'activo') THEN
        ALTER TABLE orden_compra ADD COLUMN activo TINYINT(1) NOT NULL DEFAULT 1 AFTER estado_orden;
    END IF;
END$$
DELIMITER ;

CALL sp_migrar_soptec_act48();
DROP PROCEDURE IF EXISTS sp_migrar_soptec_act48;
