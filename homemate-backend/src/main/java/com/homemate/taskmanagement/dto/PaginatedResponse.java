package com.homemate.taskmanagement.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PaginatedResponse {
    private List<TaskCardDto> tasks;
    private int page;
    private int pageSize;
    private Long totalCount;
    private Long totalPages;
}
