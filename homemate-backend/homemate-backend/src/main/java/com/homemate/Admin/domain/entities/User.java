package com.homemate.Admin.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table("Users")
public class User {
    private Long userId;
    private String email;
    private String fName;
    private String lName;
    private String phone;
    private boolean admin;
    private boolean suspended;
    private String username;
    private String status;
    private Gender gender;

    public enum Gender {
        M,
        F,
    }
}