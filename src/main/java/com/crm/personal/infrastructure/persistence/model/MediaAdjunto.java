package com.crm.personal.infrastructure.persistence.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Archivo multimedia adjunto a un TimelineRecord.
 * Contiene metadatos editables: descripción, lugar y fecha de captura.
 */
@Entity
@Table(name = "media_adjuntos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "timelineRecord")
public class MediaAdjunto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timeline_record_id", nullable = false)
    private TimelineRecord timelineRecord;

    /** Ruta absoluta al archivo en disco */
    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "nombre_archivo")
    private String nombreArchivo;

    /** Descripción / nota del usuario sobre este archivo */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /** Lugar donde se tomó la foto */
    private String lugar;

    /** Fecha en que se capturó (editable por el usuario) */
    @Column(name = "fecha_captura")
    private LocalDate fechaCaptura;

    /** MIME type: image/jpeg, image/png, etc. */
    @Column(name = "tipo_mime", length = 50)
    private String tipoMime;
}
