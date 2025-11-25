package com.homemate.TaskerProfile.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.homemate.TaskerProfile.DTO.ChangeAvailabilityDTO;
import com.homemate.TaskerProfile.DTO.ChangeBioDTO;
import com.homemate.TaskerProfile.DTO.ChangeImageDTO;
import com.homemate.TaskerProfile.DTO.DateOfBirthDTO;
import com.homemate.TaskerProfile.DTO.EmailDTO;
import com.homemate.TaskerProfile.DTO.HourRateDTO;
import com.homemate.TaskerProfile.DTO.NameDTO;
import com.homemate.TaskerProfile.DTO.PaginatedReviewRequest;
import com.homemate.TaskerProfile.DTO.PaginatedReviewResponse;
import com.homemate.TaskerProfile.DTO.PasswordDTO;
import com.homemate.TaskerProfile.DTO.PhoneNumberDTO;
import com.homemate.TaskerProfile.DTO.TaskerProfileDTO;
import com.homemate.TaskerProfile.DTO.UsernameDTO;
import com.homemate.TaskerProfile.Dao.ReviewDao;
import com.homemate.TaskerProfile.Dao.ServiceDao;
import com.homemate.TaskerProfile.Dao.TaskerDao;
import com.homemate.TaskerProfile.models.Services;
import com.homemate.TaskerProfile.models.Tasker;

@Service
public class TaskerProfileService {
    private final TaskerDao taskerDao;
    private final ServiceDao serviceDao;
    private final ReviewDao reviewDao;

    public TaskerProfileService(TaskerDao taskerDao, ServiceDao serviceDao, ReviewDao reviewDao) {
        this.taskerDao = taskerDao;
        this.serviceDao = serviceDao;
        this.reviewDao = reviewDao;
    }

    public Boolean changePassword(PasswordDTO passwordDTO) {
        final int MAX_LENGTH = 255;
        String oldPassword = passwordDTO.getOldPassword();
        String newPassword = passwordDTO.getNewPassword();
        
        if (oldPassword == null || oldPassword.isEmpty()) {
            throw new IllegalArgumentException("Old password is required.");
        }
        
        if (newPassword == null || newPassword.isEmpty()) {
            throw new IllegalArgumentException("New password is required.");
        }
        
        if (newPassword.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Password must not exceed 255 characters.");
        }
        
        Tasker tasker = taskerDao.getByID(passwordDTO.getTaskerID());
        
        if (!tasker.getPassword().equals(oldPassword)) {
            throw new IllegalArgumentException("Old password is incorrect.");
        }
        
        tasker.setPassword(newPassword);
        taskerDao.update(tasker);
        return true;
        
    }

    public Boolean changeUsername(UsernameDTO usernameDTO) {
        
        final int MAX_LENGTH = 50;
        String newUsername = usernameDTO.getNewUsername();
        if (newUsername.length() > MAX_LENGTH  || newUsername.length() == 0 ) {
            throw new IllegalArgumentException("Username must not exceed 50 characters.");
        }
        Tasker tasker = taskerDao.getByID(usernameDTO.getTaskerID());
        tasker.setUsername(usernameDTO.getNewUsername());
        taskerDao.update(tasker);
        return true;
        
    }

    public Boolean changePhoneNumber(PhoneNumberDTO phoneNumberDTO) {
        final int MAX_LENGTH = 50;
        String newPhoneNumber = phoneNumberDTO.getNewPhoneNumber();
        if (newPhoneNumber.length() > MAX_LENGTH || newPhoneNumber.length() == 0) {
            throw new IllegalArgumentException("Phone number must not exceed 50 characters.");
        }
        Tasker tasker = taskerDao.getByID(phoneNumberDTO.getTaskerID());
        tasker.setPhone(phoneNumberDTO.getNewPhoneNumber());
        taskerDao.update(tasker);
        return true;
    }

    public Boolean ChangeEmail(EmailDTO emailDTO) {
        final int MAX_LENGTH = 50;
        String newEmail = emailDTO.getNewEmail();
        if (newEmail.length() > MAX_LENGTH || newEmail.length() == 0) {
            throw new IllegalArgumentException("Email must not exceed 50 characters.");
        }
        Tasker tasker = taskerDao.getByID(emailDTO.getTaskerID());
        tasker.setEmail(emailDTO.getNewEmail());
        taskerDao.update(tasker);
        return true;
        
    }

    public Boolean changeDOB(DateOfBirthDTO dateOfBirthDTO) {
        Tasker tasker = taskerDao.getByID(dateOfBirthDTO.getTaskerID());
        tasker.setBirthDate(dateOfBirthDTO.getNewDateOfBirth());
        taskerDao.update(tasker);
        return true;
    }

    public TaskerProfileDTO getData(Long taskerID) {
        return taskerDao.getProfile(taskerID);
    }

    public PaginatedReviewResponse getReviews(PaginatedReviewRequest reviewRequest) {
        Long taskerID = reviewRequest.getTaskerID();
        int page = reviewRequest.getPage();
        int pageSize = reviewRequest.getPageSize();
        
        // Calculate offset
        int offset = (page - 1) * pageSize;
        
        // Get total number of reviews
        Long totalReviews = reviewDao.getNumberOfReviews(taskerID);
        
        // Get paginated reviews
        List<com.homemate.TaskerProfile.DTO.ReviewDTO> reviews = 
            reviewDao.getTaskerReviewsPaginated(taskerID, offset, pageSize);
        
        // Calculate total pages
        int totalPages = (int) Math.ceil((double) totalReviews / pageSize);
        
        PaginatedReviewResponse response = new PaginatedReviewResponse();
        response.setReviews(reviews);
        response.setCurrentPage(page);
        response.setPageSize(pageSize);
        response.setTotalReviews(totalReviews);
        response.setTotalPages(totalPages);
        
        return response;
    }

    public Boolean changeHourRate(HourRateDTO hourRateDTO) {
            Tasker tasker = taskerDao.getByID(hourRateDTO.getTaskerID());
            tasker.setHourrate(hourRateDTO.getNewHourRate());
            taskerDao.update(tasker);
            return true;
    }

    public Boolean changeName(NameDTO nameDTO) {
        final int MAX_LENGTH = 50;

        String newFirstName = nameDTO.getNewFirstName();
        String newLastName = nameDTO.getNewLastName();

        if (newFirstName != null && newFirstName.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("First name must not exceed 50 characters.");
        }
        if (newLastName != null && newLastName.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Last name must not exceed 50 characters.");
        }

        Tasker tasker = taskerDao.getByID(nameDTO.getTaskerID());
        tasker.setFirstName(newFirstName);
        tasker.setLastName(newLastName);
        taskerDao.update(tasker);
        return true;
    }

    public Boolean changeService(Long taskerID, Long serviceID) {
            Tasker tasker = taskerDao.getByID(taskerID);
            tasker.setServiceID(serviceID);
            taskerDao.update(tasker);
            return true;
    }

    public List<Services> getAvailableServices() {
        return serviceDao.getAll();
    }

    public Boolean changeBio(ChangeBioDTO changeBioDTO) {

        final int MAX_LENGTH = 500;

        String newBio = changeBioDTO.getNewBio();
        if (newBio.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Bio must not exceed 500 characters.");
        }
        
        Tasker tasker = taskerDao.getByID(changeBioDTO.getTaskerID());
        tasker.setBio(newBio);
        taskerDao.update(tasker);
        return true;
        
    }

    public Boolean changeAvailablity(ChangeAvailabilityDTO changeAvailabilityDTO) {
        Tasker tasker = taskerDao.getByID(changeAvailabilityDTO.getTaskerID());
        tasker.setAvailability(changeAvailabilityDTO.getNewAvailability());
        taskerDao.update(tasker);
        return true;
        
    }

    public Boolean changeImage(ChangeImageDTO changeImageDTO) {

        final long MAX_SIZE = 16L * 1024 * 1024; // 16 MB
    
        byte[] newImage = changeImageDTO.getNewImage();
        if (newImage == null || newImage.length == 0) {
            throw new IllegalArgumentException("Image cannot be empty.");
        }
        if (newImage.length > MAX_SIZE) {
            throw new IllegalArgumentException("Image size exceeds 16MB limit.");
        }
    
        Tasker tasker = taskerDao.getByID(changeImageDTO.getTaskerID());
        tasker.setImage(newImage);
        taskerDao.update(tasker);
    
        return true;
    }
    public Boolean deleteAccount(Long taskerID) {
        taskerDao.delete(taskerID);
        return true;
    }
    
}
