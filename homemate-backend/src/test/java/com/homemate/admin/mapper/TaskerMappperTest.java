package com.homemate.admin.mapper;

import com.homemate.admin.domain.dto.TaskerDto;
import com.homemate.admin.domain.entities.Tasker;
import com.homemate.admin.mappers.imp.TaskerMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import static com.homemate.admin.TestDataUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

public class TaskerMappperTest {
    private ModelMapper modelMapper;
    private TaskerMapper taskerMapper;

    @BeforeEach
    void setUp() {
        modelMapper = new ModelMapper();
        taskerMapper = new TaskerMapper(modelMapper);
    }

    @Test
    @DisplayName("Should map User entity to TaskerDto correctly")
    void testToDtoMapsAllFields() {
        Tasker tasker = createMockTaskers(1).get(0);
        TaskerDto actualDto = taskerMapper.mapTO(tasker);
        assertThat(actualDto).isNotNull();
        assertThat(actualDto.getTaskerID()).isEqualTo(tasker.getTaskerID());
        assertThat(actualDto.getFName()).isEqualTo(tasker.getFname());
        assertThat(actualDto.getLName()).isEqualTo(tasker.getLname());
        assertThat(actualDto.getUsername()).isEqualTo(tasker.getUsername());
        assertThat(actualDto.getEmail()).isEqualTo(tasker.getEmail());
        assertThat(actualDto.isSuspended()).isEqualTo(tasker.isSuspended());
    }
    @Test
    @DisplayName("Should map TaskerDto to Tasker entity correctly")
    void testToEntityMapsAllFields() {
        TaskerDto taskerDto = createMockTaskerDtos(1).get(0);
        Tasker tasker = taskerMapper.mapFrom(taskerDto);
        assertThat(tasker).isNotNull();
        assertThat(tasker.getFname()).isEqualTo(taskerDto.getFName());
        assertThat(tasker.getLname()).isEqualTo(taskerDto.getLName());
        assertThat(tasker.getUsername()).isEqualTo(taskerDto.getUsername());
        assertThat(tasker.getEmail()).isEqualTo(taskerDto.getEmail());
        assertThat(tasker.getPhone()).isEqualTo(taskerDto.getPhone());
        assertThat(tasker.getGender()).isEqualTo(taskerDto.getGender());
    }


}
