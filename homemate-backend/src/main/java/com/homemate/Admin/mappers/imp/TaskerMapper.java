package com.homemate.Admin.mappers.imp;

import com.homemate.Admin.domain.dto.TaskerDto;
import com.homemate.Admin.domain.entities.Tasker;
import com.homemate.Admin.mappers.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class TaskerMapper implements Mapper<Tasker, TaskerDto> {
    private final ModelMapper modelMapper;
    public TaskerMapper (ModelMapper modelMapper ){
        this.modelMapper=modelMapper;
    }
    @Override
    public TaskerDto mapTO(Tasker tasker) {
     return this.modelMapper.map(tasker,TaskerDto.class);

    }

    @Override
    public Tasker mapFrom(TaskerDto taskerDto) {
        return this.modelMapper.map(taskerDto,Tasker.class);
    }
}
