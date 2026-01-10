package com.tuempresa.balanza.controller;

import com.tuempresa.balanza.service.AIService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class DictamenController {

    private final AIService aiService;

    public DictamenController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping(value = "/dictamen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String dictamen(
            @RequestPart(value = "archivo", required = false) MultipartFile archivo,
            @RequestPart(value = "texto", required = false) String texto
    ) throws Exception {

        // Si hay texto en textarea, úsalo
        if (texto != null && !texto.trim().isBlank()) {
            return aiService.analizarCaso(texto.trim());
        }

        // Si hay archivo, por ahora SOLO probamos leyendo nombre (tu ya extraes en otra parte)
        if (archivo == null || archivo.isEmpty()) {
            return "LIMITACIÓN PROBATORIA: no se recibió texto ni archivo.";
        }

        // OJO: aquí debes pasar el TEXTO EXTRAÍDO real (docx/pdf)
        // Si aún no lo tienes, al menos prueba con:
        String contenidoExtraido = "Documento recibido: " + archivo.getOriginalFilename();

        return aiService.analizarCaso(contenidoExtraido);
    }
}
