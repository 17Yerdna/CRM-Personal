package com.crm.personal.application.service;

import com.crm.personal.application.dto.ContactoDTO;
import com.crm.personal.application.dto.SearchCriteriaDTO;
import com.crm.personal.application.dto.SearchOperator;
import com.crm.personal.infrastructure.persistence.model.*;
import com.crm.personal.infrastructure.persistence.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ContactoService {

    private static final Logger log = LoggerFactory.getLogger(ContactoService.class);

    private final ContactoRepository          contactoRepo;
    private final EtiquetaRepository          etiquetaRepo;
    private final CampoDinamicoValorRepository valorRepo;
    private final CampoDinamicoRepository     campoRepo;

    public ContactoService(ContactoRepository contactoRepo,
                           EtiquetaRepository etiquetaRepo,
                           CampoDinamicoValorRepository valorRepo,
                           CampoDinamicoRepository campoRepo) {
        this.contactoRepo = contactoRepo;
        this.etiquetaRepo = etiquetaRepo;
        this.valorRepo    = valorRepo;
        this.campoRepo    = campoRepo;
    }

    @Transactional(readOnly = true)
    public List<Contacto> findAll() {
        return contactoRepo.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public Contacto findById(Long id) {
        return contactoRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Contacto no encontrado: " + id));
    }

    /**
     * Carga el contacto con todas sus colecciones lazy inicializadas
     * (para mostrarlo en la UI fuera del contexto transaccional).
     */
    @Transactional(readOnly = true)
    public Contacto loadFull(Long id) {
        Contacto c = findById(id);
        Hibernate.initialize(c.getEtiquetas());
        Hibernate.initialize(c.getCamposDinamicos());
        c.getCamposDinamicos().forEach(v -> Hibernate.initialize(v.getCampo()));
        // El timeline se carga por separado via TimelineService
        return c;
    }

    /** Crea un nuevo contacto. */
    public Contacto save(ContactoDTO dto) {
        contactoRepo.findByDni(dto.getDni()).ifPresent(existing -> {
            throw new IllegalArgumentException("Ya existe un contacto con DNI: " + dto.getDni());
        });

        Contacto contacto = Contacto.builder()
                .nombre(dto.getNombre().trim())
                .dni(dto.getDni().trim())
                .direccion(dto.getDireccion().trim())
                .fotoPerfilPath(dto.getFotoPerfilPath())
                .build();

        if (dto.getEtiquetaIds() != null && !dto.getEtiquetaIds().isEmpty()) {
            contacto.setEtiquetas(new HashSet<>(etiquetaRepo.findAllById(dto.getEtiquetaIds())));
        }

        Contacto saved = contactoRepo.save(contacto);
        saveCamposDinamicos(saved, dto.getCamposDinamicos());
        log.info("Contacto creado: {} (DNI: {})", saved.getNombre(), saved.getDni());
        return saved;
    }

    /** Actualiza un contacto existente. */
    public Contacto update(Long id, ContactoDTO dto) {
        Contacto contacto = findById(id);

        // Validar DNI único (excluyendo el propio)
        contactoRepo.findByDni(dto.getDni()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("Ya existe otro contacto con DNI: " + dto.getDni());
            }
        });

        contacto.setNombre(dto.getNombre().trim());
        contacto.setDni(dto.getDni().trim());
        contacto.setDireccion(dto.getDireccion().trim());
        contacto.setFotoPerfilPath(dto.getFotoPerfilPath());

        contacto.getEtiquetas().clear();
        if (dto.getEtiquetaIds() != null && !dto.getEtiquetaIds().isEmpty()) {
            contacto.setEtiquetas(new HashSet<>(etiquetaRepo.findAllById(dto.getEtiquetaIds())));
        }

        Contacto updated = contactoRepo.save(contacto);
        valorRepo.deleteByContactoId(id);
        saveCamposDinamicos(updated, dto.getCamposDinamicos());
        log.info("Contacto actualizado: {} (ID: {})", updated.getNombre(), id);
        return updated;
    }

    public void delete(Long id) {
        Contacto contacto = findById(id);
        contactoRepo.delete(contacto);
        log.info("Contacto eliminado: {} (ID: {})", contacto.getNombre(), id);
    }

    /**
     * Motor de búsqueda combinada: texto libre + etiquetas con operador AND/OR.
     */
    @Transactional(readOnly = true)
    public List<Contacto> search(SearchCriteriaDTO criteria) {
        boolean hasTexto     = criteria.hasTexto();
        boolean hasEtiquetas = criteria.hasEtiquetas();

        if (!hasTexto && !hasEtiquetas) {
            return findAll();
        }

        if (hasTexto && !hasEtiquetas) {
            String txt = criteria.getTexto();
            return contactoRepo.findByNombreContainingIgnoreCaseOrDniContaining(txt, txt);
        }

        if (!hasTexto) {
            List<Long> ids = criteria.getEtiquetaIds();
            return criteria.getOperador() == SearchOperator.AND
                    ? contactoRepo.findByAllEtiquetas(ids, (long) ids.size())
                    : contactoRepo.findByAnyEtiqueta(ids);
        }

        // Texto + etiquetas: filtrar por etiquetas primero, luego por texto en memoria
        List<Long> ids = criteria.getEtiquetaIds();
        List<Contacto> porEtiqueta = criteria.getOperador() == SearchOperator.AND
                ? contactoRepo.findByAllEtiquetas(ids, (long) ids.size())
                : contactoRepo.findByAnyEtiqueta(ids);

        String txt = criteria.getTexto().toLowerCase();
        return porEtiqueta.stream()
                .filter(c -> c.getNombre().toLowerCase().contains(txt)
                          || c.getDni().contains(txt))
                .collect(Collectors.toList());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void saveCamposDinamicos(Contacto contacto, Map<Long, String> camposMap) {
        if (camposMap == null || camposMap.isEmpty()) return;

        camposMap.forEach((campoId, valor) -> {
            if (valor == null || valor.isBlank()) return;
            campoRepo.findById(campoId).ifPresent(campo -> {
                CampoDinamicoValor v = CampoDinamicoValor.builder()
                        .contacto(contacto)
                        .campo(campo)
                        .valor(valor.trim())
                        .build();
                valorRepo.save(v);
            });
        });
    }
}
