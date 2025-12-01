package com.homemate.service.DaoTests;

import com.homemate.service.dao.ServiceDaoImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeleteTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ServiceDaoImpl undertest;

    @Test
    public void testCorrectDeletion() throws Exception{

        long id = 1L;

        when(jdbcTemplate.update(
                anyString(),
                eq(id)
        )).thenReturn(1);
        undertest.delete(id);
        verify(jdbcTemplate).update(anyString(), eq(id));
    }
}
