package com.example.demo.util;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;

import java.util.Base64;

public class PdfGenerator {

    public static byte[] generatePdf(String name, String email, String signatureBase64) throws DocumentException {
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            document.add(new Paragraph("Name: " + name));
            document.add(new Paragraph("Email: " + email));

          
            byte[] decodedImg;
            if (signatureBase64.contains(",")) {
                decodedImg = Base64.getDecoder().decode(signatureBase64.split(",")[1]);
            } else {
                decodedImg = Base64.getDecoder().decode(signatureBase64);
            }

            
            try {
                Image signatureImage = Image.getInstance(decodedImg);
                document.add(new Paragraph("Signature:"));
                document.add(signatureImage);
            } catch (Exception e) {
                throw new DocumentException("La cadena base64 no representa una imagen válida: " + e.getMessage());
            }

            document.close();
        } catch (Exception e) {
            throw new DocumentException("Error en la generación del PDF: " + e.getMessage());
        } finally {
            if (document != null && document.isOpen()) {
                document.close();
            }
        }

        return baos.toByteArray();
    }
}

