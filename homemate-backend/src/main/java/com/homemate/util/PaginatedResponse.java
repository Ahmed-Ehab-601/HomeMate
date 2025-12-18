package com.homemate.util;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PaginatedResponse<T> {
    private List<T> data;
    private long currentPage;
    private long totalPages;
    private long totalElements;
    private long pageSize;
}

