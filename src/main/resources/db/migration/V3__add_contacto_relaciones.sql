-- Agregar tabla de relaciones entre contactos
CREATE TABLE contacto_relaciones (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    contacto_origen_id INTEGER NOT NULL,
    contacto_destino_id INTEGER NOT NULL,
    tipo_relacion TEXT NOT NULL,
    descripcion TEXT,
    fecha_creacion TEXT NOT NULL,
    CONSTRAINT fk_relacion_origen FOREIGN KEY (contacto_origen_id) REFERENCES contactos(id) ON DELETE CASCADE,
    CONSTRAINT fk_relacion_destino FOREIGN KEY (contacto_destino_id) REFERENCES contactos(id) ON DELETE CASCADE,
    CONSTRAINT uk_relacion UNIQUE (contacto_origen_id, contacto_destino_id, tipo_relacion)
);

CREATE INDEX idx_relacion_origen ON contacto_relaciones(contacto_origen_id);
CREATE INDEX idx_relacion_destino ON contacto_relaciones(contacto_destino_id);
