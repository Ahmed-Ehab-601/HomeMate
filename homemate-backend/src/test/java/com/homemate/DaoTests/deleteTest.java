package com.homemate.DaoTests;

import com.homemate.Dao.ServiceDaoImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)

public class deleteTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ServiceDaoImpl undertest;

    @Test
    public void testCorrectDeletion() throws Exception {

        long id = 5L;

        when(jdbcTemplate.update(anyString(), eq(id)))
                .thenReturn(1);

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Long.class),
                anyString()
        )).thenThrow(new EmptyResultDataAccessException(1));


        undertest.delete(id);
        assertThrows(
                EmptyResultDataAccessException.class,
                () -> undertest.findIdByName("cleaning")
        );

        verify(jdbcTemplate).update(anyString(), eq(id));

        verify(jdbcTemplate).queryForObject(
                anyString(),
                eq(Long.class),
                eq("cleaning")
        );
    }
}