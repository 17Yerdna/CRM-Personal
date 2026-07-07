package com.crm.personal.infrastructure.persistence.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Relación bidireccional entre dos contactos.
 */
@Entity
@Table(name = "contacto_relaciones",
       uniqueConstraints = @UniqueConstraint(columnNames = {"contacto_origen_id", "contacto_destino_id", "tipo_relacion"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"contactoOrigen", "contactoDestino"})
public class ContactoRelacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contacto_origen_id", nullable = false)
    private Contacto contactoOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contacto_destino_id", nullable = false)
    private Contacto contactoDestino;

    @Column(name = "tipo_relacion", nullable = false)
    private String tipoRelacion;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}
