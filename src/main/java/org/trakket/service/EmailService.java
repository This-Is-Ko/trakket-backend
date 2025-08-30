package org.trakket.service;


import org.trakket.model.EmailDetails;

public interface EmailService {

    void sendSimpleMail(EmailDetails details);
    void sendSignUpOtpMail(String recipientEmail, String username, String otp);

}