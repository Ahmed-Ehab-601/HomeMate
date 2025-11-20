package com.homemate.TaskManagementTests;

import com.homemate.TaskManagement.Dto.TaskRequestDto;
import com.homemate.TaskManagement.mappers.TaskMapper;
import com.homemate.TaskManagement.model.TaskEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TaskMapper.class})
public class TaskMapperTest {

    private  TaskMapper underTest;

   @BeforeEach
   void setUp(){
       this.underTest = new TaskMapper();
   }


    @Test
    void testThatMapperMapRequestDtoToCorrectEntity(){
        TaskRequestDto requestDto = TaskRequestDto.builder().
                description("Task Description").
                serviceID(1L).
                startDate(LocalDateTime.of(2025,12,4,10,20)).
                userID(2L).
                taskerID(1L).
                addressID(2L).
                build();

        TaskEntity taskEntity = TaskEntity.builder().
                description("Task Description").
                serviceID(1L).
                startDate(LocalDateTime.of(2025,12,4,10,20)).
                userID(2L).
                taskerID(1L).
                addressID(2L).
                build();

        TaskEntity result = underTest.getTaskEntity(requestDto);
        assertThat(result).isEqualTo(taskEntity);

    }
}
