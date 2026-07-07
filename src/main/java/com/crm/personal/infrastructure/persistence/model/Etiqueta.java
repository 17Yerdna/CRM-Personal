package com.crm.personal.infrastructure.persistence.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Etiqueta (Tag) plana con color hexadecimal.
 */
@Entity
@Table(name = "etiquetas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Etiqueta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la etiqueta no puede estar vacío")
    @Column(nullable = false)
    private String nombre;

    /** Color en formato hexadecimal, ej: "#6C63FF" */
    @Column(name = "color_hex", length = 7)
    private String colorHex;
}
