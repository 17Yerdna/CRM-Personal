package com.crm.personal.infrastructure.persistence.timeline;

import com.crm.personal.domain.contact.model.ContactId;
import com.crm.personal.domain.timeline.model.MediaAttachment;
import com.crm.personal.domain.timeline.model.TimelineRecord;
import com.crm.personal.domain.timeline.model.TimelineRecordId;
import com.crm.personal.domain.timeline.model.TimelineRecordType;
import com.crm.personal.infrastructure.filesystem.LocalMediaStorageAdapter;
import com.crm.personal.infrastructure.persistence.model.Contacto;
import com.crm.personal.infrastructure.persistence.model.MediaAdjunto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class TimelineJpaMapper {

    private final LocalMediaStorageAdapter mediaStorage;

    public TimelineJpaMapper(LocalMediaStorageAdapter mediaStorage) {
        this.mediaStorage = mediaStorage;
    }

    public TimelineRecord toDomain(com.crm.personal.infrastructure.persistence.model.TimelineRecord entity) {
        return new TimelineRecord(
                entity.getId() != null ? new TimelineRecordId(entity.getId()) : null,
                new ContactId(entity.getContacto().getId()),
                entity.getTipo() == com.crm.personal.infrastructure.persistence.model.TimelineRecordType.MEDIA
                        ? TimelineRecordType.MEDIA
                        : TimelineRecordType.NOTE,
                entity.getTitulo(),
                entity.getContenidoHtml(),
                entity.getFecha(),
                entity.getAdjuntos() == null ? List.of() : entity.getAdjuntos().stream().map(this::toDomain).toList()
        );
    }

    public com.crm.personal.infrastructure.persistence.model.TimelineRecord toEntity(TimelineRecord domain) {
        com.crm.personal.infrastructure.persistence.model.TimelineRecord entity = new com.crm.personal.infrastructure.persistence.model.TimelineRecord();
        entity.setId(domain.id() != null ? domain.id().value() : null);
        Contacto contacto = new Contacto();
        contacto.setId(domain.contactId().value());
        entity.setContacto(contacto);
        entity.setTipo(domain.type() == TimelineRecordType.MEDIA
                ? com.crm.personal.infrastructure.persistence.model.TimelineRecordType.MEDIA
                : com.crm.personal.infrastructure.persistence.model.TimelineRecordType.NOTA);
        entity.setTitulo(domain.title());
        entity.setContenidoHtml(domain.contentHtml());
        entity.setFecha(domain.date() != null ? domain.date() : LocalDateTime.now());
        return entity;
    }

    private MediaAttachment toDomain(MediaAdjunto entity) {
        return new MediaAttachment(
                entity.getId(),
                entity.getFilePath(),
                mediaStorage.resolve(entity.getFilePath()),
                entity.getNombreArchivo(),
                entity.getTipoMime(),
                entity.getDescripcion(),
                entity.getFechaCaptura() != null ? entity.getFechaCaptura().atStartOfDay() : null
        );
    }
}
