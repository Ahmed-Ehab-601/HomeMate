package com.homemate.TaskerProfile.services;

import com.homemate.TaskerProfile.DTO.TaskerSignupDTO;
import com.homemate.TaskerProfile.Dao.TaskerDao;
import com.homemate.UserProfile.DAO.UserDao;
import com.homemate.security.service.JwtService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

@Service
public class TaskerSignupService {

    private final TaskerDao taskerDao;
    private final UserDao userDao;
    private final JwtService jwtService;

    TaskerSignupService(TaskerDao taskerDao, JwtService jwtService, UserDao userDao) {
        this.taskerDao = taskerDao;
        this.jwtService = jwtService;
        this.userDao = userDao;
    }

    public String registerTasker(TaskerSignupDTO dto) {

        try {
            userDao.getByEmail(dto.getEmail());
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
