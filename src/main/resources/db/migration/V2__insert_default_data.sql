-- Insertar empresa por defecto requerida para el flujo de autoprovisión
INSERT INTO compania (id, nombre) VALUES (1, 'Empresa por Defecto');

-- Insertar un usuario administrador inicial de prueba vinculado al tenant
INSERT INTO app_user (id, azure_oid, nombre, email, rol, compania_id, activo)
VALUES (1, 'admin-oid-demo-123', 'Administrador Red DigitalFix', 'admin@digitalfix.cl', 'ADMIN', 1, 1);