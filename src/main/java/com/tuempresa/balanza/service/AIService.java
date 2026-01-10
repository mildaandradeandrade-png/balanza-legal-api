package com.tuempresa.balanza.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AIService {

    private static final String MODEL = "gemini-2.5-flash"; // ✅ modelo válido (según docs)
    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    // Puedes ponerlo en application.properties como: gemini.api.key=...
    // o dejarlo vacío y usar variable de entorno GEMINI_API_KEY
    @Value("${gemini.api.key:}")
    private String geminiKeyFromProps;

    public String analizarCaso(String contenido) {
        String apiKey = resolveApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            return dictamenContingencia("No se encontró GEMINI_API_KEY configurada en el servidor.", contenido);
        }

        try {
            String url = String.format(ENDPOINT, MODEL, apiKey);

            // Payload compatible con generateContent
            Map<String, Object> payload = new HashMap<>();
            payload.put("contents", List.of(
                    Map.of("parts", List.of(Map.of("text", buildPrompt(contenido))))
            ));

            String json = mapper.writeValueAsString(payload);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                JsonNode root = mapper.readTree(resp.body());

                // candidates[0].content.parts[0].text
                JsonNode textNode = root.path("candidates")
                        .path(0)
                        .path("content")
                        .path("parts")
                        .path(0)
                        .path("text");

                String salida = textNode.isMissingNode() ? "" : textNode.asText("");

                if (salida.isBlank()) {
                    return dictamenContingencia("La IA respondió sin texto (respuesta vacía).", contenido);
                }
                return salida;
            }

            // Si viene error JSON, lo incluimos para debug
            return dictamenContingencia(
                    "Error HTTP " + resp.statusCode() + " al consultar IA.\n" + resp.body(),
                    contenido
            );

        } catch (Exception e) {
            return dictamenContingencia("Excepción consultando IA: " + e.getMessage(), contenido);
        }
    }

    private String resolveApiKey() {
        if (geminiKeyFromProps != null && !geminiKeyFromProps.isBlank()) return geminiKeyFromProps;
        return System.getenv("GEMINI_API_KEY");
    }

    private String buildPrompt(String contenido) {
        return """
        Actúa como un asistente técnico-legal. Analiza el caso y produce un dictamen estructurado:
        1) Resumen del caso
        2) Hechos relevantes
        3) Problema jurídico
        4) Normativa aplicable (Panamá)
        5) Análisis
        6) Conclusiones
        7) Recomendaciones
        8) Limitaciones

        Caso (texto extraído o escrito por el usuario):
        """ + "\n" + contenido;
    }

    private String dictamenContingencia(String motivo, String contenido) {
        return """
        DICTAMEN TÉCNICO-LEGAL (CONTINGENCIA)

        AVISO:
        No fue posible obtener respuesta del motor de IA.
        Motivo: %s

        1) Resumen del caso
        Se recibió información para análisis; sin embargo, el dictamen automático no pudo completarse.

        2) Hechos relevantes (según lo recibido)
        %s

        3) Recomendaciones
        - Verificar API Key y modelo configurado.
        - Reintentar.
        - Si persiste, revisar cuota/plan o cambiar modelo.

        4) Limitaciones
        Dictamen emitido en modo contingencia por limitación técnica.
        """.formatted(motivo, safePreview(contenido));
    }

    private String safePreview(String contenido) {
        if (contenido == null) return "(sin contenido)";
        String t = contenido.strip();
        if (t.length() <= 1200) return t;
        return t.substring(0, 1200) + "\n...(recortado)...";
    }
}
