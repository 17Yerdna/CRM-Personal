package com.crm.personal.domain.dynamicfield.validation;

import com.crm.personal.domain.dynamicfield.model.DynamicFieldType;

public interface FieldValidator {

    DynamicFieldType supports();

    void validate(String fieldName, String rawValue);
}
