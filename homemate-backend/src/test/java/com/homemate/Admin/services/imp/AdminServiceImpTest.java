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

        
        underTest.getTaskers(taskerFilterDto, page2Request);

        verify(taskerDao, times(1)).findTaskers(anyList(), eq(10L), eq(20L)); // page 2 * size 10 = offset 20
    }

    @Test
    @DisplayName("suspendUser")
    void testSuspendUser() {
        User before = createMockUsers(1).get(0);
        before.setUserId(2L);
        before.setSuspended(false);

        User after = createMockUsers(1).get(0);
        after.setUserId(2L);
        after.setSuspended(true);

        SuspendDto suspendDto = SuspendDto.builder().userId(2L).reason("violation").build();

        when(userDao.findById(2L)).thenReturn(before, after);
        when(userDao.updateUserSuspended(2L, true)).thenReturn(true);
        when(userMapper.mapTO(after)).thenReturn(UserDto.builder().userId(2L).suspended(true).build());

        UserDto res = underTest.suspendUser(suspendDto);

        assertThat(res).isNotNull();
        assertThat(res.isSuspended()).isTrue();
        verify(userDao, times(1)).updateUserSuspended(2L, true);
    }

    @Test
    @DisplayName("suspendTasker")
    void testSuspendTasker() {
        Tasker before = createMockTaskers(1).get(0);
        before.setTaskerID(5L);
        before.setSuspended(false);

        Tasker after = createMockTaskers(1).get(0);
        after.setTaskerID(5L);
        after.setSuspended(true);

        SuspendDto dto = SuspendDto.builder().userId(5L).userType("tasker").reason("violation").build();

        when(taskerDao.findById(5L)).thenReturn(before, after);
        when(taskerDao.updateTaskerSuspended(5L, true)).thenReturn(true);
        when(taskerMapper.mapTO(after)).thenReturn(TaskerDto.builder().taskerID(5L).suspended(true).build());

        TaskerDto res = underTest.suspendTasker(dto);

        assertThat(res).isNotNull();
        assertThat(res.isSuspended()).isTrue();
        verify(taskerDao, times(1)).updateTaskerSuspended(5L, true);
    }

    @Test
    @DisplayName("reactiveUser")
    void testReactiveUser() {
        User before = createMockUsers(1).get(0);
        before.setUserId(7L);
        before.setSuspended(true);

        User after = createMockUsers(1).get(0);
        after.setUserId(7L);
        after.setSuspended(false);

        when(userDao.findById(7L)).thenReturn(before, after);
        when(userDao.updateUserSuspended(7L, false)).thenReturn(true);
        when(userMapper.mapTO(after)).thenReturn(UserDto.builder().userId(7L).suspended(false).build());

        UserDto res = underTest.reactiveUser(7L);

        assertThat(res).isNotNull();
        assertThat(res.isSuspended()).isFalse();
        verify(userDao, times(1)).updateUserSuspended(7L, false);
    }
    @Test
    @DisplayName("reactiveTasker")
    void testReactiveTasker() {
        Tasker before = createMockTaskers(1).get(0);
        before.setTaskerID(7L);
        before.setSuspended(true);

        Tasker after = createMockTaskers(1).get(0);
        after.setTaskerID(7L);
        after.setSuspended(false);

        when(taskerDao.findById(7L)).thenReturn(before, after);
        when(taskerDao.updateTaskerSuspended(7L, false)).thenReturn(true);
        when(taskerMapper.mapTO(after)).thenReturn(TaskerDto.builder().taskerID(7L).suspended(false).build());
        TaskerDto res = underTest.reactiveTasker(7L);
        assertThat(res).isNotNull();
        assertThat(res.isSuspended()).isFalse();
        verify(taskerDao, times(1)).updateTaskerSuspended(7L, false);
    }

    @Test
    @DisplayName("promote and demote user")
    void testPromoteAndDemoteUser() {
        User before = createMockUsers(1).get(0);
        before.setUserId(10L);
        before.setAdmin(false);

        User afterPromote = createMockUsers(1).get(0);
        afterPromote.setUserId(10L);
        afterPromote.setAdmin(true);

        when(userDao.findById(10L)).thenReturn(before, afterPromote, afterPromote);
        when(userDao.updateUserAdmin(10L, true)).thenReturn(true);
        when(userMapper.mapTO(afterPromote)).thenReturn(UserDto.builder().userId(10L).admin(true).build());

        UserDto promoted = underTest.promoteUser(10L);
        assertThat(promoted).isNotNull();
        assertThat(promoted.isAdmin()).isTrue();

        // demote
        User afterDemote = createMockUsers(1).get(0);
        afterDemote.setUserId(10L);
        afterDemote.setAdmin(false);

        when(userDao.findById(10L)).thenReturn(afterPromote, afterDemote);
        when(userDao.updateUserAdmin(10L, false)).thenReturn(true);
        when(userMapper.mapTO(afterDemote)).thenReturn(UserDto.builder().userId(10L).admin(false).build());

        UserDto demoted = underTest.demoteUser(10L);
        assertThat(demoted).isNotNull();
        assertThat(demoted.isAdmin()).isFalse();
    }

    @Test
    @DisplayName("suspendUser - null dto returns null and no update")
    void testSuspendUser_NullDto() {
        UserDto res = underTest.suspendUser(null);
        assertThat(res).isNull();
        verify(userDao, times(0)).updateUserSuspended(anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("suspendUser - user not found returns null")
    void testSuspendUser_UserNotFound() {
        SuspendDto suspendDto = SuspendDto.builder().userId(99L).reason("x").build();
        when(userDao.findById(99L)).thenReturn(null);

        UserDto res = underTest.suspendUser(suspendDto);
        assertThat(res).isNull();
        verify(userDao, times(0)).updateUserSuspended(anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("suspendUser - update fails returns null")
    void testSuspendUser_UpdateFails() {
        User before = createMockUsers(1).get(0);
        before.setUserId(3L);
        before.setSuspended(false);

        when(userDao.findById(3L)).thenReturn(before);
        when(userDao.updateUserSuspended(3L, true)).thenReturn(false);

        SuspendDto suspendDto = SuspendDto.builder().userId(3L).reason("x").build();
        UserDto res = underTest.suspendUser(suspendDto);
        assertThat(res).isNull();
        verify(userDao, times(1)).updateUserSuspended(3L, true);
    }

    @Test
    @DisplayName("suspendTasker - null dto returns null and no update")
    void testSuspendTasker_NullDto() {
        TaskerDto res = underTest.suspendTasker(null);
        assertThat(res).isNull();
        verify(taskerDao, times(0)).updateTaskerSuspended(anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("suspendTasker - tasker not found returns null")
    void testSuspendTasker_NotFound() {
        SuspendDto dto = SuspendDto.builder().userId(123L).userType("TASKER").reason("x").build();
        when(taskerDao.findById(123L)).thenReturn(null);

        TaskerDto res = underTest.suspendTasker(dto);
        assertThat(res).isNull();
        verify(taskerDao, times(0)).updateTaskerSuspended(anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("suspendTasker - update fails returns null")
    void testSuspendTasker_UpdateFails() {
        Tasker before = createMockTaskers(1).get(0);
        before.setTaskerID(4L);
        before.setSuspended(false);

        when(taskerDao.findById(4L)).thenReturn(before);
        when(taskerDao.updateTaskerSuspended(4L, true)).thenReturn(false);

        SuspendDto dto = SuspendDto.builder().userId(4L).userType("TASKER").reason("x").build();
        TaskerDto res = underTest.suspendTasker(dto);
        assertThat(res).isNull();
        verify(taskerDao, times(1)).updateTaskerSuspended(4L, true);
    }

    @Test
    @DisplayName("reactiveUser - null id returns null and no update")
    void testReactiveUser_NullId() {
        UserDto res = underTest.reactiveUser(null);
        assertThat(res).isNull();
        verify(userDao, times(0)).updateUserSuspended(anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("reactiveUser - user not found returns null")
    void testReactiveUser_NotFound() {
        when(userDao.findById(77L)).thenReturn(null);
        UserDto res = underTest.reactiveUser(77L);
        assertThat(res).isNull();
        verify(userDao, times(0)).updateUserSuspended(anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("reactiveUser - update fails returns null")
    void testReactiveUser_UpdateFails() {
        User before = createMockUsers(1).get(0);
        before.setUserId(8L);
        before.setSuspended(true);

        when(userDao.findById(8L)).thenReturn(before);
        when(userDao.updateUserSuspended(8L, false)).thenReturn(false);

        UserDto res = underTest.reactiveUser(8L);
        assertThat(res).isNull();
        verify(userDao, times(1)).updateUserSuspended(8L, false);
    }

    @Test
    @DisplayName("reactiveTasker - tasker not found returns null")
    void testReactiveTasker_NotFound() {
        when(taskerDao.findById(88L)).thenReturn(null);
        TaskerDto res = underTest.reactiveTasker(88L);
        assertThat(res).isNull();
        verify(taskerDao, times(0)).updateTaskerSuspended(anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("reactiveTasker - update fails returns null")
    void testReactiveTasker_UpdateFails() {
        Tasker before = createMockTaskers(1).get(0);
        before.setTaskerID(9L);
        before.setSuspended(true);

        when(taskerDao.findById(9L)).thenReturn(before);
        when(taskerDao.updateTaskerSuspended(9L, false)).thenReturn(false);

        TaskerDto res = underTest.reactiveTasker(9L);
        assertThat(res).isNull();
        verify(taskerDao, times(1)).updateTaskerSuspended(9L, false);
    }

}


