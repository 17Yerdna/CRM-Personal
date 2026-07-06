package com.crm.personal.infrastructure.persistence.model;

import com.crm.personal.infrastructure.validation.ValidDni;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entidad principal del CRM. Representa a un contacto con sus datos,
 * etiquetas (ManyToMany), campos dinámicos (EAV) y expediente (Timeline).
 */
@Entity
@Table(name = "contactos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = { "etiquetas", "camposDinamicos", "timeline" })
public class Contacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false)
    private String nombre;

    /** DNI: exactamente 8 dígitos numéricos */
    @ValidDni
    @Column(nullable = false, unique = true, length = 8)
    private String dni;

    @NotBlank(message = "La dirección es obligatoria")
    @Column(nullable = false)
    private String direccion;

    /** Ruta absoluta al archivo de foto de perfil */
    @Column(name = "foto_perfil_path")
    private String fotoPerfilPath;

    // ── Relaciones ─────────────────────────────────────────────────────────

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "contacto_etiquetas",
        joinColumns        = @JoinColumn(name = "contacto_id"),
        inverseJoinColumns = @JoinColumn(name = "etiqueta_id")
    )
    @Builder.Default
    private Set<Etiqueta> etiquetas = new HashSet<>();

    @OneToMany(mappedBy = "contacto", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CampoDinamicoValor> camposDinamicos = new ArrayList<>();

    @OneToMany(mappedBy = "contacto", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("fecha ASC")
    @Builder.Default
    private List<TimelineRecord> timeline = new ArrayList<>();

    // ── Auditoría ──────────────────────────────────────────────────────────

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
