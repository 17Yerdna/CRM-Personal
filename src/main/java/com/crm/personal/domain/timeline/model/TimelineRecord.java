package com.crm.personal.domain.timeline.model;

import com.crm.personal.domain.contact.model.ContactId;
import com.crm.personal.domain.shared.DomainException;

import java.time.LocalDateTime;
import java.util.List;

public record TimelineRecord(
        TimelineRecordId id,
        ContactId contactId,
        TimelineRecordType type,
        String title,
        String contentHtml,
        LocalDateTime date,
        List<MediaAttachment> attachments
) {

    public TimelineRecord {
        if (contactId == null) {
            throw new DomainException("El contacto del timeline es obligatorio");
        }
        if (type == null) {
            throw new DomainException("El tipo de timeline es obligatorio");
        }
        if (title == null || title.isBlank()) {
            throw new DomainException("El titulo del timeline es obligatorio");
        }
        title = title.trim();
        date = date == null ? LocalDateTime.now() : date;
        attachments = attachments == null ? List.of() : List.copyOf(attachments);
    }

    public static TimelineRecord note(ContactId contactId, String title, String safeHtml, LocalDateTime date) {
        return new TimelineRecord(null, contactId, TimelineRecordType.NOTE, title, safeHtml, date, List.of());
    }
}
