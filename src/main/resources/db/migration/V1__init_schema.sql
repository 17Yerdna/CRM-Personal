PRAGMA foreign_keys = ON;

CREATE TABLE contactos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    dni TEXT NOT NULL UNIQUE,
    direccion TEXT NOT NULL,
    foto_perfil_path TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE etiquetas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    color_hex TEXT,
    padre_id INTEGER,
    CONSTRAINT fk_etiquetas_padre
        FOREIGN KEY (padre_id)
        REFERENCES etiquetas(id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_etiquetas_padre_id ON etiquetas(padre_id);

CREATE TABLE contacto_etiquetas (
    contacto_id INTEGER NOT NULL,
    etiqueta_id INTEGER NOT NULL,
    PRIMARY KEY (contacto_id, etiqueta_id),
    CONSTRAINT fk_contacto_etiquetas_contacto
        FOREIGN KEY (contacto_id)
        REFERENCES contactos(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_contacto_etiquetas_etiqueta
        FOREIGN KEY (etiqueta_id)
        REFERENCES etiquetas(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_contacto_etiquetas_etiqueta_id ON contacto_etiquetas(etiqueta_id);

CREATE TABLE campos_dinamicos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL UNIQUE,
    tipo TEXT NOT NULL,
    activo INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE campo_dinamico_valores (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    contacto_id INTEGER NOT NULL,
    campo_id INTEGER NOT NULL,
    valor TEXT,
    CONSTRAINT uk_campo_dinamico_valores_contacto_campo
        UNIQUE (contacto_id, campo_id),
    CONSTRAINT fk_campo_dinamico_valores_contacto
        FOREIGN KEY (contacto_id)
        REFERENCES contactos(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_campo_dinamico_valores_campo
        FOREIGN KEY (campo_id)
        REFERENCES campos_dinamicos(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_campo_dinamico_valores_contacto_id ON campo_dinamico_valores(contacto_id);

CREATE TABLE timeline_records (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    contacto_id INTEGER NOT NULL,
    tipo TEXT NOT NULL,
    titulo TEXT NOT NULL,
    contenido_html TEXT,
    fecha TEXT NOT NULL,
    CONSTRAINT fk_timeline_records_contacto
        FOREIGN KEY (contacto_id)
        REFERENCES contactos(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_timeline_records_contacto_fecha ON timeline_records(contacto_id, fecha);

CREATE TABLE media_adjuntos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    timeline_record_id INTEGER NOT NULL,
    file_path TEXT NOT NULL,
    nombre_archivo TEXT,
    descripcion TEXT,
    lugar TEXT,
    fecha_captura TEXT,
    tipo_mime TEXT,
    CONSTRAINT fk_media_adjuntos_timeline_record
        FOREIGN KEY (timeline_record_id)
        REFERENCES timeline_records(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_media_adjuntos_timeline_record_id ON media_adjuntos(timeline_record_id);
