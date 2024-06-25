package com.example.demo.controller;

import com.example.demo.util.PdfGenerator;
import com.itextpdf.text.DocumentException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class PdfController {

    @GetMapping("/generatePdf")
    public void generatePdf(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String signature,
            HttpServletResponse response) throws IOException {
        try {
            byte[] pdfBytes = PdfGenerator.generatePdf(name, email, signature);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=documento_firmado.pdf");
            response.setContentLength(pdfBytes.length);

            response.getOutputStream().write(pdfBytes);
            response.getOutputStream().flush();
        } catch (DocumentException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al generar el PDF: " + e.getMessage());
        }
    }
}

