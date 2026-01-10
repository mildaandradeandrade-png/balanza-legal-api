package com.tuempresa.balanza.service;

import org.springframework.stereotype.Service;

@Service
public class CasoIAService {

    // 🔹 CONSTRUCTOR
    public CasoIAService() {
        // aquí NO va lógica pesada
        // solo inicialización si hace falta
    }

    // 🔹 MÉTODO PRINCIPAL DE IA (mock por ahora)
    public String analizarCaso(String hechos, String pais, String tipoAsunto) {

        // MVP: reglas simples (luego IA real)
        if (hechos.toLowerCase().contains("detencion")) {
            return "Posible violación de derechos fundamentales";
        }

        return "Caso en análisis preliminar";
    }
}
