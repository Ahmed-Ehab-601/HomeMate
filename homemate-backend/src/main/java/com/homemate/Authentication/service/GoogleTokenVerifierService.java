package com.homemate.Authentication.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.homemate.Authentication.dto.GoogleUserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleTokenVerifierService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    public GoogleUserDto verify(String idTokenString) {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(clientId))
                .build();

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();

                String sub = payload.getSubject();
                String email = payload.getEmail();
                boolean emailVerified = Boolean.TRUE.equals(payload.getEmailVerified());
                String name = (String) payload.get("name");
                String givenName = (String) payload.get("given_name");
                String familyName = (String) payload.get("family_name");
                String picture = (String) payload.get("picture");
                String locale = (String) payload.get("locale");

                long issuedAt = payload.getIssuedAtTimeSeconds();
                long expiry = payload.getExpirationTimeSeconds();

                if (!emailVerified) {
                    return null;
                }

                return new GoogleUserDto(
                        sub,
                        email,
                        emailVerified,
                        name,
                        givenName,
                        familyName,
                        picture,
                        locale,
                        issuedAt,
                        expiry
                );
            }
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}