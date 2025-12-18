package com.homemate.chat.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

    @Data
    @Builder
    public class PaginatedResponse {
        private List<MessageDto> messages;
        private int page;
        private int pageSize;
        private Long totalCount;
        private Long totalPages;
        private boolean hasNext;
        private boolean hasPrevious;
    }
