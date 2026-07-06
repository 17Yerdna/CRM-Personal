package com.crm.personal.application.contact.port;

import com.crm.personal.application.contact.command.CreateContactCommand;
import com.crm.personal.application.contact.dto.ContactDto;

public interface CreateContactUseCase {

    ContactDto create(CreateContactCommand command);
}
