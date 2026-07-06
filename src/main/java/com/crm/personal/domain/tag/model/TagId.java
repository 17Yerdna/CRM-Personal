package com.crm.personal.domain.tag.model;

import com.crm.personal.domain.shared.DomainException;

public record TagId(Long value) {

    public TagId {
        if (value != null && value <= 0) {
            throw new DomainException("El id de etiqueta debe ser positivo");
        }
    }
}
