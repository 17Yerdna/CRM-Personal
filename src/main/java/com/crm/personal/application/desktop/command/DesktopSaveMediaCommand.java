package com.crm.personal.application.desktop.command;

import java.time.LocalDate;

public record DesktopSaveMediaCommand(
        String filePath,
        String nombreArchivo,
        String descripcion,
        String lugar,
        LocalDate fechaCaptura,
        String tipoMime
) {
}
