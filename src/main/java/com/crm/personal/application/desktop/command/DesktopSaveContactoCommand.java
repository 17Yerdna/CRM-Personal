package com.crm.personal.application.desktop.command;

import java.util.Map;
import java.util.Set;

public record DesktopSaveContactoCommand(
        Long id,
        String nombre,
        String dni,
        String direccion,
        String fotoPerfilPath,
        Set<Long> etiquetaIds,
        Map<Long, String> camposDinamicos
) {
}
