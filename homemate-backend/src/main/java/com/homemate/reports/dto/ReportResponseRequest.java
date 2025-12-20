package com.homemate.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportResponseRequest {
    @Size(max = 200, message = "message text cannot exceed 200 characters")
    private String message;
}
