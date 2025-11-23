package com.homemate.DaoTests;

import com.homemate.MapRow.ServiceMapRow;
import com.homemate.Model.ServiceEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RowMapTest {

    @Test
    public void testServiceMapRowMapping() throws SQLException {
        // Mock ResultSet
        ResultSet rs = mock(ResultSet.class);

        when(rs.getLong("serviceID")).thenReturn(5L);
        when(rs.getString("name")).thenReturn("CLEANING");
        when(rs.getString("description")).thenReturn("GREAT");
        when(rs.getBytes("imageData")).thenReturn(null);
        when(rs.getString("imageName")).thenReturn("img");
        when(rs.getString("imageType")).thenReturn("jpeg");

        // Map the row
        RowMapper<ServiceEntity> mapper = new ServiceMapRow();
        ServiceEntity service = mapper.mapRow(rs, 1);

        // Assertions
        assertEquals(5L, Objects.requireNonNull(service).getId());
        assertEquals("CLEANING", service.getName());
        assertEquals("GREAT", service.getDescription());
        assertNull(service.getImageData());
        assertEquals("img", service.getImageName());
        assertEquals("jpeg", service.getImageType());
    }
}
