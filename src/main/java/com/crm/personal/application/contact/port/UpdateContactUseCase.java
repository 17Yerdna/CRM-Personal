package com.crm.personal.application.contact.port;

import com.crm.personal.application.contact.command.UpdateContactCommand;
import com.crm.personal.application.contact.dto.ContactDto;

public interface UpdateContactUseCase {

    ContactDto update(UpdateContactCommand command);
}
