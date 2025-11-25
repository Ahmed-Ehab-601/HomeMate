package com.homemate.Authentication.service;

import com.homemate.Authentication.Entity.Tasker;
import com.homemate.Authentication.Entity.User;
import com.homemate.Authentication.dao.TaskerDaoLogin;
import com.homemate.Authentication.dao.UserDaoLogin;
import com.homemate.Authentication.dto.LoginRequestDto;
import com.homemate.Authentication.dto.LoginResponseDto;
import com.homemate.security.service.JwtService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

@Service
public class LoginService {
    TaskerDaoLogin taskerDao;
    UserDaoLogin userDaoLogin;
    JwtService jwtService;

    public LoginService(TaskerDaoLogin taskerDao, UserDaoLogin userDaoLogin, JwtService jwtService) {
        this.taskerDao = taskerDao;
        this.userDaoLogin = userDaoLogin;
        this.jwtService = jwtService;
    }

    public LoginResponseDto login(LoginRequestDto loginRequestDto) {

        if (
                loginRequestDto.getPassword() == null ||
                loginRequestDto.getPassword().isEmpty() ||
                loginRequestDto.getEmail() == null ||
                loginRequestDto.getEmail().isEmpty()
        ) return null;

        try {

            User user = userDaoLogin.getUserByEmail(loginRequestDto.getEmail());
            if (user.getPassword().equals(loginRequestDto.getPassword())) {

                String role = "ROLE_USER";
                if (user.isAdmin())
                    role = "ROLE_ADMIN";

                String token = jwtService.generateToken(
                        Long.valueOf(user.getUserID()),
                        user.getUsername(),
                        user.getEmail(),
                        role
                );

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
                String token = jwtService.generateToken(
                        Long.valueOf(tasker.getTaskerID()),
                        tasker.getUsername(),
                        tasker.getEmail(),
                        role
                );

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
