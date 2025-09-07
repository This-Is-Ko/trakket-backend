package org.trakket.service.email;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.trakket.exception.EmailSendException;
import org.trakket.model.EmailDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public void sendSimpleMail(EmailDetails details) throws EmailSendException {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom(new InternetAddress(senderEmail, "Trakket"));
            helper.setTo(details.getRecipient());
            helper.setSubject(details.getSubject());
            helper.setText(details.getMsgBody(), false);

            javaMailSender.send(mimeMessage);
            log.atInfo().setMessage("Email sent successfully to: " + details.getRecipient()).log();
        } catch (Exception e) {
            log.atError().setMessage("Error while sending Mail: " + Arrays.toString(e.getStackTrace())).log();
            throw new EmailSendException("Failed to send email ", e);
        }
    }

    public void sendSignUpOtpMail(String recipientEmail, String username, String otp) {
        String subject = "Trakket Sign Up - Verification Code";
        String body = String.format(
                "Hello %s,\n\n" +
                        "Thank you for signing up to Trakket.\n\n" +
                        "Your One-Time Password (OTP) is: %s\n\n" +
                        "Please enter this code on the website to complete your registration.\n\n" +
                        "If you did not sign up, please ignore this email.\n\n" +
                        "Thank you\n",
                username,
                otp
        );

        EmailDetails details = new EmailDetails();
        details.setRecipient(recipientEmail);
        details.setSubject(subject);
        details.setMsgBody(body);

        sendSimpleMail(details);
    }

}