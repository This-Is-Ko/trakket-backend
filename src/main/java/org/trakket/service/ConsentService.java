package org.trakket.service;

import lombok.RequiredArgsConstructor;
import org.trakket.dto.consent.ConsentStatusResponse;
import org.trakket.enums.ConsentStatus;
import org.trakket.model.User;
import org.trakket.model.UserConsent;
import org.trakket.repository.UserConsentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ConsentService {

    private final UserConsentRepository consentRepository;
    private static final int CURRENT_POLICY_VERSION = 1;

    @Transactional
    public ConsentStatusResponse upsertConsent(User user, ConsentStatus status) {
        UserConsent consent = consentRepository.findByUserId(user.getId())
            .orElseGet(() -> {
                UserConsent c = new UserConsent();
                c.setUser(user);
                return c;
            });
        consent.setStatus(status);
        consent.setUpdatedAt(LocalDateTime.now());
        consent.setPolicyVersion(CURRENT_POLICY_VERSION);
        consent = consentRepository.save(consent);
        return toResponse(consent);
    }

    @Transactional(readOnly = true)
    public ConsentStatusResponse getConsent(User user) {
        return consentRepository.findByUserId(user.getId())
            .map(this::toResponse)
            .orElse(null);
    }

    private ConsentStatusResponse toResponse(UserConsent c) {
        return new ConsentStatusResponse(c.getStatus(), c.getUpdatedAt(), c.getPolicyVersion());
    }
}
