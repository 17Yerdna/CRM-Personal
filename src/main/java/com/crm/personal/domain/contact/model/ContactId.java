package com.crm.personal.domain.contact.model;

import com.crm.personal.domain.shared.DomainException;

public record ContactId(Long value) {

    public ContactId {
        if (value != null && value <= 0) {
            throw new DomainException("El id de contacto debe ser positivo");
        }
    }
}
