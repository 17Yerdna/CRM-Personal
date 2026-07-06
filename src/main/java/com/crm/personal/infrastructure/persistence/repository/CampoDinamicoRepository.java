package com.crm.personal.infrastructure.persistence.repository;

import com.crm.personal.infrastructure.persistence.model.CampoDinamico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampoDinamicoRepository extends JpaRepository<CampoDinamico, Long> {

    List<CampoDinamico> findByActivoTrue();

    boolean existsByNombre(String nombre);
}
