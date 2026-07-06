package com.crm.personal.application.contact.service;

import com.crm.personal.application.contact.command.CreateContactCommand;
import com.crm.personal.application.contact.command.SearchContactsQuery;
import com.crm.personal.application.contact.command.UpdateContactCommand;
import com.crm.personal.application.contact.dto.ContactDetailDto;
import com.crm.personal.application.contact.dto.ContactDto;
import com.crm.personal.application.contact.port.CreateContactUseCase;
import com.crm.personal.application.contact.port.DeleteContactUseCase;
import com.crm.personal.application.contact.port.GetContactDetailUseCase;
import com.crm.personal.application.contact.port.SearchContactsUseCase;
import com.crm.personal.application.contact.port.UpdateContactUseCase;
import com.crm.personal.domain.contact.model.Contact;
import com.crm.personal.domain.contact.model.ContactId;
import com.crm.personal.domain.contact.model.ContactName;
import com.crm.personal.domain.contact.model.Dni;
import com.crm.personal.domain.contact.model.ProfilePhoto;
import com.crm.personal.domain.contact.port.ContactRepositoryPort;
import com.crm.personal.domain.dynamicfield.model.DynamicFieldValue;
import com.crm.personal.domain.shared.NotFoundException;
import com.crm.personal.domain.tag.model.TagId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class ContactApplicationService implements
        CreateContactUseCase,
        UpdateContactUseCase,
        DeleteContactUseCase,
        SearchContactsUseCase,
        GetContactDetailUseCase {

    private final ContactRepositoryPort contacts;

    public ContactApplicationService(ContactRepositoryPort contacts) {
        this.contacts = contacts;
    }

    @Override
    public ContactDto create(CreateContactCommand command) {
        Contact contact = new Contact(
                null,
                new ContactName(command.name()),
                new Dni(command.dni()),
                command.address(),
                blankToNull(command.profilePhotoPath()),
                toTagIds(command.tagIds()),
                toDynamicFieldValues(command.dynamicFields()),
                null,
                null
        );

        return toDto(contacts.save(contact));
    }

    @Override
    public ContactDto update(UpdateContactCommand command) {
        ContactId id = new ContactId(command.id());
        Contact existing = contacts.findById(id)
                .orElseThrow(() -> new NotFoundException("Contacto no encontrado: " + command.id()));

        Contact updated = new Contact(
                existing.id(),
                new ContactName(command.name()),
                new Dni(command.dni()),
                command.address(),
                blankToNull(command.profilePhotoPath()),
                toTagIds(command.tagIds()),
                toDynamicFieldValues(command.dynamicFields()),
                existing.createdAt(),
                existing.updatedAt()
        );

        return toDto(contacts.save(updated));
    }

    @Override
    public void delete(Long contactId) {
        contacts.deleteById(new ContactId(contactId));
    }

    @Override
    public List<ContactDto> search(SearchContactsQuery query) {
        Predicate<Contact> matchesText = contact -> {
            if (query.text() == null || query.text().isBlank()) {
                return true;
            }
            String normalized = query.text().trim().toLowerCase();
            return contact.name().value().toLowerCase().contains(normalized)
                    || contact.dni().value().contains(normalized);
        };

        Predicate<Contact> matchesTags = contact -> {
            if (query.tagIds() == null || query.tagIds().isEmpty()) {
                return true;
            }

            Set<Long> selected = query.tagIds();
            Set<Long> owned = contact.tagIds().stream().map(TagId::value).collect(Collectors.toSet());

            if ("AND".equalsIgnoreCase(query.operator())) {
                return owned.containsAll(selected);
            }

            return selected.stream().anyMatch(owned::contains);
        };

        return contacts.findAll().stream()
                .filter(matchesText.and(matchesTags))
                .map(this::toDto)
                .toList();
    }

    @Override
    public ContactDetailDto getById(Long contactId) {
        Contact contact = contacts.findById(new ContactId(contactId))
                .orElseThrow(() -> new NotFoundException("Contacto no encontrado: " + contactId));

        Map<Long, String> dynamicFields = contact.dynamicFields().stream()
                .collect(Collectors.toMap(value -> value.fieldId().value(), DynamicFieldValue::value));

        return new ContactDetailDto(
                contact.id() != null ? contact.id().value() : null,
                contact.name().value(),
                contact.dni().value(),
                contact.address(),
                contact.profilePhoto() != null ? contact.profilePhoto().relativePath() : null,
                contact.tagIds().stream().map(TagId::value).collect(Collectors.toSet()),
                dynamicFields,
                List.of(),
                contact.createdAt(),
                contact.updatedAt()
        );
    }

    private ContactDto toDto(Contact contact) {
        return new ContactDto(
                contact.id() != null ? contact.id().value() : null,
                contact.name().value(),
                contact.dni().value(),
                contact.address(),
                contact.profilePhoto() != null ? contact.profilePhoto().relativePath() : null,
                contact.tagIds().stream().map(TagId::value).collect(Collectors.toSet())
        );
    }

    private Set<TagId> toTagIds(Set<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return Set.of();
        }
        return tagIds.stream().map(TagId::new).collect(Collectors.toUnmodifiableSet());
    }

    private List<DynamicFieldValue> toDynamicFieldValues(Map<Long, String> dynamicFields) {
        if (dynamicFields == null || dynamicFields.isEmpty()) {
            return List.of();
        }

        return dynamicFields.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isBlank())
                .map(entry -> new DynamicFieldValue(null, new com.crm.personal.domain.dynamicfield.model.DynamicFieldId(entry.getKey()), entry.getValue().trim()))
                .toList();
    }

    private ProfilePhoto blankToNull(String profilePhotoPath) {
        if (profilePhotoPath == null || profilePhotoPath.isBlank()) {
            return null;
        }
        return new ProfilePhoto(profilePhotoPath.trim());
    }
}
