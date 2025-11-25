package com.homemate.UserProfile.Services;

import com.homemate.UserProfile.DAO.UserDao;
import com.homemate.UserProfile.DTO.SignupUserDTO;
import com.homemate.UserProfile.Models.User;
import com.homemate.security.service.JwtService;
import org.springframework.stereotype.Service;

@Service
public class UserSignupService {

    UserDao userDao;
    JwtService jwtService;

    public UserSignupService(UserDao userDao, JwtService jwtService) {
        this.userDao = userDao;
        this.jwtService = jwtService;
    }

     public String signup(SignupUserDTO userData) {
         if (userData == null)
             return null;

         User user = getUser(userData);
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
