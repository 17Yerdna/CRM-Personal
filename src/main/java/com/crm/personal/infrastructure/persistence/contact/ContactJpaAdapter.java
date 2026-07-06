package com.crm.personal.infrastructure.persistence.contact;

import com.crm.personal.domain.contact.model.Contact;
import com.crm.personal.domain.contact.model.ContactId;
import com.crm.personal.domain.contact.model.Dni;
import com.crm.personal.domain.contact.port.ContactRepositoryPort;
import com.crm.personal.infrastructure.persistence.model.Contacto;
import com.crm.personal.infrastructure.persistence.repository.ContactoRepository;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class ContactJpaAdapter implements ContactRepositoryPort {

    private final ContactoRepository repository;
    private final ContactJpaMapper mapper;

    public ContactJpaAdapter(ContactoRepository repository, ContactJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contact> findAll() {
        return repository.findAllByOrderByNombreAsc().stream()
                .map(this::loadAndMap)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Contact> findById(ContactId id) {
        return repository.findById(id.value()).map(this::loadAndMap);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Contact> findByDni(Dni dni) {
        return repository.findByDni(dni.value()).map(this::loadAndMap);
    }

    @Override
    public Contact save(Contact contact) {
        Contacto entity = mapper.toEntity(contact);
        Contacto saved = repository.save(entity);
        return loadAndMap(saved);
    }

    @Override
    public void deleteById(ContactId id) {
        repository.deleteById(id.value());
    }

    private Contact loadAndMap(Contacto entity) {
        Hibernate.initialize(entity.getEtiquetas());
        Hibernate.initialize(entity.getCamposDinamicos());
        entity.getCamposDinamicos().forEach(value -> Hibernate.initialize(value.getCampo()));
        return mapper.toDomain(entity);
    }
}
