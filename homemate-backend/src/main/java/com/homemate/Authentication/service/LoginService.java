package com.homemate.Authentication.service;

import com.homemate.TaskerProfile.models.Tasker;
import com.homemate.UserProfile.Models.User;
import com.homemate.TaskerProfile.Dao.TaskerDao;
import com.homemate.UserProfile.DAO.UserDao;
import com.homemate.Authentication.dto.LoginRequestDto;
import com.homemate.Authentication.dto.LoginResponseDto;
import com.homemate.Authentication.dto.PasswordResetDto;
import com.homemate.chat.Service.ChatService;
import com.homemate.security.service.JwtService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.homemate.security.service.ValidateSignupService;

@Service
public class LoginService {
    TaskerDao taskerDao;
    UserDao userDao;
    JwtService jwtService;
    ValidateSignupService validateSignup;
    ChatService chatService;
    public LoginService(
        TaskerDao taskerDao,
         UserDao userDao,
          JwtService jwtService,
          ValidateSignupService validateSignup,
        ChatService chatService
    ) {
        this.taskerDao = taskerDao;
        this.userDao = userDao;
        this.jwtService = jwtService;
        this.validateSignup = validateSignup;
        this.chatService=chatService;
    }

    public LoginResponseDto loginWithEmailPassword(LoginRequestDto loginRequestDto) {

        if (
                loginRequestDto.getPassword() == null ||
                loginRequestDto.getPassword().isEmpty() ||
                loginRequestDto.getEmail() == null ||
                loginRequestDto.getEmail().isEmpty()
                // validateSignup.validateEmail(loginRequestDto.getEmail()) != null
                // validateSignup.validatePassword(loginRequestDto.getPassword()) != null
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
                chatService.setUserOnline(user.getUserID()) ;

                String token = jwtService.generateToken(
                        user.getUserID(),
                        user.getUsername(),
                        user.getEmail(),
                        role
                );

                return new LoginResponseDto(
                    token,
                    role,
                    user.getUsername(),
                    user.getFirstName(),
                    user.getLastName()
                );
            }
            return null;

        } catch (EmptyResultDataAccessException e) {} catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {

                Tasker tasker = taskerDao.getByEmail(email);

                if (Boolean.TRUE.equals(tasker.getIsSuspended())) {
                    return new LoginResponseDto("SUSPENDED","", "","", "");
                }

                if (tasker.getPassword().equals(password)) {
                    String role = "ROLE_TASKER";
                    String token = jwtService.generateToken(
                        tasker.getTaskerID(),
                        tasker.getUsername(),
                        tasker.getEmail(),
                        role
                    );
                    chatService.setTaskerOnline(tasker.getTaskerID());
                    return new LoginResponseDto(
                        token,
                        role,
                        tasker.getUsername(),
                        tasker.getFirstName(),
                        tasker.getLastName()
                    );
                }
            return null;

        } catch (EmptyResultDataAccessException e) {} catch (Exception e) {
            throw new RuntimeException(e);
        }
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
            chatService.setUserOnline(user.getUserID()) ;
                return new LoginResponseDto(
                    token,
                    role,
                    user.getUsername(),
                    user.getFirstName(),
                    user.getLastName()
                );

        } catch (Exception e) {}

        try {

                Tasker tasker = taskerDao.getByEmail(email);

                if (Boolean.TRUE.equals(tasker.getIsSuspended())) {
                    return new LoginResponseDto("SUSPENDED","", "","", "");
                }

                String role = "ROLE_TASKER";
                String token = jwtService.generateToken(
                    tasker.getTaskerID(),
                    tasker.getUsername(),
                    tasker.getEmail(),
                    role
                );
            chatService.setTaskerOnline(tasker.getTaskerID());
                return new LoginResponseDto(
                    token,
                    role,
                    tasker.getUsername(),
                    tasker.getFirstName(),
                    tasker.getLastName()
                );

        } catch (Exception e) {}
        return null;
    }

    public String resetPassword(PasswordResetDto passwordResetDto) {
        // Validate and extract email from verify token
        String email = jwtService.validateVerifyToken(passwordResetDto.getVerifyToken());
        if (email == null) {
            return "Invalid or expired verification token";
        }

        // Validate the new password
        String passwordError = validateSignup.validatePassword(passwordResetDto.getNewPassword());
        if (passwordError != null) {
            return passwordError;
        }

        // Try to find and update user
        try {
            User user = userDao.getByEmail(email);
            if (user != null) {
                userDao.updatePassword(email, passwordResetDto.getNewPassword());
                return null; // Success
            }
        } catch (EmptyResultDataAccessException e) {
            // User not found, try tasker
        } catch (Exception e) {
            return "Error updating password: " + e.getMessage();
        }

        // Try to find and update tasker
        try {
            Tasker tasker = taskerDao.getByEmail(email);
            if (tasker != null) {
                taskerDao.updatePassword(email, passwordResetDto.getNewPassword());
                return null; // Success
            }
        } catch (EmptyResultDataAccessException e) {
            return "No user or tasker found with this email";
        } catch (Exception e) {
            return "Error updating password: " + e.getMessage();
        }

        return "No user or tasker found with this email";
    }
}
