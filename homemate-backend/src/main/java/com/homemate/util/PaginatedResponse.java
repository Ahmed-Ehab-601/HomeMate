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
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
}

