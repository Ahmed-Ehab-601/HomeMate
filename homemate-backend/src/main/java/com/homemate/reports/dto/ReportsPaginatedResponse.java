package com.homemate.reports.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ReportsPaginatedResponse<T> {
    private List<T> reportes;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
}
