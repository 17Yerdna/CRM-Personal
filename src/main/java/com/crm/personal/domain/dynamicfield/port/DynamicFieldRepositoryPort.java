package com.crm.personal.domain.dynamicfield.port;

import com.crm.personal.domain.dynamicfield.model.DynamicField;
import com.crm.personal.domain.dynamicfield.model.DynamicFieldId;

import java.util.List;
import java.util.Optional;

public interface DynamicFieldRepositoryPort {

    List<DynamicField> findActive();

    Optional<DynamicField> findById(DynamicFieldId id);
}
