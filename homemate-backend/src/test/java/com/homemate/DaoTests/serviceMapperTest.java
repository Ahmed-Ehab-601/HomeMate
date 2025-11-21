package com.homemate.DaoTests;

import com.homemate.Dto.ServiceDto;
import com.homemate.Mapper.ServiceMapper;
import com.homemate.Model.ServiceEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class serviceMapperTest {

    @Test
    public void testToDto() {
        ServiceEntity entity = ServiceEntity.builder()
                .id(5L)
                .name("Cleaning")
                .description("Best cleaning service")
                .imageData(null)
                .imageName("img1")
                .imageType("png")
                .build();

        ServiceDto dto = ServiceMapper.mapToDto(entity); // Call static method

        assertNotNull(dto);
        assertEquals(entity.getId(), dto.getId());
        assertEquals(entity.getName(), dto.getName());
        assertEquals(entity.getDescription(), dto.getDescription());
        assertEquals(entity.getImageName(), dto.getImageName());
        assertEquals(entity.getImageType(), dto.getImageType());
        assertEquals(entity.getImageData(), dto.getImageData());
    }

    @Test
    public void testFromDto() {
        ServiceDto dto = ServiceDto.builder()
                .id(5L)
                .name("Plumbing")
                .description("Plumbing service")
                .imageData(null)
                .imageName("img2")
                .imageType("jpg")
                .build();

        ServiceEntity entity = ServiceMapper.mapFromDto(dto); // Call static method

        assertNotNull(entity);
        assertEquals(dto.getId(), entity.getId());
        assertEquals(dto.getName(), entity.getName());
        assertEquals(dto.getDescription(), entity.getDescription());
        assertEquals(dto.getImageName(), entity.getImageName());
        assertEquals(dto.getImageType(), entity.getImageType());
        assertEquals(dto.getImageData(), entity.getImageData());
    }


}
