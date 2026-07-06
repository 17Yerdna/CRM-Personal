package com.crm.personal.infrastructure.persistence.contact;

import com.crm.personal.domain.contact.model.Contact;
import com.crm.personal.domain.contact.model.ContactId;
import com.crm.personal.domain.contact.model.ContactName;
import com.crm.personal.domain.contact.model.Dni;
import com.crm.personal.domain.contact.model.ProfilePhoto;
import com.crm.personal.domain.dynamicfield.model.DynamicFieldId;
import com.crm.personal.domain.dynamicfield.model.DynamicFieldValue;
import com.crm.personal.domain.tag.model.TagId;
import com.crm.personal.infrastructure.persistence.model.CampoDinamico;
import com.crm.personal.infrastructure.persistence.model.CampoDinamicoValor;
import com.crm.personal.infrastructure.persistence.model.Contacto;
import com.crm.personal.infrastructure.persistence.model.Etiqueta;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class ContactJpaMapper {

    public Contact toDomain(Contacto entity) {
        return new Contact(
                entity.getId() != null ? new ContactId(entity.getId()) : null,
                new ContactName(entity.getNombre()),
                new Dni(entity.getDni()),
                entity.getDireccion(),
                entity.getFotoPerfilPath() != null ? new ProfilePhoto(entity.getFotoPerfilPath()) : null,
                mapTagIds(entity.getEtiquetas()),
                mapDynamicFieldValues(entity.getCamposDinamicos()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public Contacto toEntity(Contact domain) {
        Contacto entity = new Contacto();
        entity.setId(domain.id() != null ? domain.id().value() : null);
        entity.setNombre(domain.name().value());
        entity.setDni(domain.dni().value());
        entity.setDireccion(domain.address());
        entity.setFotoPerfilPath(domain.profilePhoto() != null ? domain.profilePhoto().relativePath() : null);
        entity.setEtiquetas(mapTags(domain.tagIds()));
        entity.setCamposDinamicos(mapDynamicFieldEntities(entity, domain.dynamicFields()));
        return entity;
    }

    private Set<TagId> mapTagIds(Set<Etiqueta> etiquetas) {
        if (etiquetas == null || etiquetas.isEmpty()) {
            return Set.of();
        }

        Set<TagId> ids = new HashSet<>();
        for (Etiqueta etiqueta : etiquetas) {
            ids.add(new TagId(etiqueta.getId()));
        }
        return Set.copyOf(ids);
    }

    private List<DynamicFieldValue> mapDynamicFieldValues(List<CampoDinamicoValor> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        List<DynamicFieldValue> mapped = new ArrayList<>();
        for (CampoDinamicoValor value : values) {
            mapped.add(new DynamicFieldValue(
                    value.getContacto() != null ? new ContactId(value.getContacto().getId()) : null,
                    new DynamicFieldId(value.getCampo().getId()),
                    value.getValor()
            ));
        }
        return List.copyOf(mapped);
    }

    private Set<Etiqueta> mapTags(Set<TagId> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return Set.of();
        }

        Set<Etiqueta> tags = new HashSet<>();
        for (TagId tagId : tagIds) {
            Etiqueta etiqueta = new Etiqueta();
            etiqueta.setId(tagId.value());
            tags.add(etiqueta);
        }
        return tags;
    }

    private List<CampoDinamicoValor> mapDynamicFieldEntities(Contacto contacto, List<DynamicFieldValue> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        List<CampoDinamicoValor> mapped = new ArrayList<>();
        for (DynamicFieldValue value : values) {
            CampoDinamico campo = new CampoDinamico();
            campo.setId(value.fieldId().value());

            CampoDinamicoValor entity = new CampoDinamicoValor();
            entity.setContacto(contacto);
            entity.setCampo(campo);
            entity.setValor(value.value());
            mapped.add(entity);
        }
        return mapped;
    }
}
