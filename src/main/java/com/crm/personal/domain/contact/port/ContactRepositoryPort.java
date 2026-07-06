package com.crm.personal.domain.contact.port;

import com.crm.personal.domain.contact.model.Contact;
import com.crm.personal.domain.contact.model.ContactId;
import com.crm.personal.domain.contact.model.Dni;

import java.util.List;
import java.util.Optional;

public interface ContactRepositoryPort {

    List<Contact> findAll();

    Optional<Contact> findById(ContactId id);

    Optional<Contact> findByDni(Dni dni);

    Contact save(Contact contact);

    void deleteById(ContactId id);
}
