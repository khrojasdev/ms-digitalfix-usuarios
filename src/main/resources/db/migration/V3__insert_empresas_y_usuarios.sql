-- Insertar empresas de mantención eléctrica (ID 1 ya es la Empresa por Defecto de V2)
INSERT INTO compania (id, nombre) VALUES (2, 'ElectroRed Valparaíso');
INSERT INTO compania (id, nombre) VALUES (3, 'Mantenciones Alta Tensión S.A.');
INSERT INTO compania (id, nombre) VALUES (4, 'Servicios Eléctricos PyME');

-- Insertar usuarios de prueba (OIDs inventados)
INSERT INTO app_user (id, azure_oid, nombre, email, rol, compania_id, activo)
VALUES (2, 'mock-oid-sup-001', 'Carlos Supervisor', 'carlos@electrored.cl', 'SUPERVISOR', 2, 1);

INSERT INTO app_user (id, azure_oid, nombre, email, rol, compania_id, activo)
VALUES (3, 'mock-oid-cli-002', 'Juan Cliente', 'juan.cliente@pyme.cl', 'CLIENTE', 4, 1);