package com.crm.personal.domain.timeline.model;

import com.crm.personal.domain.shared.DomainException;

public record TimelineRecordId(Long value) {

    public TimelineRecordId {
        if (value != null && value <= 0) {
            throw new DomainException("El id de timeline debe ser positivo");
        }
    }
}
