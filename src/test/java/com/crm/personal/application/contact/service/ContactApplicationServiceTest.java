package com.crm.personal.application.contact.service;

import com.crm.personal.application.contact.command.CreateContactCommand;
import com.crm.personal.application.contact.command.SearchContactsQuery;
import com.crm.personal.application.contact.command.UpdateContactCommand;
import com.crm.personal.application.contact.dto.ContactDto;
import com.crm.personal.domain.contact.model.Contact;
import com.crm.personal.domain.contact.model.ContactId;
import com.crm.personal.domain.contact.model.ContactName;
import com.crm.personal.domain.contact.model.Dni;
import com.crm.personal.domain.contact.port.ContactRepositoryPort;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContactApplicationServiceTest {

    @Test
    void should_create_contact() {
        InMemoryContactRepository repository = new InMemoryContactRepository();
        ContactApplicationService service = new ContactApplicationService(repository);

        ContactDto created = service.create(new CreateContactCommand(
                "Andrea",
                "12345678",
                "Calle 123",
                null,
                null,
                null
        ));

        assertEquals(1L, created.id());
        assertEquals("Andrea", created.name());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void should_reject_duplicate_dni() {
        InMemoryContactRepository repository = new InMemoryContactRepository();
        ContactApplicationService service = new ContactApplicationService(repository);

        service.create(new CreateContactCommand("Andrea", "12345678", "Calle 123", null, null, null));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                service.create(new CreateContactCommand("Bruno", "12345678", "Calle 456", null, null, null))
        );

        assertEquals("Ya existe un contacto con DNI: 12345678", error.getMessage());
    }

    @Test
    void should_search_by_text() {
        InMemoryContactRepository repository = new InMemoryContactRepository();
        ContactApplicationService service = new ContactApplicationService(repository);

        service.create(new CreateContactCommand("Andrea", "12345678", "Calle 123", null, null, null));
        service.create(new CreateContactCommand("Carlos", "87654321", "Calle 456", null, null, null));

        List<ContactDto> results = service.search(new SearchContactsQuery("and", null, "AND"));

        assertEquals(1, results.size());
        assertEquals("Andrea", results.getFirst().name());
    }

    @Test
    void should_update_contact() {
        InMemoryContactRepository repository = new InMemoryContactRepository();
        ContactApplicationService service = new ContactApplicationService(repository);

        ContactDto created = service.create(new CreateContactCommand("Andrea", "12345678", "Calle 123", null, null, null));

        ContactDto updated = service.update(new UpdateContactCommand(
                created.id(),
                "Andrea Gomez",
                "12345678",
                "Nueva direccion",
                null,
                null,
                null
        ));

        assertEquals("Andrea Gomez", updated.name());
        assertEquals("Nueva direccion", updated.address());
    }

    private static final class InMemoryContactRepository implements ContactRepositoryPort {

        private final Map<Long, Contact> store = new HashMap<>();
        private long sequence = 1L;

        @Override
        public List<Contact> findAll() {
            return store.values().stream()
                    .sorted(Comparator.comparing(contact -> contact.name().value()))
                    .toList();
        }

        @Override
        public Optional<Contact> findById(ContactId id) {
            return Optional.ofNullable(store.get(id.value()));
        }

        @Override
        public Optional<Contact> findByDni(Dni dni) {
            return store.values().stream()
                    .filter(contact -> contact.dni().equals(dni))
                    .findFirst();
        }

        @Override
        public Contact save(Contact contact) {
            Long id = contact.id() != null ? contact.id().value() : sequence++;
            Contact persisted = new Contact(
                    new ContactId(id),
                    new ContactName(contact.name().value()),
                    new Dni(contact.dni().value()),
                    contact.address(),
                    contact.profilePhoto(),
                    contact.tagIds(),
                    new ArrayList<>(contact.dynamicFields()),
                    contact.createdAt(),
                    contact.updatedAt()
            );
            store.put(id, persisted);
            return persisted;
        }

        @Override
        public void deleteById(ContactId id) {
            store.remove(id.value());
        }
    }
}
