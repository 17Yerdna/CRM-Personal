package com.crm.personal.domain.timeline.port;

import com.crm.personal.domain.contact.model.ContactId;
import com.crm.personal.domain.timeline.model.TimelineRecord;
import com.crm.personal.domain.timeline.model.TimelineRecordId;

import java.util.List;

public interface TimelineRepositoryPort {

    List<TimelineRecord> findByContactId(ContactId contactId);
    TimelineRecord save(TimelineRecord record);
    void deleteById(TimelineRecordId id);
}
