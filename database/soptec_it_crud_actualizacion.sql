-- =====================================================================
-- SOPTEC-IT | Actualizacion de Base de Datos para CRUD Tablas Base
-- Actividad 4.6: Imagenes, estados activo/inactivo y relaciones 1:N
-- Compatible con MariaDB 10.4+ / MySQL 8.0+
-- =====================================================================

USE soptec_it;

DROP PROCEDURE IF EXISTS sp_migrar_soptec_crud;

DELIMITER $$
CREATE PROCEDURE sp_migrar_soptec_crud()
BEGIN
    -- 1. Campos de imagen en cliente, empleado y proveedor
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cliente' AND COLUMN_NAME = 'imagen') THEN
        ALTER TABLE cliente ADD COLUMN imagen VARCHAR(500) NULL AFTER estado;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'empleado' AND COLUMN_NAME = 'imagen') THEN
        ALTER TABLE empleado ADD COLUMN imagen VARCHAR(500) NULL AFTER correo;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'proveedor' AND COLUMN_NAME = 'imagen') THEN
        ALTER TABLE proveedor ADD COLUMN imagen VARCHAR(500) NULL AFTER correo_proveedor;
    END IF;

    -- 2. Campos de estado logico (activo/inactivo) en las 8 tablas base
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cliente' AND COLUMN_NAME = 'activo') THEN
        ALTER TABLE cliente ADD COLUMN activo TINYINT(1) NOT NULL DEFAULT 1 AFTER imagen;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cliente_telefono' AND COLUMN_NAME = 'activo') THEN
        ALTER TABLE cliente_telefono ADD COLUMN activo TINYINT(1) NOT NULL DEFAULT 1 AFTER telefono;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'empleado' AND COLUMN_NAME = 'activo') THEN
        ALTER TABLE empleado ADD COLUMN activo TINYINT(1) NOT NULL DEFAULT 1 AFTER imagen;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'material' AND COLUMN_NAME = 'activo') THEN
        ALTER TABLE material ADD COLUMN activo TINYINT(1) NOT NULL DEFAULT 1 AFTER existencias;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'proveedor' AND COLUMN_NAME = 'activo') THEN
        ALTER TABLE proveedor ADD COLUMN activo TINYINT(1) NOT NULL DEFAULT 1 AFTER imagen;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cita' AND COLUMN_NAME = 'activo') THEN
        ALTER TABLE cita ADD COLUMN activo TINYINT(1) NOT NULL DEFAULT 1 AFTER id_cliente;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'proyecto' AND COLUMN_NAME = 'activo') THEN
        ALTER TABLE proyecto ADD COLUMN activo TINYINT(1) NOT NULL DEFAULT 1 AFTER costo_total;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'levantamiento' AND COLUMN_NAME = 'activo') THEN
        ALTER TABLE levantamiento ADD COLUMN activo TINYINT(1) NOT NULL DEFAULT 1 AFTER observaciones_tecnicas;
    END IF;

    -- 3. Ajustar cliente_telefono FK a ON DELETE RESTRICT para validar integridad referencial
    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'cliente_telefono' AND CONSTRAINT_NAME = 'cliente_telefono_ibfk_1' AND DELETE_RULE = 'CASCADE') THEN
        ALTER TABLE cliente_telefono DROP FOREIGN KEY cliente_telefono_ibfk_1;
        ALTER TABLE cliente_telefono ADD CONSTRAINT cliente_telefono_ibfk_1 FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente) ON DELETE RESTRICT ON UPDATE CASCADE;
    END IF;

    -- 4. Ajustar proyecto: reemplazar FK id_levantamiento por id_cliente -> cliente(id_cliente)
    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'proyecto' AND CONSTRAINT_NAME = 'proyecto_ibfk_1' AND CONSTRAINT_TYPE = 'FOREIGN KEY') THEN
        ALTER TABLE proyecto DROP FOREIGN KEY proyecto_ibfk_1;
    END IF;

    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'proyecto' AND INDEX_NAME = 'id_levantamiento') THEN
        ALTER TABLE proyecto DROP INDEX id_levantamiento;
    END IF;

    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'proyecto' AND COLUMN_NAME = 'id_levantamiento') THEN
        ALTER TABLE proyecto DROP COLUMN id_levantamiento;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'proyecto' AND COLUMN_NAME = 'id_cliente') THEN
        ALTER TABLE proyecto ADD COLUMN id_cliente INT(11) NOT NULL AFTER costo_total;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'proyecto' AND CONSTRAINT_NAME = 'proyecto_ibfk_cliente') THEN
        ALTER TABLE proyecto ADD CONSTRAINT proyecto_ibfk_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente) ON DELETE RESTRICT ON UPDATE CASCADE;
    END IF;

    -- 5. Ajustar levantamiento: agregar id_proyecto NOT NULL -> proyecto(id_proyecto) y hacer id_cita opcional
    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'levantamiento' AND CONSTRAINT_NAME = 'levantamiento_ibfk_1' AND CONSTRAINT_TYPE = 'FOREIGN KEY') THEN
        ALTER TABLE levantamiento DROP FOREIGN KEY levantamiento_ibfk_1;
    END IF;

    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'levantamiento' AND INDEX_NAME = 'id_cita') THEN
        ALTER TABLE levantamiento DROP INDEX id_cita;
    END IF;

    ALTER TABLE levantamiento MODIFY COLUMN id_cita INT(11) NULL;

    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'levantamiento' AND CONSTRAINT_NAME = 'levantamiento_ibfk_cita') THEN
        ALTER TABLE levantamiento ADD CONSTRAINT levantamiento_ibfk_cita FOREIGN KEY (id_cita) REFERENCES cita(id_cita) ON DELETE RESTRICT ON UPDATE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'levantamiento' AND COLUMN_NAME = 'id_proyecto') THEN
        ALTER TABLE levantamiento ADD COLUMN id_proyecto INT(11) NOT NULL AFTER id_cita;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'levantamiento' AND CONSTRAINT_NAME = 'levantamiento_ibfk_proyecto') THEN
        ALTER TABLE levantamiento ADD CONSTRAINT levantamiento_ibfk_proyecto FOREIGN KEY (id_proyecto) REFERENCES proyecto(id_proyecto) ON DELETE RESTRICT ON UPDATE CASCADE;
    END IF;
END$$
DELIMITER ;

CALL sp_migrar_soptec_crud();
DROP PROCEDURE IF EXISTS sp_migrar_soptec_crud;
