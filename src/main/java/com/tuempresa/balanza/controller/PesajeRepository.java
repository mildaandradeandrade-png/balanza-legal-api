package com.tuempresa.balanza.repository;

import com.tuempresa.balanza.model.Pesaje;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PesajeRepository extends JpaRepository<Pesaje, Long> {
    List<Pesaje> findByUsuarioId(String usuarioId); // Busca solo los pesajes del usuario actual
}