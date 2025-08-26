package org.sportstracker.service;


import org.sportstracker.model.EmailDetails;

public interface EmailService {

    void sendSimpleMail(EmailDetails details);
    void sendSignUpOtpMail(String recipientEmail, String username, String otp);

}