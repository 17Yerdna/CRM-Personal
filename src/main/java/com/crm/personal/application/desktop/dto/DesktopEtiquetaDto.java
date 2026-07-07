package com.crm.personal.application.desktop.dto;

public record DesktopEtiquetaDto(Long id, String nombre, String colorHex) {

    @Override
    public String toString() {
        return nombre;
    }
}
