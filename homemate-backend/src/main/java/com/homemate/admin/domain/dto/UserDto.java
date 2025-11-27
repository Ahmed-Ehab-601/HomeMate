package com.homemate.admin.domain.dto;

import com.homemate.admin.domain.entities.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class UserDto {
    private Long userId;
    private String email;
    private String fName;
    private String lName;
    private String phone;
    private boolean admin;
    private String username;
    private boolean suspended;
    private User.Gender gender;

}
