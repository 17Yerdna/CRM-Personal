package com.crm.personal.application.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EtiquetaDTO {

    private Long   id;        // null para crear
    private String nombre;
    private String colorHex;  // ej. "#6C63FF"
    private Long   padreId;   // null = etiqueta raíz
}
