package org.trakket.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class CustomJwtAuthenticationConverter implements Converter<Jwt, JwtAuthenticationToken> {

    private final AuthUserDetailsService userDetailsService;

    public CustomJwtAuthenticationConverter(AuthUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public JwtAuthenticationToken convert(Jwt jwt) {
        String email = jwt.getClaimAsString("sub");
        AuthUser authUser = (AuthUser) userDetailsService.loadUserByUsername(email);
        JwtAuthenticationToken token = new JwtAuthenticationToken(jwt, authUser.getAuthorities());
        token.setDetails(authUser.getUser());
        return token;
    }
}
