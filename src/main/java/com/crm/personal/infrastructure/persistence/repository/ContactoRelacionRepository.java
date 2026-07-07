package com.crm.personal.infrastructure.persistence.repository;

import com.crm.personal.infrastructure.persistence.model.ContactoRelacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactoRelacionRepository extends JpaRepository<ContactoRelacion, Long> {

    @Query("SELECT r FROM ContactoRelacion r WHERE r.contactoOrigen.id = :contactoId OR r.contactoDestino.id = :contactoId")
    List<ContactoRelacion> findByContactoId(@Param("contactoId") Long contactoId);

    void deleteByContactoOrigenIdAndContactoDestinoId(Long origenId, Long destinoId);
}
