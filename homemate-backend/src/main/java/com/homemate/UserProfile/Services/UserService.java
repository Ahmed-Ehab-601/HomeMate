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

    // public String signup(SignupUserDTO userData) {
    //     Objects.requireNonNull(userData, "userData cannot be null");
    //     User user = new User();
    //     user.setUsername(userData.getUsername());
    //     user.setFirstName(userData.getFirstName());
    //     user.setLastName(userData.getLastName());
    //     user.setEmail(userData.getEmail());
    //     user.setPassword(userData.getPassword());
    //     user.setBirthDate(userData.getBirthDate());
    //     user.setGender(userData.getGender());
    //     user.setPhone(userData.getPhone());
    //     user.setIsAdmin(Boolean.TRUE.equals(userData.getAdmin()));
    //     user.setIsSuspended(Boolean.TRUE.equals(userData.getSuspended()));
    //     userDao.signup(user);
    //     return "User created successfully";
    // }

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
        Objects.requireNonNull(nameDTO, "nameDTO cannot be null");
        User user = requireUser(nameDTO.getUserId());
        user.setFirstName(nameDTO.getNewFirstName());
        user.setLastName(nameDTO.getNewLastName());
        userDao.update(user);
        return Boolean.TRUE;
    }

    public Boolean changeUsername(UsernameDTO newUsernameDTO) {
        Objects.requireNonNull(newUsernameDTO, "newUsernameDTO cannot be null");
        User user = requireUser(newUsernameDTO.getUserId());
        user.setUsername(newUsernameDTO.getUsername());
        userDao.update(user);
        return Boolean.TRUE;
    }

    public Boolean changePhoneNumber(PhoneNumberDTO newPhoneNumberDTO) {
        Objects.requireNonNull(newPhoneNumberDTO, "newPhoneNumberDTO cannot be null");
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