package com.crm.personal.infrastructure.persistence.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Valor de un campo dinámico para un Contacto específico.
 * Implementa el patrón EAV (Entity–Attribute–Value).
 */
@Entity
@Table(
    name = "campo_dinamico_valores",
    uniqueConstraints = @UniqueConstraint(columnNames = { "contacto_id", "campo_id" })
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class CampoDinamicoValor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contacto_id", nullable = false)
    private Contacto contacto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "campo_id", nullable = false)
    private CampoDinamico campo;

    @Column(columnDefinition = "TEXT")
    private String valor;
}
