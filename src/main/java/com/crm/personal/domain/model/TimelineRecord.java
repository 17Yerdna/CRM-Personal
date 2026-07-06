package com.crm.personal.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Registro del expediente cronológico de un Contacto.
 * Puede ser una Nota (rich-text HTML) o un conjunto de medios adjuntos.
 */
@Entity
@Table(name = "timeline_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = { "contacto", "adjuntos" })
public class TimelineRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contacto_id", nullable = false)
    private Contacto contacto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TimelineRecordType tipo;

    @Column(nullable = false)
    private String titulo;

    /**
     * Contenido en HTML para registros de tipo NOTA.
     * Para tipo MEDIA puede ser una descripción general del conjunto.
     */
    @Lob
    @Column(name = "contenido_html", columnDefinition = "TEXT")
    private String contenidoHtml;

    /** Determina el orden cronológico en el expediente */
    @Column(nullable = false)
    private LocalDateTime fecha;

    @OneToMany(mappedBy = "timelineRecord", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MediaAdjunto> adjuntos = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (fecha == null) fecha = LocalDateTime.now();
    }
}
