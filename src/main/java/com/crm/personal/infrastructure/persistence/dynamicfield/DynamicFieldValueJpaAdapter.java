package com.crm.personal.infrastructure.persistence.dynamicfield;

import com.crm.personal.domain.contact.model.ContactId;
import com.crm.personal.domain.dynamicfield.model.DynamicFieldValue;
import com.crm.personal.domain.dynamicfield.port.DynamicFieldValueRepositoryPort;
import com.crm.personal.infrastructure.persistence.model.CampoDinamico;
import com.crm.personal.infrastructure.persistence.model.CampoDinamicoValor;
import com.crm.personal.infrastructure.persistence.model.Contacto;
import com.crm.personal.infrastructure.persistence.repository.CampoDinamicoValorRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class DynamicFieldValueJpaAdapter implements DynamicFieldValueRepositoryPort {

    private final CampoDinamicoValorRepository repository;

    public DynamicFieldValueJpaAdapter(CampoDinamicoValorRepository repository) {
        this.repository = repository;
    }

    @Override
    public DynamicFieldValue save(DynamicFieldValue value) {
        Contacto contacto = new Contacto();
        ContactId contactId = value.contactId();
        if (contactId != null) {
            contacto.setId(contactId.value());
        }

        CampoDinamico campo = new CampoDinamico();
        campo.setId(value.fieldId().value());

        CampoDinamicoValor entity = new CampoDinamicoValor();
        entity.setContacto(contacto);
        entity.setCampo(campo);
        entity.setValor(value.value());

        CampoDinamicoValor saved = repository.save(entity);

        return new DynamicFieldValue(
                new ContactId(saved.getContacto().getId()),
                value.fieldId(),
                saved.getValor()
        );
    }
}
