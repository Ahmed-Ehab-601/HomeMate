package com.homemate.Dto;

import com.homemate.Model.ServiceEntity;
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
