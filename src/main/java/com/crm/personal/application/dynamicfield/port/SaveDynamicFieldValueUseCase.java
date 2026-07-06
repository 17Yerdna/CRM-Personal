package com.crm.personal.application.dynamicfield.port;

import com.crm.personal.application.dynamicfield.command.SaveDynamicFieldValueCommand;

public interface SaveDynamicFieldValueUseCase {

    void save(SaveDynamicFieldValueCommand command);
}
