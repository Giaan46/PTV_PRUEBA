package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.PdfGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${endpoint.baseurl}")
    private String baseUrl;

    @GetMapping("/")
    public String login() {
        return "login";
    }

    @PostMapping("/form")
    public String form(@RequestParam String userId, @RequestParam String dni, Model model) {
        try {
           
            String endpointUrl = baseUrl + "/ws/accesotec/" + userId + "/" + dni;

            
            URL url = new URL(endpointUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = in.lines().collect(Collectors.joining());
            in.close();

            
            String name = parseNameFromResponse(response);  
            String email = parseEmailFromResponse(response);  


            model.addAttribute("userId", userId);
            model.addAttribute("dni", dni);
            model.addAttribute("name", name);
            model.addAttribute("email", email);

            return "form";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al obtener datos del servidor");
            return "login";  
        }
    }

    private String parseNameFromResponse(String xmlResponse) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(xmlResponse.getBytes()));

        Element root = document.getDocumentElement();
        NodeList registroList = root.getElementsByTagName("Registro");

        if (registroList.getLength() > 0) {
            Element registro = (Element) registroList.item(0);
            return registro.getAttribute("Nombre").trim();
        } else {
            throw new Exception("No se encontró el elemento Registro en la respuesta XML");
        }
    }

    private String parseEmailFromResponse(String xmlResponse) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(xmlResponse.getBytes()));

        Element root = document.getDocumentElement();
        NodeList registroList = root.getElementsByTagName("Registro");

        if (registroList.getLength() > 0) {
            Element registro = (Element) registroList.item(0);
            return registro.getAttribute("Email").trim();
        } else {
            throw new Exception("No se encontró el elemento Registro en la respuesta XML");
        }
    }

    @PostMapping("/submit")
    public String submit(@RequestParam String userId, @RequestParam String dni,
                         @RequestParam String name, @RequestParam String email,
                         @RequestParam String signature, Model model) {
        try {
           
            User newUser = new User();
            newUser.setUserId(userId);
            newUser.setDni(dni);
            newUser.setName(name);
            newUser.setEmail(email);
            newUser.setSignature(signature);
            userRepository.save(newUser);

       
            byte[] pdfContent = PdfGenerator.generatePdf(name, email, signature);

           
            sendEmailWithAttachment(newUser.getEmail(), "Documento Firmado", "Adjunto encontrarás el documento firmado.", pdfContent);

           
            model.addAttribute("message", "Datos guardados correctamente y correo enviado.");
            model.addAttribute("pdfFileName", "documento_firmado.pdf"); 

            return "form";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al procesar los datos y enviar el correo.");
            return "login"; 
        }
    }

    private void sendEmailWithAttachment(String to, String subject, String text, byte[] pdfContent) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text);
        helper.setFrom(fromEmail);  

        ByteArrayDataSource dataSource = new ByteArrayDataSource(pdfContent, "application/pdf");
        helper.addAttachment("documento_firmado.pdf", dataSource);

        javaMailSender.send(message);
    }

}
