package org.trakket.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.trakket.dto.consent.ConsentStatusRequest;
import org.trakket.dto.consent.ConsentStatusResponse;
import org.trakket.model.User;
import org.trakket.service.ConsentService;

@RestController
@RequestMapping("/api/user/consent")
@RequiredArgsConstructor
public class ConsentController {

    private final ConsentService consentService;

    @GetMapping
    public ResponseEntity<ConsentStatusResponse> getConsent(Authentication auth) {
        User user = (User) auth.getDetails();
        ConsentStatusResponse resp = consentService.getConsent(user);
        return resp != null ? ResponseEntity.ok(resp) : ResponseEntity.noContent().build();
    }

    @PutMapping
    public ConsentStatusResponse setConsent(
        Authentication auth,
        @Valid @RequestBody ConsentStatusRequest request
    ) {
        User user = (User) auth.getDetails();
        return consentService.upsertConsent(user, request.status());
    }
}
