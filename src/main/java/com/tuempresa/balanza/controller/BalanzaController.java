package com.tuempresa.balanza.controller;

import com.tuempresa.balanza.service.AIService;
import com.tuempresa.balanza.service.FileExtractService;
import com.tuempresa.balanza.service.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/balanza")
public class BalanzaController {

    private final AIService aiService;
    private final FileExtractService fileExtractService;
    private final PdfService pdfService;

    public BalanzaController(AIService aiService,
                             FileExtractService fileExtractService,
                             PdfService pdfService) {
        this.aiService = aiService;
        this.fileExtractService = fileExtractService;
        this.pdfService = pdfService;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

    // ✅ Para mostrar resultado en pantalla (JSON)
    @PostMapping(
            value = "/analizar-texto",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> analizarTexto(
            @RequestPart(value = "texto", required = false) String texto,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        String contenido = obtenerContenido(texto, file);

        String dictamen;
        try {
            dictamen = aiService.analizarCaso(contenido);
        } catch (Exception e) {
            dictamen = dictamenContingencia("Error llamando IA: " + e.getMessage(), contenido);
        }

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "dictamen", dictamen
        ));
    }

    // ✅ Para descargar PDF (BLOB)
    @PostMapping(
            value = "/analizar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<byte[]> analizarPdf(
            @RequestPart(value = "texto", required = false) String texto,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        String contenido = obtenerContenido(texto, file);

        String dictamen;
        try {
            dictamen = aiService.analizarCaso(contenido);
        } catch (Exception e) {
            dictamen = dictamenContingencia("Error llamando IA: " + e.getMessage(), contenido);
        }

        byte[] pdfBytes = pdfService.generarDictamenPdf(dictamen);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=dictamen_balanza.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    // ---------------- helpers ----------------

    private String obtenerContenido(String texto, MultipartFile file) {
        String contenido = "";

        if (texto != null && !texto.trim().isEmpty()) {
            contenido = texto.trim();
        }

        if (file != null && !file.isEmpty()) {
            String extraido = fileExtractService.extraerTexto(file);
            if (extraido != null && !extraido.trim().isEmpty()) {
                contenido = extraido.trim();
            }
        }

        if (contenido.isBlank()) {
            contenido = "LIMITACIÓN PROBATORIA\n\nNo se recibió texto ni archivo válido para análisis.";
        }

        return contenido;
    }

    private String dictamenContingencia(String motivo, String contenido) {
        return ""
                + "DICTAMEN TÉCNICO-LEGAL (CONTINGENCIA)\n"
                + "Fecha/Hora: " + OffsetDateTime.now() + "\n\n"
                + "AVISO:\n"
                + "No fue posible obtener respuesta del motor de IA.\n"
                + "Motivo: " + motivo + "\n\n"
                + "1) Resumen del caso\n"
                + "Se recibió información para análisis; sin embargo, el dictamen automático no pudo completarse por limitaciones técnicas temporales.\n\n"
                + "2) Hechos relevantes (según lo recibido)\n"
                + "(Extracto/entrada recibida)\n\n"
                + contenido + "\n\n"
                + "8) Limitaciones\n"
                + "Dictamen emitido en modo contingencia por limitación técnica temporal.\n";
    }
}
