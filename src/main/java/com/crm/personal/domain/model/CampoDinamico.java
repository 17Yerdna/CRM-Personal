package com.crm.personal.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Definición de un campo dinámico (patrón EAV — Attribute).
 * Ejemplos: "Empresa", "Teléfono secundario", "LinkedIn".
 */
@Entity
@Table(name = "campos_dinamicos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class CampoDinamico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del campo no puede estar vacío")
    @Column(nullable = false, unique = true)
    private String nombre;

    /**
     * Tipo de dato sugerido para la UI: TEXT, NUMBER, DATE, BOOLEAN, EMAIL, URL
     */
    @Builder.Default
    private String tipo = "TEXT";

    @Builder.Default
    @Column(nullable = false)
    private boolean activo = true;
}
