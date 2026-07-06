package com.crm.personal.application.contact.port;

import com.crm.personal.application.contact.dto.ContactDetailDto;

public interface GetContactDetailUseCase {

    ContactDetailDto getById(Long contactId);
}
