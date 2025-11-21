package com.homemate.Admin.mappers.imp;

import com.homemate.Admin.domain.dto.UserDto;
import com.homemate.Admin.domain.entities.User;
import com.homemate.Admin.mappers.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class UserMapper implements Mapper<User, UserDto> {
    private final ModelMapper modelMapper;
    public UserMapper (ModelMapper modelMapper){

        this.modelMapper=modelMapper;
    }
    @Override
    public UserDto mapTO(User user) {
        return modelMapper.map(user,UserDto.class);
    }

    @Override
    public User mapFrom(UserDto userDto) {
        return modelMapper.map(userDto, User.class);
    }
}
