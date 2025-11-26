package com.homemate.TaskerProfile.services;

import com.homemate.TaskerProfile.DTO.TaskerSignupDTO;
import com.homemate.TaskerProfile.Dao.TaskerDao;
import com.homemate.security.service.JwtService;
import org.springframework.stereotype.Service;

@Service
public class TaskerSignupService {

    private final TaskerDao taskerDao;
    private final JwtService jwtService;

    TaskerSignupService(TaskerDao taskerDao, JwtService jwtService) {
        this.taskerDao = taskerDao;
        this.jwtService = jwtService;
    }

    public String registerTasker(TaskerSignupDTO dto) {
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
