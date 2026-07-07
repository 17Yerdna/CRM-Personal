package com.crm.personal.infrastructure.legacy;

import com.crm.personal.application.dto.EtiquetaDTO;
import com.crm.personal.infrastructure.persistence.model.Etiqueta;
import com.crm.personal.infrastructure.persistence.repository.EtiquetaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EtiquetaService {

    private static final Logger log = LoggerFactory.getLogger(EtiquetaService.class);

    private final EtiquetaRepository etiquetaRepository;

    public EtiquetaService(EtiquetaRepository etiquetaRepository) {
        this.etiquetaRepository = etiquetaRepository;
    }

    @Transactional(readOnly = true)
    public List<Etiqueta> findAll() {
        return etiquetaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Etiqueta> findRoots() {
        return etiquetaRepository.findByPadreIsNull();
    }

    @Transactional(readOnly = true)
    public Etiqueta findById(Long id) {
        return etiquetaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Etiqueta no encontrada: " + id));
    }

    public Etiqueta save(EtiquetaDTO dto) {
        Etiqueta etiqueta = (dto.getId() != null)
                ? findById(dto.getId())
                : new Etiqueta();

        etiqueta.setNombre(dto.getNombre().trim());
        etiqueta.setColorHex(dto.getColorHex() != null ? dto.getColorHex() : "#6C63FF");

        if (dto.getPadreId() != null) {
            etiqueta.setPadre(findById(dto.getPadreId()));
        } else {
            etiqueta.setPadre(null);
        }

        return etiquetaRepository.save(etiqueta);
    }

    public void delete(Long id) {
        Etiqueta etiqueta = findById(id);
        List<Etiqueta> hijos = etiquetaRepository.findByPadre(etiqueta);
        if (!hijos.isEmpty()) {
            throw new IllegalStateException(
                "No se puede eliminar '" + etiqueta.getNombre() +
                "' porque tiene " + hijos.size() + " sub-etiqueta(s).");
        }
        etiquetaRepository.delete(etiqueta);
        log.info("Etiqueta eliminada: {}", etiqueta.getNombre());
    }
}
