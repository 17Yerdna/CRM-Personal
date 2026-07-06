package com.crm.personal.domain.repository;

import com.crm.personal.domain.model.CampoDinamico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampoDinamicoRepository extends JpaRepository<CampoDinamico, Long> {

    List<CampoDinamico> findByActivoTrue();

    boolean existsByNombre(String nombre);
}
