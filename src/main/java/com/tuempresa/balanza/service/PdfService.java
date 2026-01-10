package com.tuempresa.balanza.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfService {

    // Ajusta a tu gusto
    private static final PDRectangle PAGE_SIZE = PDRectangle.LETTER; // o A4
    private static final float MARGIN = 50f;
    private static final float FONT_SIZE = 11f;
    private static final float LEADING = 1.35f * FONT_SIZE; // interlineado
    private static final PDFont FONT = PDType1Font.COURIER; // se parece a "dictamen" en consola

    public byte[] generarDictamenPdf(String dictamen) {
        if (dictamen == null) dictamen = "";

        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // Normalizar saltos
            String normalized = dictamen.replace("\r\n", "\n").replace("\r", "\n");

            PDPage page = new PDPage(PAGE_SIZE);
            doc.addPage(page);

            float yStart = PAGE_SIZE.getHeight() - MARGIN;
            float y = yStart;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(FONT, FONT_SIZE);
                cs.newLineAtOffset(MARGIN, y);

                float usableWidth = PAGE_SIZE.getWidth() - 2 * MARGIN;

                // Partimos por párrafos (saltos de línea)
                String[] paragraphs = normalized.split("\n", -1);

                for (int p = 0; p < paragraphs.length; p++) {
                    String para = paragraphs[p];

                    // Línea en blanco
                    if (para.isBlank()) {
                        y = newLine(cs, y, doc, page);
                        if (y < MARGIN + LEADING) {
                            // nueva página
                            PageBundle pb = newPage(doc, cs, yStart);
                            page = pb.page;
                            y = pb.y;
                        }
                        continue;
                    }

                    // Wrap del párrafo
                    List<String> lines = wrapLine(para, FONT, FONT_SIZE, usableWidth);

                    for (String line : lines) {
                        // Si no cabe, nueva página
                        if (y < MARGIN + LEADING) {
                            cs.endText();
                            // cerramos stream actual y creamos nuevo
                            // Nota: para PDFBox, hay que cerrar y abrir un nuevo ContentStream
                            // así que salimos del try y manejamos manualmente con helper:
                            throw new NeedNewPageException(line, lines, p, paragraphs);
                        }

                        cs.showText(line);
                        y = newLine(cs, y, doc, page);
                    }
                }

                cs.endText();
            } catch (NeedNewPageException ex) {
                // Re-intentamos con un render “manual” multi-page sin hacks:
                return renderMultiPage(doc, normalized);
            }

            doc.save(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            // PDF de emergencia en caso de error
            String fallback = "No fue posible generar el PDF.\n\nDetalle: " + e.getMessage();
            return fallback.getBytes();
        }
    }

    // Render robusto multi-page
    private byte[] renderMultiPage(PDDocument doc, String normalized) throws IOException {
        // limpiamos páginas anteriores
        while (doc.getNumberOfPages() > 0) doc.removePage(0);

        float yStart = PAGE_SIZE.getHeight() - MARGIN;
        float usableWidth = PAGE_SIZE.getWidth() - 2 * MARGIN;

        PDPage page = new PDPage(PAGE_SIZE);
        doc.addPage(page);

        PDPageContentStream cs = new PDPageContentStream(doc, page);
        cs.beginText();
        cs.setFont(FONT, FONT_SIZE);
        cs.newLineAtOffset(MARGIN, yStart);

        float y = yStart;

        String[] paragraphs = normalized.split("\n", -1);
        for (String para : paragraphs) {

            if (para.isBlank()) {
                y -= LEADING;
                cs.newLineAtOffset(0, -LEADING);
                if (y < MARGIN + LEADING) {
                    cs.endText();
                    cs.close();
                    PageBundle pb = newPage(doc, yStart);
                    page = pb.page;
                    cs = pb.cs;
                    y = pb.y;
                }
                continue;
            }

            List<String> lines = wrapLine(para, FONT, FONT_SIZE, usableWidth);

            for (String line : lines) {
                cs.showText(line);

                y -= LEADING;
                cs.newLineAtOffset(0, -LEADING);

                if (y < MARGIN + LEADING) {
                    cs.endText();
                    cs.close();
                    PageBundle pb = newPage(doc, yStart);
                    page = pb.page;
                    cs = pb.cs;
                    y = pb.y;
                }
            }
        }

        cs.endText();
        cs.close();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        doc.save(baos);
        return baos.toByteArray();
    }

    private float newLine(PDPageContentStream cs, float y, PDDocument doc, PDPage page) throws IOException {
        y -= LEADING;
        cs.newLineAtOffset(0, -LEADING);
        return y;
    }

    private PageBundle newPage(PDDocument doc, float yStart) throws IOException {
        PDPage newPage = new PDPage(PAGE_SIZE);
        doc.addPage(newPage);

        PDPageContentStream newCs = new PDPageContentStream(doc, newPage);
        newCs.beginText();
        newCs.setFont(FONT, FONT_SIZE);
        newCs.newLineAtOffset(MARGIN, yStart);

        return new PageBundle(newPage, newCs, yStart);
    }

    // (Solo para el primer intento; luego usamos renderMultiPage)
    private PageBundle newPage(PDDocument doc, PDPageContentStream oldCs, float yStart) throws IOException {
        return newPage(doc, yStart);
    }

    private List<String> wrapLine(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        String[] words = text.split("\\s+");

        StringBuilder line = new StringBuilder();
        for (String word : words) {
            String test = line.isEmpty() ? word : line + " " + word;

            float width = font.getStringWidth(test) / 1000f * fontSize;
            if (width <= maxWidth) {
                line.setLength(0);
                line.append(test);
            } else {
                if (!line.isEmpty()) lines.add(line.toString());
                line.setLength(0);
                line.append(word);
            }
        }
        if (!line.isEmpty()) lines.add(line.toString());

        // si vino una línea larguísima sin espacios, la cortamos “a lo bruto”
        if (lines.isEmpty()) lines.add(text);
        return lines;
    }

    private static class PageBundle {
        PDPage page;
        PDPageContentStream cs;
        float y;

        PageBundle(PDPage page, PDPageContentStream cs, float y) {
            this.page = page;
            this.cs = cs;
            this.y = y;
        }

        PageBundle(PDPage page, float y) {
            this.page = page;
            this.y = y;
        }
    }

    private static class NeedNewPageException extends RuntimeException {
        NeedNewPageException(String a, List<String> b, int c, String[] d) { super("NeedNewPage"); }
    }
}
