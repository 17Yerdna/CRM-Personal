package com.crm.personal.infrastructure.persistence.repository;

import com.crm.personal.infrastructure.persistence.model.MediaAdjunto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaAdjuntoRepository extends JpaRepository<MediaAdjunto, Long> {

    List<MediaAdjunto> findByTimelineRecordId(Long timelineRecordId);
}
