package com.homemate.Authentication.service;

import com.homemate.TaskerProfile.models.Tasker;
import com.homemate.UserProfile.Models.User;
import com.homemate.TaskerProfile.Dao.TaskerDao;
import com.homemate.UserProfile.DAO.UserDao;
import com.homemate.Authentication.dto.LoginRequestDto;
import com.homemate.Authentication.dto.LoginResponseDto;
import com.homemate.security.service.JwtService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

@Service
public class LoginService {
    TaskerDao taskerDao;
    UserDao userDao;
    JwtService jwtService;

    public LoginService(TaskerDao taskerDao, UserDao userDao, JwtService jwtService) {
        this.taskerDao = taskerDao;
        this.userDao = userDao;
        this.jwtService = jwtService;
    }

    public LoginResponseDto loginWithEmailPassword(LoginRequestDto loginRequestDto) {

        if (
                loginRequestDto.getPassword() == null ||
                loginRequestDto.getPassword().isEmpty() ||
                loginRequestDto.getEmail() == null ||
                loginRequestDto.getEmail().isEmpty()
        ) return null;

        return login(loginRequestDto.getEmail(), loginRequestDto.getPassword());
    }

    public LoginResponseDto login(String email, String password) {
        try {

            User user = userDao.getByEmail(email);

            if (Boolean.TRUE.equals(user.getIsSuspended())) {
                return new LoginResponseDto("SUSPENDED","", "","", "");
            }

            if (user.getPassword().equals(password)) {

                String role = "ROLE_USER";
                if (Boolean.TRUE.equals(user.getIsAdmin()))
                    role = "ROLE_ADMIN";

                String token = jwtService.generateToken(
                        user.getUserID(),
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

                Tasker tasker = taskerDao.getByEmail(email);

                if (tasker.getPassword().equals(password)) {

                String role = "ROLE_TASKER";
                String token = jwtService.generateToken(
                    tasker.getTaskerID(),
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

    public LoginResponseDto login(String email) {
        try {
            User user = userDao.getByEmail(email);

            if (Boolean.TRUE.equals(user.getIsSuspended())) {
                return new LoginResponseDto("SUSPENDED","", "","", "");
            }

            String role = "ROLE_USER";
            if (Boolean.TRUE.equals(user.getIsAdmin()))
                role = "ROLE_ADMIN";

            String token = jwtService.generateToken(
                    user.getUserID(),
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

        } catch (EmptyResultDataAccessException e) {}

        try {

                Tasker tasker = taskerDao.getByEmail(email);

                String role = "ROLE_TASKER";
                String token = jwtService.generateToken(
                    tasker.getTaskerID(),
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

        } catch (EmptyResultDataAccessException e) {}
        return null;
    }
}
