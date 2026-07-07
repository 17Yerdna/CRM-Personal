package com.crm.personal.application.desktop.dto;

import java.util.List;
import java.util.Set;

public record DesktopContactoDto(
        Long id,
        String nombre,
        String dni,
        String direccion,
        String fotoPerfilPath,
        Set<DesktopEtiquetaDto> etiquetas,
        List<DesktopCampoValorDto> camposDinamicos
) {

    public DesktopContactoDto {
        etiquetas = etiquetas == null ? Set.of() : Set.copyOf(etiquetas);
        camposDinamicos = camposDinamicos == null ? List.of() : List.copyOf(camposDinamicos);
    }

    @Override
    public String toString() {
        return nombre;
    }
}
