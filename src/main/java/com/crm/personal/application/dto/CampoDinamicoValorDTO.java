package com.crm.personal.application.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampoDinamicoValorDTO {

    private Long   campoId;
    private String campoNombre;
    private String valor;
}
