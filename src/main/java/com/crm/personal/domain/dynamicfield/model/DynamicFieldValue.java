package com.crm.personal.domain.dynamicfield.model;

import com.crm.personal.domain.contact.model.ContactId;

public record DynamicFieldValue(ContactId contactId, DynamicFieldId fieldId, String value) {
}
