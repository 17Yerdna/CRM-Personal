package com.crm.personal.application.dto;

import lombok.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactoDTO {

    private Long   id;           // null para crear, non-null para actualizar
    private String nombre;
    private String dni;
    private String direccion;
    private String fotoPerfilPath;

    @Builder.Default
    private Set<Long> etiquetaIds = new HashSet<>();

    /** campoId → valor (EAV) */
    @Builder.Default
    private Map<Long, String> camposDinamicos = new HashMap<>();
}
