package com.crm.personal.application.desktop.dto;

import java.time.LocalDateTime;
import java.util.List;

public record DesktopTimelineRecordDto(
        Long id,
        String tipo,
        String titulo,
        String contenidoHtml,
        LocalDateTime fecha,
        List<DesktopMediaAdjuntoDto> adjuntos
) {

    public DesktopTimelineRecordDto {
        adjuntos = adjuntos == null ? List.of() : List.copyOf(adjuntos);
    }
}
