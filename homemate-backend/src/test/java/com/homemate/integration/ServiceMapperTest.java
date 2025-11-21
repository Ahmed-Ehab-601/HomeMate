package com.homemate.integration;

import com.homemate.Dto.ServiceDto;

import com.homemate.Mapper.ServiceMapper;
import com.homemate.Model.ServiceEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
public class ServiceMapperTest {

    private ServiceMapper serviceMapper;

    @BeforeEach
    void setUp() {
        serviceMapper = new ServiceMapper();
    }

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

        ServiceDto dto = serviceMapper.mapToDto(entity);

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
        ServiceDto Dto = ServiceDto.builder()
                .id(5L)
                .name("Cleaning")
                .description("Best cleaning service")
                .imageData(null)
                .imageName("img1")
                .imageType("png")
                .build();

        ServiceEntity entity1= serviceMapper.mapFromDto(Dto);

        assertNotNull(entity1);
        assertEquals(Dto.getId(), entity1.getId());
        assertEquals(Dto.getName(), entity1.getName());
        assertEquals(Dto.getDescription(), entity1.getDescription());
        assertEquals(Dto.getImageName(), entity1.getImageName());
        assertEquals(Dto.getImageType(), entity1.getImageType());
        assertEquals(Dto.getImageData(), entity1.getImageData());
    }
}
