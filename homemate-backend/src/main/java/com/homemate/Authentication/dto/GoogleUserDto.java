package com.homemate.Authentication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GoogleUserDto {
    private String sub;           // Google unique id
    private String email;
    private boolean emailVerified;
    private String name;
    private String givenName;
    private String familyName;
    private String picture;
    private String locale;
    private long issuedAt;
    private long expiry;
}
