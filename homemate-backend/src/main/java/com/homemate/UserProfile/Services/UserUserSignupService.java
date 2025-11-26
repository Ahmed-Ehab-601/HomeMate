package com.homemate.UserProfile.Services;

import com.homemate.Authentication.dao.TaskerDaoLogin;
import com.homemate.UserProfile.DAO.UserDao;
import com.homemate.UserProfile.DTO.SignupUserDTO;
import com.homemate.UserProfile.Models.User;
import com.homemate.security.service.JwtService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

@Service
public class UserUserSignupService {

    UserDao userDao;
    JwtService jwtService;
    TaskerDaoLogin taskerDaoLogin;

    public UserUserSignupService(UserDao userDao, JwtService jwtService, TaskerDaoLogin taskerDaoLogin) {
        this.userDao = userDao;
        this.jwtService = jwtService;
        this.taskerDaoLogin = taskerDaoLogin;
    }

     public String signup(SignupUserDTO userData) {
         if (userData == null)
             return null;

         User user = getUser(userData);

         try {
            taskerDaoLogin.getTaskerByEmail(user.getEmail());
            return null;
         } catch (EmptyResultDataAccessException e) {}

         Long id = userDao.signup(user);

         if (id == -1) {
             return null;
         }

         return jwtService.generateToken(
                 id,
                 user.getUsername(),
                 user.getEmail(),
                 "ROLE_USER"
         );
     }

    private static User getUser(SignupUserDTO userData) {
        User user = new User();
        user.setUsername(userData.getUsername());
        user.setFirstName(userData.getFirstName());
        user.setLastName(userData.getLastName());
        user.setEmail(userData.getEmail());
        user.setPassword(userData.getPassword());
        user.setBirthDate(userData.getBirthDate());
        user.setGender(userData.getGender());
        user.setPhone(userData.getPhone());
        user.setIsAdmin(Boolean.FALSE);
        user.setIsSuspended(Boolean.FALSE);
        return user;
    }

}
