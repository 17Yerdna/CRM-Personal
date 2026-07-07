package com.crm.personal.application.desktop.command;

public record DesktopCrearRelacionCommand(
    Long origenId,
    Long destinoId,
    String tipoRelacion,
    String descripcion
) {
}
