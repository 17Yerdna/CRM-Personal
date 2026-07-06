package com.crm.personal.domain.repository;

import com.crm.personal.domain.model.Contacto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactoRepository extends JpaRepository<Contacto, Long> {

    List<Contacto> findAllByOrderByNombreAsc();

    List<Contacto> findByNombreContainingIgnoreCaseOrDniContaining(String nombre, String dni);

    Optional<Contacto> findByDni(String dni);

    /**
     * OR: contacto que tiene AL MENOS UNA de las etiquetas indicadas.
     */
    @Query("SELECT DISTINCT c FROM Contacto c JOIN c.etiquetas e WHERE e.id IN :ids")
    List<Contacto> findByAnyEtiqueta(@Param("ids") List<Long> ids);

    /**
     * AND: contacto que tiene TODAS las etiquetas indicadas.
     */
    @Query("SELECT c FROM Contacto c JOIN c.etiquetas e " +
           "WHERE e.id IN :ids GROUP BY c HAVING COUNT(DISTINCT e.id) = :count")
    List<Contacto> findByAllEtiquetas(@Param("ids") List<Long> ids, @Param("count") long count);
}
