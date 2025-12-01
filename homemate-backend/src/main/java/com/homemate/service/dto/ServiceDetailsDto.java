package com.homemate.service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ServiceDetailsDto {
    private ServiceDto service;
    private int completedTasks;
    private int taskers;
}
