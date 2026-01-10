package com.tuempresa.balanza.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class FileExtractService {

    public String extraerTexto(MultipartFile file) {
        try {
            String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();

            if (name.endsWith(".docx")) return extraerDocx(file);
            if (name.endsWith(".pdf")) return extraerPdf(file);

            return "LIMITACIÓN PROBATORIA\n\nTipo de archivo no soportado: " + name;

        } catch (Exception e) {
            return "LIMITACIÓN PROBATORIA\n\nError al extraer texto del archivo: " + e.getMessage();
        }
    }

    private String extraerDocx(MultipartFile file) throws Exception {
        try (InputStream is = file.getInputStream();
             XWPFDocument doc = new XWPFDocument(is);
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText();
        }
    }

    private String extraerPdf(MultipartFile file) throws Exception {
        try (InputStream is = file.getInputStream();
             PDDocument pdf = PDDocument.load(is)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(pdf);
        }
    }
}
