package com.homemate.admin.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskerFilterDto {
    Boolean suspended;
    @Max(5)
    @Min(0)
    Double minRate;
    @Size(max = 50)
    String username;

}
