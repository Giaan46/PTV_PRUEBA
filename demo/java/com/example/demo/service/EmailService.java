package com.example.demo.service;





import java.io.File;
import java.nio.charset.StandardCharsets;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.example.demo.model.EmailBody;

@Service
public class EmailService implements IEmailService {

    @Autowired
    private JavaMailSender mailSender;

    public boolean sendEmail(EmailBody emailBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(emailBody.getEmail());
            helper.setText(emailBody.getContent(), true);
            helper.setSubject(emailBody.getSubject());
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

	@Override
	public void sendEmail(String toUser, String subject, String message) {

		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setFrom("gianlucamaida996@gmail.com");
		mailMessage.setText(toUser);
		mailMessage.setSubject(subject);
		mailMessage.setText(message);
		mailSender.send(mailMessage);

	}

	@Override
	public void sendEmailWithFile(String toUser, String subject, String message,File file) {
		MimeMessage mimeMessage = mailSender.createMimeMessage();
		try {
			MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

			mimeMessageHelper.setFrom("gianlucamaida996@gmail.com");
			mimeMessageHelper.setTo(toUser);
			mimeMessageHelper.setSubject(subject);
			mimeMessageHelper.setText(message);
			mimeMessageHelper.addAttachment(file.getName(),file);

			mailSender.send(mimeMessage);

		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}
}