package com.homemate.security.service;

import org.springframework.stereotype.Service;

import com.homemate.TaskerProfile.DTO.TaskerSignupDTO;
import com.homemate.UserProfile.DTO.SignupUserDTO;


import java.util.Date;
import java.sql.Timestamp;

@Service
public class ValidateSignupService {
    
    public String validateUserSignup(SignupUserDTO userData) {

        String error = validateUsername(userData.getUsername());
        if (error != null) {
            return error;
        }
        error = validateEmail(userData.getEmail());
        if (error != null) {
            return error;
        }
        error = validatePassword(userData.getPassword());
        if (error != null) {
            return error;
        }
        error = validateBirthDate(userData.getBirthDate());
        if (error != null) {
            return error;
        }
        error = validateGender(userData.getGender());
        if (error != null) {
            return error;
        }
        error = validatePhone(userData.getPhone());
        if (error != null) {
            return error;
        }
        return null;
    }

    public String validateTaskerSignup(TaskerSignupDTO taskerData) {
        String error = validateUsername(taskerData.getUsername());
        if (error != null) {
            return error;
        }
        error = validateEmail(taskerData.getEmail());
        if (error != null) {
            return error;
        }
        error = validatePassword(taskerData.getPassword());
        if (error != null) {
            return error;
        }
        error = validatePhone(taskerData.getPhoneNumber());
        if (error != null) {
            return error;
        }
        error = validateHourRate(taskerData.getHourRate());
        if (error != null) {
            return error;
        }
        error = validateBio(taskerData.getBio());
        if (error != null) {
            return error;
        }
        error = validateCity(taskerData.getCity());
        if (error != null) {
            return error;
        }
        error = validateBirthDate(taskerData.getDateOfBirth());
        if (error != null) {
            return error;
        }
        error = validateName(taskerData.getFirstName());
        if (error != null) {
            return error;
        }
        error = validateName(taskerData.getLastName());
        if (error != null) {
            return error;
        }
        error = validateProfileImage(taskerData.getProfileImage());
        if (error != null) {
            return error;
        }

        return null;
    }

    String validateName(String name) {
        if (name == null || name.isEmpty()) {
            return "first name and last name are required";
        }
        if (name.length() > 20 || name.length() < 3) {
            return "name must be between 3 and 200 characters";
        }
        if (!name.matches("^[a-zA-Z]+$")) {
            return "name must contain only letters";
        }
        return null;
    }

    String validateProfileImage(byte[] profileImage) {
        if (profileImage == null) {
            return "Profile image is required";
        }
        if (profileImage.length > 1024 * 1024 * 16) {
            return "Profile image must be less than 16MB";
        }
        return null;
    }

    String validateBio(String bio) {
        if (bio == null || bio.isEmpty()) {
            return "Bio is required";
        }
        if (bio.length() > 500) {
            return "Bio must be less than 500 characters";
        }
        if (bio.length() < 10) {
            return "Bio must be at least 10 characters";
        }
        return null;
    }

    String validateCity(String city) {
        if (city == null || city.isEmpty()) {
            return "City is required";
        }
        if (city.length() > 200 || city.length() < 3) {
            return "City must be between 3 and 200 characters";
        }
        if (!city.matches("^[a-zA-Z]+$")) {
            return "City must contain only letters";
        }
        return null;
    }

    String validateHourRate(Double hourRate) {
        if (hourRate == null) {
            return "Hour rate is required";
        }
        if (hourRate < 0) {
            return "Hour rate must be greater than 0";
        }
        return null;
    }

    String validateGender(Character gender) {
        if (gender == null) {
            return "Gender is required";
        }
        if (gender != 'M' && gender != 'F') {
            return "Gender must be M or F";
        }
        return null;
    }

    String validateBirthDate(Timestamp birthDate) {
        if (birthDate == null) {
            return "Birth date is required";
        }
        if (birthDate.after(new Date())) {
            return "Birth date must be in the past";
        }
        return null;
    }

    public String validateEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "Email is required";
        }
        if (email.length() < 3 || email.length() > 50) {
            return "Email must be between 3 and 50 characters";
        }
        return null;
    }

    String validateUsername(String username) {
        if (username == null || username.isEmpty()) {
            return "Username is required";
        }
        if (username.length() < 3 || username.length() > 25) {
            return "Username must be between 3 and 20 characters";
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            return "Username must contain only letters and numbers";
        }
        return null;
    }

    public String validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return "Password is required";
        }
        if (password.length() < 8 || password.length() > 25) {
            return "password must be between 8 and 25 characters";
        }
        
        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            }
            if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            }
            if (Character.isDigit(c)) {
                hasDigit = true;
            }
            if (!Character.isLetterOrDigit(c)) {
                hasSpecialChar = true;
            }
        }
        if (!hasUpperCase || !hasLowerCase || !hasDigit || !hasSpecialChar) {
            return "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character";
        }
        
        return null;
    }

    String validatePhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return "Phone is required";
        }
        if (phone.length() != 11) {
            return "Phone must be 11 characters";
        }
        if (
            !phone.startsWith("010") && 
            !phone.startsWith("011") && 
            !phone.startsWith("012") && 
            !phone.startsWith("015")
        ) {
            return "Phone must start with 010, 011, 012, or 015";
        }
        return null;
    }

}
