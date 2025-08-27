package org.sportstracker.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sportstracker.exception.EmailSendException;
import org.sportstracker.exception.OtpException;
import org.sportstracker.exception.OtpSendException;
import org.sportstracker.model.Otp;
import org.sportstracker.model.User;
import org.sportstracker.repository.OtpRepository;
import org.sportstracker.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepository otpRepository;
    private final OtpTxService otpTxService;
    private final EmailService emailService;

    @Value("${otp.expiry}")
    private Integer otpExpiryMinutes;

    @Transactional
    public void prepareAndSendSignUpOtp(User user) {
        String otp = generateOtp();
        Otp otpEntity = otpRepository.findByUser(user)
                .orElse(new Otp());
        otpEntity.setOtp(otp);
        otpEntity.setUser(user);
        otpEntity.setExpiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes != null ? otpExpiryMinutes : 5));
        otpEntity.setAttempts(0);
        otpEntity = otpRepository.save(otpEntity);

        try {
            emailService.sendSignUpOtpMail(user.getEmail(), user.getUsername(), otp);
        } catch (EmailSendException ex) {
            otpRepository.delete(otpEntity);
            throw new OtpSendException("OTP email sending unsuccessful", ex);
        }
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < 6; i++) otp.append(random.nextInt(10));
        return otp.toString();
    }

    @Transactional(noRollbackFor = OtpException.class)
    public void validateOtp(User user, String providedOtp) {
        Otp otpEntity = otpRepository.findByUser(user)
                .orElseThrow(() -> new OtpException("Invalid challenge."));

        // Check expiry
        if (otpEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new OtpException("Expired OTP.");
        }
        if (otpEntity.getAttempts() >= 3) {
            throw new OtpException("Max attempts reached.");
        }

        if (!otpEntity.getOtp().equals(providedOtp)) {
            // runs in its own REQUIRES_NEW transaction; commits even if outer txn rolls back
            otpTxService.incrementAttempts(otpEntity.getId());
            throw new OtpException("Invalid OTP.");
        }

        otpRepository.delete(otpEntity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementAttempts(Otp otpEntity) {
        otpEntity.setAttempts(otpEntity.getAttempts() + 1);
        otpRepository.save(otpEntity);
    }


    public void deleteExpiredOtps() {
        otpRepository.deleteAll(
                otpRepository.findAll().stream()
                        .filter(otp -> otp.getExpiresAt().isBefore(LocalDateTime.now()))
                        .toList()
        );
    }
}
