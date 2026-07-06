package com.crm.personal.application.contact.port;

import com.crm.personal.application.contact.command.SearchContactsQuery;
import com.crm.personal.application.contact.dto.ContactDto;

import java.util.List;

public interface SearchContactsUseCase {

    List<ContactDto> search(SearchContactsQuery query);
}
