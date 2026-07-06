package com.crm.personal.application.service;

import com.crm.personal.infrastructure.persistence.model.*;
import com.crm.personal.infrastructure.persistence.repository.ContactoRepository;
import com.crm.personal.infrastructure.persistence.repository.MediaAdjuntoRepository;
import com.crm.personal.infrastructure.persistence.repository.TimelineRecordRepository;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TimelineService {

    private static final Logger log = LoggerFactory.getLogger(TimelineService.class);

    private final TimelineRecordRepository timelineRepo;
    private final ContactoRepository       contactoRepo;
    private final MediaAdjuntoRepository   mediaRepo;

    public TimelineService(TimelineRecordRepository timelineRepo,
                           ContactoRepository contactoRepo,
                           MediaAdjuntoRepository mediaRepo) {
        this.timelineRepo = timelineRepo;
        this.contactoRepo = contactoRepo;
        this.mediaRepo    = mediaRepo;
    }

    @Transactional(readOnly = true)
    public List<TimelineRecord> getTimeline(Long contactoId) {
        List<TimelineRecord> records = timelineRepo.findByContactoIdOrderByFechaAsc(contactoId);
        // Inicializar adjuntos lazy dentro de la transacción
        records.forEach(r -> Hibernate.initialize(r.getAdjuntos()));
        return records;
    }

    /** Agrega una nota de texto enriquecido al expediente. */
    public TimelineRecord addNota(Long contactoId, String titulo, String contenidoHtml) {
        Contacto contacto = contactoRepo.findById(contactoId)
                .orElseThrow(() -> new EntityNotFoundException("Contacto no encontrado: " + contactoId));

        TimelineRecord record = TimelineRecord.builder()
                .contacto(contacto)
                .tipo(TimelineRecordType.NOTA)
                .titulo(titulo != null ? titulo : "Nota")
                .contenidoHtml(contenidoHtml)
                .fecha(LocalDateTime.now())
                .build();

        TimelineRecord saved = timelineRepo.save(record);
        log.info("Nota agregada al timeline del contacto {}", contactoId);
        return saved;
    }

    /** Agrega un registro multimedia con fotos y metadatos. */
    public TimelineRecord addMedia(Long contactoId, String titulo, List<MediaAdjunto> adjuntos) {
        Contacto contacto = contactoRepo.findById(contactoId)
                .orElseThrow(() -> new EntityNotFoundException("Contacto no encontrado: " + contactoId));

        TimelineRecord record = TimelineRecord.builder()
                .contacto(contacto)
                .tipo(TimelineRecordType.MEDIA)
                .titulo(titulo != null ? titulo : "Galería")
                .fecha(LocalDateTime.now())
                .build();

        for (MediaAdjunto adj : adjuntos) {
            adj.setTimelineRecord(record);
            record.getAdjuntos().add(adj);
        }

        TimelineRecord saved = timelineRepo.save(record);
        log.info("Registro media ({} archivos) agregado al contacto {}", adjuntos.size(), contactoId);
        return saved;
    }

    /** Actualiza el contenido de una nota existente. */
    public TimelineRecord updateNota(Long recordId, String titulo, String contenidoHtml) {
        TimelineRecord record = timelineRepo.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("Registro no encontrado: " + recordId));

        if (record.getTipo() != TimelineRecordType.NOTA) {
            throw new IllegalArgumentException("Solo se pueden editar registros de tipo NOTA.");
        }
        record.setTitulo(titulo);
        record.setContenidoHtml(contenidoHtml);
        return timelineRepo.save(record);
    }

    /** Elimina un registro (y sus adjuntos por cascada). */
    public void deleteRecord(Long recordId) {
        timelineRepo.deleteById(recordId);
        log.info("Registro de timeline {} eliminado.", recordId);
    }
}
