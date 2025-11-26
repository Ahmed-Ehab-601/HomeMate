package com.homemate.UserProfile.Services;

import java.util.Objects;

import org.springframework.stereotype.Service;      

import com.homemate.UserProfile.DAO.AddressDao;
import com.homemate.UserProfile.DAO.UserDao;
import com.homemate.UserProfile.DTO.AddressDTO;
import com.homemate.UserProfile.DTO.DateOfBirthDTO;
import com.homemate.UserProfile.DTO.DeleteAccountRequestDTO;
import com.homemate.UserProfile.DTO.EmailDTO;
import com.homemate.UserProfile.DTO.NameDTO;
import com.homemate.UserProfile.DTO.PasswordDTO;
import com.homemate.UserProfile.DTO.PhoneNumberDTO;
import com.homemate.UserProfile.DTO.RemoveAddressDTO;
import com.homemate.UserProfile.DTO.UserProfileDTO;
import com.homemate.UserProfile.DTO.UserRequestTaskerDTO;
import com.homemate.UserProfile.DTO.UsernameDTO;
import com.homemate.UserProfile.Models.User;

@Service
public class UserService {

    private final UserDao userDao;
    private final AddressDao addressDao;
    public UserService(UserDao userDao, AddressDao addressDao) {
        this.userDao = userDao;
        this.addressDao = addressDao;
    }



    public Boolean changePassword(PasswordDTO passwordDTO) {
        Objects.requireNonNull(passwordDTO, "passwordDTO cannot be null");
        
        String oldPassword = passwordDTO.getOldPassword();
        String newPassword = passwordDTO.getNewPassword();
        
        if (oldPassword == null || oldPassword.isEmpty()) {
            throw new IllegalArgumentException("Old password is required.");
        }
        
        if (newPassword == null || newPassword.isEmpty()) {
            throw new IllegalArgumentException("New password is required.");
        }
        
        User user = requireUser(passwordDTO.getUserId());
        
        if (!user.getPassword().equals(oldPassword)) {
            throw new IllegalArgumentException("Old password is incorrect.");
        }
        
        user.setPassword(newPassword);
        userDao.update(user);
        return Boolean.TRUE;
    }
    
    public Boolean changeName(NameDTO nameDTO) {
        final int  max_len = 50;
        User user = requireUser(nameDTO.getUserId());
        if (nameDTO.getNewFirstName().length() > max_len) {
            throw new IllegalArgumentException("New first name is too long.");
        }
        if (nameDTO.getNewLastName().length() > max_len) {
            throw new IllegalArgumentException("New last name is too long.");
        }
        user.setFirstName(nameDTO.getNewFirstName());
        user.setLastName(nameDTO.getNewLastName());
        userDao.update(user);
        return Boolean.TRUE;
    }

    public Boolean changeUsername(UsernameDTO newUsernameDTO) {
        Objects.requireNonNull(newUsernameDTO, "newUsernameDTO cannot be null");
        User user = requireUser(newUsernameDTO.getUserId());
        final int  max_len = 50;

        if (newUsernameDTO.getUsername().length() > max_len) {
            throw new IllegalArgumentException("New Username is too long.");
        }
        user.setUsername(newUsernameDTO.getUsername());
        userDao.update(user);
        return Boolean.TRUE;
    }

    public Boolean changePhoneNumber(PhoneNumberDTO newPhoneNumberDTO) {
        Objects.requireNonNull(newPhoneNumberDTO, "newPhoneNumberDTO cannot be null");
        final int  max_len = 50;

        if (newPhoneNumberDTO.getPhoneNumber().length() > max_len) {
            throw new IllegalArgumentException("New phone number is too long.");
        }
        User user = requireUser(newPhoneNumberDTO.getUserId());
        user.setPhone(newPhoneNumberDTO.getPhoneNumber());
        userDao.update(user);
        return Boolean.TRUE;
    }

        // public UserProfileDTO getData(Long userId) {
        //     Objects.requireNonNull(userId, "userId cannot be null");
        //     return userDao.getProfile(userId);
        // }

    public UserProfileDTO getProfile(Long userID) {
        Objects.requireNonNull(userID, "userID cannot be null");
        return userDao.getProfile(userID);
    }

    public Boolean changeDOB(DateOfBirthDTO newDateOfBirthDTO) {
        Objects.requireNonNull(newDateOfBirthDTO, "newDateOfBirthDTO cannot be null");
        User user = requireUser(newDateOfBirthDTO.getUserId());
        user.setBirthDate(newDateOfBirthDTO.getDateOfBirth());
        userDao.update(user);
        return Boolean.TRUE;
    }

    public Boolean changeEmail(EmailDTO newEmailDTO) {
        Objects.requireNonNull(newEmailDTO, "newEmailDTO cannot be null");
        final int  max_len = 255;

        if (newEmailDTO.getEmail().length() > max_len) {
            throw new IllegalArgumentException("New email is too long.");
        }
        User user = requireUser(newEmailDTO.getUserId());
        user.setEmail(newEmailDTO.getEmail());
        userDao.update(user);
        return Boolean.TRUE;
    }
    public AddressDTO[] getAddresses(Long userID) {
        return addressDao.getByUserID((long) userID).toArray(new AddressDTO[0]);
    }

    public Boolean addAddress(AddressDTO newAddress) {
        Objects.requireNonNull(newAddress, "newAddress cannot be null");
          
        addressDao.addAddress(newAddress);
        return Boolean.TRUE;
    }

    public Boolean removeAddress(RemoveAddressDTO removeAddress) {
        Objects.requireNonNull(removeAddress, "removeAddress cannot be null");
        addressDao.deleteAddress(removeAddress.getAddressId());
        return Boolean.TRUE;
    }

    public Boolean updateAddress(AddressDTO AddressDTO) {
        Objects.requireNonNull(AddressDTO, "updateAddress cannot be null");
        addressDao.updateAddress(AddressDTO);
        return Boolean.TRUE;
    }

    public Boolean deleteAccount(DeleteAccountRequestDTO deleteAccountRequest) {
        Objects.requireNonNull(deleteAccountRequest, "deleteAccountRequest cannot be null");
        userDao.delete(deleteAccountRequest.getUserId());
        return Boolean.TRUE;
    }

    public UserRequestTaskerDTO getTaskerProfile(Long taskerID) {
        Objects.requireNonNull(taskerID, "taskerID cannot be null");
        return userDao.getTaskerProfile(taskerID);
    }

    
    private User requireUser(Long userId) {
        Objects.requireNonNull(userId, "userId cannot be null");
        return userDao.getByID(userId);
    }
}