package com.tuempresa.balanza.repository;

import com.tuempresa.balanza.model.CasoLegal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface CasoRepository extends JpaRepository<CasoLegal, Long> {
    // Busca casos que contengan la palabra clave en hechos o en el dictamen técnico
    @Query("SELECT c FROM CasoLegal c WHERE LOWER(c.hechos) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.respuestaConstitucional) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<CasoLegal> buscarPorPalabraClave(String keyword);
}