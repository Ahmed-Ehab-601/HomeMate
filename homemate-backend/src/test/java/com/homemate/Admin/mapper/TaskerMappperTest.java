package com.homemate.Admin.mapper;

import com.homemate.Admin.domain.dto.TaskerDto;
import com.homemate.Admin.domain.entities.Tasker;
import com.homemate.Admin.mappers.imp.TaskerMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import static com.homemate.Admin.TestDataUtil.*;
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
    void testToDto_MapsAllFields() {
        Tasker tasker = createMockTaskers(1).get(0);
        TaskerDto actualDto = taskerMapper.mapTO(tasker);
        assertThat(actualDto).isNotNull();
        assertThat(actualDto.getTaskerID()).isEqualTo(tasker.getTaskerID());
        assertThat(actualDto.getFname()).isEqualTo(tasker.getFname());
        assertThat(actualDto.getLname()).isEqualTo(tasker.getLname());
        assertThat(actualDto.getUsername()).isEqualTo(tasker.getUsername());
        assertThat(actualDto.getEmail()).isEqualTo(tasker.getEmail());
        assertThat(actualDto.isSuspended()).isEqualTo(tasker.isSuspended());
    }
    @Test
    @DisplayName("Should map TaskerDto to Tasker entity correctly")
    void testToEntity_MapsAllFields() {
        TaskerDto taskerDto = createMockTaskerDtos(1).get(0);
        Tasker tasker = taskerMapper.mapFrom(taskerDto);
        assertThat(tasker).isNotNull();
        assertThat(tasker.getFname()).isEqualTo(taskerDto.getFname());
        assertThat(tasker.getLname()).isEqualTo(taskerDto.getLname());
        assertThat(tasker.getUsername()).isEqualTo(taskerDto.getUsername());
        assertThat(tasker.getEmail()).isEqualTo(taskerDto.getEmail());
        assertThat(tasker.getPhone()).isEqualTo(taskerDto.getPhone());
        assertThat(tasker.getGender()).isEqualTo(taskerDto.getGender());
    }


}
