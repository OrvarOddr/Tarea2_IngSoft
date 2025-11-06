DELETE FROM cotizacion_item;
DELETE FROM cotizacion;
DELETE FROM variacion;
DELETE FROM mueble;

INSERT INTO mueble (id, nombre_mueble, tipo, precio_base, stock, estado, tamano, material)
VALUES
    (1, 'Silla Comedor Nogal', 'SILLA', 45000, 25, 'ACTIVO', 'MEDIANO', 'Madera Nogal'),
    (2, 'Sillón Relax Premium', 'SILLON', 125000, 8, 'ACTIVO', 'GRANDE', 'Cuero'),
    (3, 'Mesa Centro Minimalista', 'MESA', 78000, 12, 'ACTIVO', 'MEDIANO', 'Madera y vidrio'),
    (4, 'Estante Modular', 'ESTANTE', 63000, 5, 'INACTIVO', 'GRANDE', 'Pino'),
    (5, 'Banco Vikingo Tallado', 'SILLA', 69500, 14, 'ACTIVO', 'MEDIANO', 'Roble rústico'),
    (6, 'Trono del Jarl', 'SILLON', 198000, 3, 'ACTIVO', 'GRANDE', 'Roble y piel'),
    (7, 'Mesa Festín Nórdico', 'MESA', 142000, 6, 'ACTIVO', 'GRANDE', 'Roble macizo'),
    (8, 'Cofre de Tesoro Rúnico', 'CAJON', 88500, 10, 'ACTIVO', 'MEDIANO', 'Madera reforzada');

INSERT INTO variacion (id, mueble_id, nombre, descripcion, valor_ajuste, estrategia_precio, activa)
VALUES
    (1, 1, 'Barniz Premium', 'Acabado brillante resistente a manchas', 6500, 'ADDITIVE', true),
    (2, 1, 'Cojín Acolchado', 'Cojines desmontables acolchados', 15, 'PERCENTAGE', true),
    (3, 2, 'Tapiz Tela', 'Cambio a tapiz de tela respirable', -12000, 'ADDITIVE', true),
    (4, 3, 'Vidrio Templado', 'Mejora de superficie a vidrio templado', 10, 'PERCENTAGE', true),
    (5, 4, 'Armado Básico', 'Incluye armado estándar', 0, 'NONE', true),
    (6, 5, 'Grabado de Runas', 'Detallado artesanal con runas personalizadas', 9500, 'ADDITIVE', true),
    (7, 5, 'Aceite de Pino Boreal', 'Tratamiento protector y aromático', 8, 'PERCENTAGE', true),
    (8, 6, 'Cuero de Oso Polar', 'Tapizado superior con piel auténtica', 22, 'PERCENTAGE', true),
    (9, 6, 'Llamas de Llama', 'Detalle de iluminación LED cálida', 17500, 'ADDITIVE', true),
    (10, 7, 'Extensión de Banquete', 'Paneles adicionales para 4 comensales', 18, 'PERCENTAGE', true),
    (11, 8, 'Refuerzos de Hierro', 'Placas de hierro martillado para seguridad', 15500, 'ADDITIVE', true),
    (12, 8, 'Sellado Antihumedad', 'Forro interior repelente a humedad marina', 12, 'PERCENTAGE', true);
