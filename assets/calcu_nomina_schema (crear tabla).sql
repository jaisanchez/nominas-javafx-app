
-- Script para crear la tabla 'calculos' en SQLite
CREATE TABLE "calculos" (
    fecha TEXT PRIMARY KEY,
    salario_bruto REAL NOT NULL,
    salario_neto REAL NOT NULL,
    detalle_json TEXT
);

-- Consulta de ejemplo para ver el historial
SELECT * FROM calculos ORDER BY fecha DESC;
