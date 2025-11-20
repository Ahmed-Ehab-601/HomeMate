package com.homemate.security.service;

import com.homemate.security.dao.UserDetailsDao;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    UserDetailsDao userDetailsDao;

    public AppUserDetailsService(UserDetailsDao userDetailsDao) {
        this.userDetailsDao = userDetailsDao;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        throw new UnsupportedOperationException("This method is not used for JWT authentication. Use loadUserById.");
    }

    public UserDetails loadUserById(Long userId) throws Exception {

        try {
            return userDetailsDao.findUserDetailsById(userId);
        } catch (EmptyResultDataAccessException e) {
            try {
                return userDetailsDao.findTakserDetailsById(userId);
            } catch (EmptyResultDataAccessException r) {
                throw new Exception("User not found with ID" + userId);
            }
        }

//        return null;
    }
}