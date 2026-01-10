package com.tuempresa.balanza.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Pesaje {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String usuarioId;
    private Double peso;
    private LocalDateTime fecha = LocalDateTime.now();

    // --- DEBES AGREGAR ESTOS SETTERS MANUALMENTE ---
    
    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public void setPeso(Double peso) {
        this.peso = peso;
    }

    // --- AGREGAR GETTERS PARA CONSULTAR LUEGO ---

    public String getUsuarioId() { return usuarioId; }
    public Double getPeso() { return peso; }
    public Long getId() { return id; }
}