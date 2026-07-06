package com.crm.personal.application.service;

import com.crm.personal.application.contact.command.CreateContactCommand;
import com.crm.personal.application.contact.command.SearchContactsQuery;
import com.crm.personal.application.contact.command.UpdateContactCommand;
import com.crm.personal.application.contact.dto.ContactDto;
import com.crm.personal.application.contact.port.CreateContactUseCase;
import com.crm.personal.application.contact.port.DeleteContactUseCase;
import com.crm.personal.application.contact.port.SearchContactsUseCase;
import com.crm.personal.application.contact.port.UpdateContactUseCase;
import com.crm.personal.application.dto.ContactoDTO;
import com.crm.personal.application.dto.SearchCriteriaDTO;
import com.crm.personal.infrastructure.persistence.model.*;
import com.crm.personal.infrastructure.persistence.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ContactoService {

    private static final Logger log = LoggerFactory.getLogger(ContactoService.class);

    private final ContactoRepository contactoRepo;
    private final CreateContactUseCase createContactUseCase;
    private final UpdateContactUseCase updateContactUseCase;
    private final DeleteContactUseCase deleteContactUseCase;
    private final SearchContactsUseCase searchContactsUseCase;

    public ContactoService(ContactoRepository contactoRepo,
                           CreateContactUseCase createContactUseCase,
                           UpdateContactUseCase updateContactUseCase,
                           DeleteContactUseCase deleteContactUseCase,
                           SearchContactsUseCase searchContactsUseCase) {
        this.contactoRepo = contactoRepo;
        this.createContactUseCase = createContactUseCase;
        this.updateContactUseCase = updateContactUseCase;
        this.deleteContactUseCase = deleteContactUseCase;
        this.searchContactsUseCase = searchContactsUseCase;
    }

    @Transactional(readOnly = true)
    public List<Contacto> findAll() {
        return resolveContacts(searchContactsUseCase.search(new SearchContactsQuery(null, null, "AND")));
    }

    @Transactional(readOnly = true)
    public Contacto findById(Long id) {
        return contactoRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Contacto no encontrado: " + id));
    }

    /**
     * Carga el contacto con todas sus colecciones lazy inicializadas
     * (para mostrarlo en la UI fuera del contexto transaccional).
     */
    @Transactional(readOnly = true)
    public Contacto loadFull(Long id) {
        Contacto c = findById(id);
        Hibernate.initialize(c.getEtiquetas());
        Hibernate.initialize(c.getCamposDinamicos());
        c.getCamposDinamicos().forEach(v -> Hibernate.initialize(v.getCampo()));
        // El timeline se carga por separado via TimelineService
        return c;
    }

    /** Crea un nuevo contacto. */
    public Contacto save(ContactoDTO dto) {
        ContactDto saved = createContactUseCase.create(toCreateCommand(dto));
        Contacto loaded = loadFull(saved.id());
        log.info("Contacto creado: {} (DNI: {})", loaded.getNombre(), loaded.getDni());
        return loaded;
    }

    /** Actualiza un contacto existente. */
    public Contacto update(Long id, ContactoDTO dto) {
        ContactDto updated = updateContactUseCase.update(toUpdateCommand(id, dto));
        Contacto loaded = loadFull(updated.id());
        log.info("Contacto actualizado: {} (ID: {})", loaded.getNombre(), id);
        return loaded;
    }

    public void delete(Long id) {
        Contacto contacto = findById(id);
        deleteContactUseCase.delete(id);
        log.info("Contacto eliminado: {} (ID: {})", contacto.getNombre(), id);
    }

    /**
     * Motor de búsqueda combinada: texto libre + etiquetas con operador AND/OR.
     */
    @Transactional(readOnly = true)
    public List<Contacto> search(SearchCriteriaDTO criteria) {
        ContactDto[] results = searchContactsUseCase.search(new SearchContactsQuery(
                        criteria.getTexto(),
                        criteria.getEtiquetaIds() == null ? null : new HashSet<>(criteria.getEtiquetaIds()),
                        criteria.getOperador() == null ? "AND" : criteria.getOperador().name()
                ))
                .toArray(ContactDto[]::new);

        return resolveContacts(Arrays.asList(results));
    }

    private List<Contacto> resolveContacts(List<ContactDto> results) {
        if (results.isEmpty()) {
            return List.of();
        }

        Map<Long, Contacto> byId = contactoRepo.findAllById(
                results.stream().map(ContactDto::id).toList()
        ).stream().collect(Collectors.toMap(Contacto::getId, contacto -> contacto));

        return results.stream()
                .map(ContactDto::id)
                .map(byId::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private CreateContactCommand toCreateCommand(ContactoDTO dto) {
        return new CreateContactCommand(
                dto.getNombre(),
                dto.getDni(),
                dto.getDireccion(),
                dto.getFotoPerfilPath(),
                dto.getEtiquetaIds(),
                dto.getCamposDinamicos()
        );
    }

    private UpdateContactCommand toUpdateCommand(Long id, ContactoDTO dto) {
        return new UpdateContactCommand(
                id,
                dto.getNombre(),
                dto.getDni(),
                dto.getDireccion(),
                dto.getFotoPerfilPath(),
                dto.getEtiquetaIds(),
                dto.getCamposDinamicos()
        );
    }
}
