package com.crm.personal.application.desktop.dto;

public record DesktopContactoRelacionDto(
    Long id,
    Long contactoRelacionadoId,
    String contactoRelacionadoNombre,
    String tipoRelacion,
    String descripcion
) {
}
