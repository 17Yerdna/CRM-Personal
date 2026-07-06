package com.crm.personal.application.dynamicfield.command;

public record SaveDynamicFieldValueCommand(Long contactId, Long fieldId, String rawValue) {
}
