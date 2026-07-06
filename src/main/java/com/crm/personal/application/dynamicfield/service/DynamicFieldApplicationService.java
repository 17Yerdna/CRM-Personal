package com.crm.personal.application.dynamicfield.service;

import com.crm.personal.application.dynamicfield.command.SaveDynamicFieldValueCommand;
import com.crm.personal.application.dynamicfield.port.SaveDynamicFieldValueUseCase;
import com.crm.personal.domain.contact.model.ContactId;
import com.crm.personal.domain.dynamicfield.model.DynamicField;
import com.crm.personal.domain.dynamicfield.model.DynamicFieldId;
import com.crm.personal.domain.dynamicfield.model.DynamicFieldType;
import com.crm.personal.domain.dynamicfield.model.DynamicFieldValue;
import com.crm.personal.domain.dynamicfield.port.DynamicFieldRepositoryPort;
import com.crm.personal.domain.dynamicfield.port.DynamicFieldValueRepositoryPort;
import com.crm.personal.domain.dynamicfield.validation.DateFieldValidator;
import com.crm.personal.domain.dynamicfield.validation.FieldValidator;
import com.crm.personal.domain.dynamicfield.validation.TextFieldValidator;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;

@Service
public class DynamicFieldApplicationService implements SaveDynamicFieldValueUseCase {

    private final DynamicFieldRepositoryPort fields;
    private final DynamicFieldValueRepositoryPort values;
    private final EnumMap<DynamicFieldType, FieldValidator> validators;

    public DynamicFieldApplicationService(
            DynamicFieldRepositoryPort fields,
            DynamicFieldValueRepositoryPort values
    ) {
        this.fields = fields;
        this.values = values;
        this.validators = new EnumMap<>(DynamicFieldType.class);

        registerValidators(List.of(
                new TextFieldValidator(),
                new DateFieldValidator()
        ));
    }

    @Override
    public void save(SaveDynamicFieldValueCommand command) {
        DynamicField field = fields.findById(new DynamicFieldId(command.fieldId()))
                .orElseThrow(() -> new IllegalArgumentException("Campo dinámico no encontrado: " + command.fieldId()));

        FieldValidator validator = validators.get(field.type());
        if (validator != null) {
            validator.validate(field.name(), command.rawValue());
        }

        values.save(new DynamicFieldValue(
                new ContactId(command.contactId()),
                new DynamicFieldId(command.fieldId()),
                command.rawValue() != null ? command.rawValue().trim() : null
        ));
    }

    private void registerValidators(List<FieldValidator> validators) {
        validators.forEach(validator -> this.validators.put(validator.supports(), validator));
    }
}
