package com.crm.personal.infrastructure.persistence.timeline;

import com.crm.personal.domain.contact.model.ContactId;
import com.crm.personal.domain.timeline.model.TimelineRecord;
import com.crm.personal.domain.timeline.model.TimelineRecordId;
import com.crm.personal.domain.timeline.port.TimelineRepositoryPort;
import com.crm.personal.infrastructure.persistence.repository.TimelineRecordRepository;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional
public class TimelineJpaAdapter implements TimelineRepositoryPort {

    private final TimelineRecordRepository repository;
    private final TimelineJpaMapper mapper;

    public TimelineJpaAdapter(TimelineRecordRepository repository, TimelineJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimelineRecord> findByContactId(ContactId contactId) {
        return repository.findByContactoIdOrderByFechaAsc(contactId.value()).stream()
                .peek(record -> Hibernate.initialize(record.getAdjuntos()))
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public TimelineRecord save(TimelineRecord record) {
        return mapper.toDomain(repository.save(mapper.toEntity(record)));
    }

    @Override
    public void deleteById(TimelineRecordId id) {
        repository.deleteById(id.value());
    }
}
