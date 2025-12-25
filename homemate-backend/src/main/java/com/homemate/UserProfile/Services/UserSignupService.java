package com.homemate.UserProfile.Services;

import com.homemate.TaskerProfile.Dao.TaskerDao;
import com.homemate.UserProfile.DAO.UserDao;
import com.homemate.UserProfile.DTO.SignupUserDTO;
import com.homemate.UserProfile.Models.User;
import com.homemate.hashing.HashingService;
import com.homemate.security.service.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserSignupService {

    private final UserDao userDao;
    private final JwtService jwtService;
    private final TaskerDao taskerDao;
    private final HashingService hashingService;



     public String signup(SignupUserDTO userData) {
         if (userData == null)
             return null;

         User user = getUser(userData);

            try {
                taskerDao.getByEmail(user.getEmail());
                return null;
            } catch (EmptyResultDataAccessException ignored) {}


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

    private  User getUser(SignupUserDTO userData) {
        User user = new User();
        user.setUsername(userData.getUsername());
        user.setFirstName(userData.getFirstName());
        user.setLastName(userData.getLastName());
        user.setEmail(userData.getEmail());
        user.setPassword(hashingService.hashPassword(userData.getPassword()));
        user.setBirthDate(userData.getBirthDate());
        user.setGender(userData.getGender());
        user.setPhone(userData.getPhone());
        user.setIsAdmin(Boolean.FALSE);
        user.setIsSuspended(Boolean.FALSE);
        return user;
    }

}
