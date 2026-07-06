package com.crm.personal.domain.dynamicfield.port;

import com.crm.personal.domain.dynamicfield.model.DynamicFieldValue;

public interface DynamicFieldValueRepositoryPort {

    DynamicFieldValue save(DynamicFieldValue value);
}
