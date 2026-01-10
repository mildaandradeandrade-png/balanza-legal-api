package com.tuempresa.balanza.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class PdfController {

    @PostMapping(value = "/api/pdf/dictamen", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generarPdf(@RequestBody Map<String, String> body) throws Exception {

        String texto = body.getOrDefault("texto", "Sin contenido");

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(doc, page)) {
                content.beginText();
                content.setFont(PDType1Font.HELVETICA, 11);
                content.newLineAtOffset(50, 750);

                // Wrap básico por líneas (evita overflow)
                for (String line : texto.split("\n")) {
                    String safe = line.replace("\t", "    ");
                    while (safe.length() > 110) {
                        content.showText(safe.substring(0, 110));
                        content.newLineAtOffset(0, -14);
                        safe = safe.substring(110);
                    }
                    content.showText(safe);
                    content.newLineAtOffset(0, -14);
                }

                content.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                    ContentDisposition.attachment().filename("dictamen_tecnico.pdf").build()
            );

            return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);
        }
    }
}
