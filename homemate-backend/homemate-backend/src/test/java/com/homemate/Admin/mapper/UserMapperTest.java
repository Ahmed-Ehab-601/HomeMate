package com.homemate.Admin.mapper;

import com.homemate.Admin.domain.dto.UserDto;
import com.homemate.Admin.domain.entities.User;
import com.homemate.Admin.mappers.imp.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;


import static com.homemate.Admin.TestDataUtil.createMockUserDtos;
import static com.homemate.Admin.TestDataUtil.createMockUsers;
import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private ModelMapper modelMapper;
    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        modelMapper = new ModelMapper();
        userMapper = new UserMapper(modelMapper);
    }

    @Test
    @DisplayName("Should map User entity to UserDto correctly")
    void testToDto_MapsAllFields() {
        User user = createMockUsers(1).get(0);
        UserDto actualDto = userMapper.mapTO(user);
        assertThat(actualDto).isNotNull();
        assertThat(actualDto.getUserId()).isEqualTo(user.getUserId());
        assertThat(actualDto.getFName()).isEqualTo(user.getFName());
        assertThat(actualDto.getLName()).isEqualTo(user.getLName());
        assertThat(actualDto.getUsername()).isEqualTo(user.getUsername());
        assertThat(actualDto.getEmail()).isEqualTo(user.getEmail());
        assertThat(actualDto.isAdmin()).isEqualTo(user.isAdmin());
        assertThat(actualDto.isSuspended()).isEqualTo(user.isSuspended());
    }
    @Test
    @DisplayName("Should map UserDto to User entity correctly")
    void testToEntity_MapsAllFields() {
        UserDto userDto = createMockUserDtos(1).get(0);
        User actualUser = userMapper.mapFrom(userDto);
        assertThat(actualUser).isNotNull();
        assertThat(actualUser.getFName()).isEqualTo(userDto.getFName());
        assertThat(actualUser.getLName()).isEqualTo(userDto.getLName());
        assertThat(actualUser.getUsername()).isEqualTo(userDto.getUsername());
        assertThat(actualUser.getEmail()).isEqualTo(userDto.getEmail());
        assertThat(actualUser.getPhone()).isEqualTo(userDto.getPhone());
        assertThat(actualUser.getGender()).isEqualTo(userDto.getGender());
    }

}