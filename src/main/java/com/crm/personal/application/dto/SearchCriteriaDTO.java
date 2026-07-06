package com.crm.personal.application.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchCriteriaDTO {

    /** Texto libre: busca en nombre y DNI */
    private String texto;

    /** IDs de etiquetas a filtrar (null o vacío = sin filtro) */
    private List<Long> etiquetaIds;

    /** Operador lógico entre etiquetas */
    @Builder.Default
    private SearchOperator operador = SearchOperator.AND;

    public boolean hasTexto() {
        return texto != null && !texto.isBlank();
    }

    public boolean hasEtiquetas() {
        return etiquetaIds != null && !etiquetaIds.isEmpty();
    }
}
