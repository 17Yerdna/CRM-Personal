package com.crm.personal.infrastructure.persistence.repository;

import com.crm.personal.infrastructure.persistence.model.Etiqueta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EtiquetaRepository extends JpaRepository<Etiqueta, Long> {

    List<Etiqueta> findByNombreContainingIgnoreCase(String nombre);
}
