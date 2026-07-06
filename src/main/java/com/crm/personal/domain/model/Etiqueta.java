package com.crm.personal.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Etiqueta (Tag) con soporte de jerarquía padre-hijo y color hexadecimal.
 */
@Entity
@Table(name = "etiquetas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = { "padre", "hijos" })
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

    /** Etiqueta padre — null si es raíz */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "padre_id")
    private Etiqueta padre;

    @OneToMany(mappedBy = "padre", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Etiqueta> hijos = new ArrayList<>();
}
