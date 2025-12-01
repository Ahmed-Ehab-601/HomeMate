package com.homemate.admin.domain.dto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserFilterDto {
    Boolean admin;
    Boolean suspended;
    @Size(max = 50)
    String username;

}
