package com.crm.personal.domain.dynamicfield.model;

import com.crm.personal.domain.shared.DomainException;

public record DynamicFieldId(Long value) {

    public DynamicFieldId {
        if (value != null && value <= 0) {
            throw new DomainException("El id de campo dinamico debe ser positivo");
        }
    }
}
