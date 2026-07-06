package com.crm.personal.domain.shared;

public final class NotFoundException extends DomainException {

    public NotFoundException(String message) {
        super(message);
    }
}
