package org.trakket.service.auth;

import lombok.RequiredArgsConstructor;
import org.trakket.repository.OtpRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OtpTxService {
    private final OtpRepository otpRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementAttempts(Long otpId) {
        otpRepository.incrementAttempts(otpId);
    }
}
