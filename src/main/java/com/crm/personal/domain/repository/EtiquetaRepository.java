package com.crm.personal.domain.repository;

import com.crm.personal.domain.model.Etiqueta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EtiquetaRepository extends JpaRepository<Etiqueta, Long> {

    /** Etiquetas raíz (sin padre) */
    List<Etiqueta> findByPadreIsNull();

    /** Hijos directos de una etiqueta */
    List<Etiqueta> findByPadre(Etiqueta padre);

    List<Etiqueta> findByNombreContainingIgnoreCase(String nombre);
}
