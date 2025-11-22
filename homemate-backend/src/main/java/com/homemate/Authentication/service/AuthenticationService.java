package com.homemate.Authentication.service;

import com.homemate.Authentication.Entity.Tasker;
import com.homemate.Authentication.Entity.User;
import com.homemate.Authentication.dao.TaskerDao;
import com.homemate.Authentication.dao.UserDao;
import com.homemate.Authentication.dto.LoginRequestDto;
import com.homemate.Authentication.dto.LoginResponseDto;
import com.homemate.security.service.JwtService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    TaskerDao taskerDao;
    UserDao userDao;
    JwtService jwtService;

    public AuthenticationService(TaskerDao taskerDao, UserDao userDao, JwtService jwtService) {
        this.taskerDao = taskerDao;
        this.userDao = userDao;
        this.jwtService = jwtService;
    }

    public LoginResponseDto login(LoginRequestDto loginRequestDto) {

        try {

            User user = userDao.getUserByEmail(loginRequestDto.getEmail());
            if (user.getPassword().equals(loginRequestDto.getPassword())) {

                String role = "ROLE_USER";
                if (user.isAdmin())
                    role = "ROLE_ADMIN";

                String token = jwtService.generateToken(Long.valueOf(user.getUserID()));

                return new LoginResponseDto(
                        role,
                        user.getUsername(),
                        user.getFirstName(),
                        user.getLastName(),
                        token
                );
            }
            return null;

        } catch (EmptyResultDataAccessException e) {}

        try {

            Tasker tasker = taskerDao.getTaskerByEmail(loginRequestDto.getEmail());
            if (tasker.getPassword().equals(loginRequestDto.getPassword())) {

                String role = "ROLE_TASKER";
                String token = jwtService.generateToken(Long.valueOf(tasker.getTaskerID()));

                return new LoginResponseDto(
                        role,
                        tasker.getUsername(),
                        tasker.getFirstName(),
                        tasker.getLastName(),
                        token
                );
            }
            return null;

        } catch (EmptyResultDataAccessException e) {}

        return null;
    }
}
