package com.homemate.TaskerProfile.services;

import com.homemate.Authentication.dao.UserDaoLogin;
import com.homemate.TaskerProfile.DTO.TaskerSignupDTO;
import com.homemate.TaskerProfile.Dao.TaskerDao;
import com.homemate.UserProfile.DAO.UserDao;
import com.homemate.security.service.JwtService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

@Service
public class TaskerSignupService {

    private final TaskerDao taskerDao;
    private final UserDaoLogin userDaoLogin;
    private final JwtService jwtService;

    TaskerSignupService(TaskerDao taskerDao, JwtService jwtService, UserDaoLogin userDaoLogin) {
        this.taskerDao = taskerDao;
        this.jwtService = jwtService;
        this.userDaoLogin = userDaoLogin;
    }

    public String registerTasker(TaskerSignupDTO dto) {

        try {
            userDaoLogin.getUserByEmail(dto.getEmail());
            return null;
        } catch (EmptyResultDataAccessException e) {}

        Long result = taskerDao.saveTasker(dto);

        if (result == -1) {
            return null;
        }

        return jwtService.generateToken(
                result,
                dto.getUsername(),
                dto.getEmail(),
                "ROLE_TASKER"
        );
    }

}
