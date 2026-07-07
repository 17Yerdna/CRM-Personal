package com.crm.personal.application.desktop.dto;

import java.util.List;

public record DesktopEtiquetaDto(Long id, String nombre, String colorHex, Long padreId, List<DesktopEtiquetaDto> hijos) {

    public DesktopEtiquetaDto {
        hijos = hijos == null ? List.of() : List.copyOf(hijos);
    }

    @Override
    public String toString() {
        return nombre;
    }
}
