package com.tuempresa.balanza.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "casos_legales")
public class CasoLegal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String pais;
    private String rama;

    // EL CAMBIO CLAVE: Permite textos largos como el del Caso Tinajitas
    @Column(columnDefinition = "TEXT")
    private String hechos;

    // También ponemos TEXT aquí por si la respuesta de la IA es extensa
    @Column(columnDefinition = "TEXT")
    private String respuestaConstitucional;

    private String usuarioId;
    private LocalDateTime fechaCreacion;

    // Constructor vacío requerido por JPA
    public CasoLegal() {
        this.fechaCreacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }

    public String getRama() { return rama; }
    public void setRama(String rama) { this.rama = rama; }

    public String getHechos() { return hechos; }
    public void setHechos(String hechos) { this.hechos = hechos; }

    public String getRespuestaConstitucional() { return respuestaConstitucional; }
    public void setRespuestaConstitucional(String respuestaConstitucional) { 
        this.respuestaConstitucional = respuestaConstitucional; 
    }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { 
        this.fechaCreacion = fechaCreacion; 
    }
}