package com.homemate.admin.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table("Taskers")
public class Tasker {
    private Long taskerID;
    private String username;
    private String email;
    private String phone;
    private boolean suspended;
    private Double avgRating;
    private String fname;
    private String lname;
    private Double hourRate;
    private Gender gender;
    public enum Gender {
        M,
        F
    }
}