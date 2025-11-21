package com.homemate.Admin.services.imp;

import com.homemate.Admin.dao.TaskerDao;
import com.homemate.Admin.dao.UserDao;
import com.homemate.Admin.domain.dto.*;
import com.homemate.Admin.domain.entities.Tasker;
import com.homemate.Admin.domain.entities.User;

import com.homemate.Admin.mappers.imp.TaskerMapper;
import com.homemate.Admin.mappers.imp.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static com.homemate.Admin.TestDataUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminService Unit Tests")
class AdminServiceImpTest {

    @Mock
    private UserDao userDao;

    @Mock
    private TaskerDao taskerDao;

    @Mock
    private UserMapper userMapper;

    @Mock
    private TaskerMapper taskerMapper;

    @InjectMocks
    private AdminServiceImp underTest;

    private PageRequest pageRequest;
    private UserFilterDto userFilterDto;
    private TaskerFilterDto taskerFilterDto;

    @BeforeEach
    void setUp() {
        pageRequest = PageRequest.builder()
                .page(0L)
                .size(10L)
                .build();

        userFilterDto = new UserFilterDto();
        taskerFilterDto = new TaskerFilterDto();
    }

    @Test
    @DisplayName("GetUsers - Should return paginated users when filters are provided")
    void testGetUsers_WithFilters_ReturnsPaginatedUsers() {
        userFilterDto.setAdmin(true);
        userFilterDto.setSuspended(false);
        List<User> mockUsers = createMockUsers(2);
        List<UserDto> mockUserDtos = createMockUserDtos(2);
        Long totalCount = 2L;
        when(userDao.findUsers(anyList(), eq(10L), eq(0L))).thenReturn(mockUsers);
        when(userDao.countUsers(anyList())).thenReturn(totalCount);
        when(userMapper.mapTO(any(User.class))).thenReturn(mockUserDtos.get(0), mockUserDtos.get(1));
        PageResponse<UserDto> result = underTest.getUsers(userFilterDto, pageRequest);
        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getTotalItems()).isEqualTo(2L);
        assertThat(result.getTotalPages()).isEqualTo(1L);
        verify(userDao, times(1)).findUsers(anyList(), eq(10L), eq(0L));
        verify(userDao, times(1)).countUsers(anyList());
        verify(userMapper, times(2)).mapTO(any(User.class));
    }

    @Test
    @DisplayName("GetUsers - Should return empty list when no users found")
    void testGetUsers_WhenNoUsers_ReturnsEmptyList() {

        when(userDao.findUsers(anyList(), any(), any())).thenReturn(new ArrayList<>());
        when(userDao.countUsers(anyList())).thenReturn(0L);

        PageResponse<UserDto> result = underTest.getUsers(userFilterDto, pageRequest);

        assertThat(result).isNotNull();
        assertThat(result.getItems()).isEmpty();
        assertThat(result.getTotalItems()).isEqualTo(0L);
        assertThat(result.getTotalPages()).isEqualTo(0L);
        verify(userDao, times(1)).findUsers(anyList(), eq(10L), eq(0L));
        verify(userDao, times(1)).countUsers(anyList());
        verify(userMapper, times(0)).mapTO(any(User.class));
    }

    @Test
    @DisplayName("GetUsers - Should calculate total pages correctly")
    void testGetUsers_WithLargeDataset_CalculatesTotalPagesCorrectly() {
        List<User> mockUsers = createMockUsers(10);
        Long totalCount = 25L;

        when(userDao.findUsers(anyList(), eq(10L), eq(0L))).thenReturn(mockUsers);
        when(userDao.countUsers(anyList())).thenReturn(totalCount);
        when(userMapper.mapTO(any(User.class))).thenReturn(new UserDto());

        PageResponse<UserDto> result = underTest.getUsers(userFilterDto, pageRequest);

        assertThat(result.getTotalPages()).isEqualTo(3L); // (25 + 10 - 1) / 10 = 3
        assertThat(result.getTotalItems()).isEqualTo(25L);
    }

    @Test
    @DisplayName("GetTaskers - Should return paginated taskers")
    void testGetTaskers_WithFilters_ReturnsPaginatedTaskers() {
        // Given
        taskerFilterDto.setSuspended(false);
        taskerFilterDto.setMinRate(4.5);

        List<Tasker> mockTaskers = createMockTaskers(3);
        List<TaskerDto> mockTaskerDtos = createMockTaskerDtos(3);
        Long totalCount = 3L;

        when(taskerDao.findTaskers(anyList(), eq(10L), eq(0L))).thenReturn(mockTaskers);
        when(taskerDao.countTaskers(anyList())).thenReturn(totalCount);
        when(taskerMapper.mapTO(any(Tasker.class)))
                .thenReturn(mockTaskerDtos.get(0), mockTaskerDtos.get(1), mockTaskerDtos.get(2));


        PageResponse<TaskerDto> result = underTest.getTaskers(taskerFilterDto, pageRequest);


        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(3);
        assertThat(result.getTotalItems()).isEqualTo(3L);
        assertThat(result.getTotalPages()).isEqualTo(1L);

        verify(taskerDao, times(1)).findTaskers(anyList(), eq(10L), eq(0L));
        verify(taskerDao, times(1)).countTaskers(anyList());
        verify(taskerMapper,times(3)).mapTO(any(Tasker.class));
    }

    @Test
    @DisplayName("GetTaskers - Should handle pagination offset correctly")
    void testGetTaskers_WithPage2_CalculatesOffsetCorrectly() {
        PageRequest page2Request = PageRequest.builder()
                .page(2L)
                .size(10L)
                .build();
        Long limit=10L;
        Long offest=2L*10L;
        when(taskerDao.findTaskers(anyList(), eq(limit), eq(offest))).thenReturn(new ArrayList<>());
        when(taskerDao.countTaskers(anyList())).thenReturn(0L);

        // When
        underTest.getTaskers(taskerFilterDto, page2Request);

        // Then
        verify(taskerDao, times(1)).findTaskers(anyList(), eq(10L), eq(20L)); // page 2 * size 10 = offset 20
    }



}


