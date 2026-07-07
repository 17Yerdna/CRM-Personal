package com.crm.personal.application.desktop.dto;

import java.time.LocalDate;

public record DesktopMediaAdjuntoDto(
        Long id,
        String filePath,
        String nombreArchivo,
        String descripcion,
        String lugar,
        LocalDate fechaCaptura,
        String tipoMime
) {
}
