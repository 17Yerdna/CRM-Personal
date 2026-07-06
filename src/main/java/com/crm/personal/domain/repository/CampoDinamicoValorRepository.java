package com.crm.personal.domain.repository;

import com.crm.personal.domain.model.CampoDinamicoValor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CampoDinamicoValorRepository extends JpaRepository<CampoDinamicoValor, Long> {

    List<CampoDinamicoValor> findByContactoId(Long contactoId);

    @Modifying
    @Transactional
    @Query("DELETE FROM CampoDinamicoValor v WHERE v.contacto.id = :contactoId")
    void deleteByContactoId(@Param("contactoId") Long contactoId);
}
