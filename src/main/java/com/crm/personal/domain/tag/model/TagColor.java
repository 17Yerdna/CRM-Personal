package com.crm.personal.domain.tag.model;

import com.crm.personal.domain.shared.DomainException;

public record TagColor(String hex) {

    private static final String HEX_PATTERN = "#[0-9a-fA-F]{6}";

    public TagColor {
        if (hex == null || !hex.matches(HEX_PATTERN)) {
            throw new DomainException("El color de etiqueta debe tener formato hexadecimal #RRGGBB");
        }
    }
}
