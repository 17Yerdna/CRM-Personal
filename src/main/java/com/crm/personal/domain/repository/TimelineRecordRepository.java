package com.crm.personal.domain.repository;

import com.crm.personal.domain.model.TimelineRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TimelineRecordRepository extends JpaRepository<TimelineRecord, Long> {

    List<TimelineRecord> findByContactoIdOrderByFechaAsc(Long contactoId);

    @Modifying
    @Transactional
    @Query("DELETE FROM TimelineRecord t WHERE t.contacto.id = :contactoId")
    void deleteByContactoId(@Param("contactoId") Long contactoId);
}
