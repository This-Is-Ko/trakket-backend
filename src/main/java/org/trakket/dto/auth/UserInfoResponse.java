package org.trakket.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.trakket.dto.consent.ConsentStatusResponse;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoResponse {

    private String email;
    private String username;
    private String role;
    private ConsentStatusResponse consent;

}
