package com.homemate.TaskerProfile.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.homemate.TaskerProfile.Dao.ReviewDao;
import com.homemate.TaskerProfile.Dao.ServiceDao;
import com.homemate.TaskerProfile.Dao.TaskerDao;
import com.homemate.TaskerProfile.DTO.ChangeAvailabilityDTO;
import com.homemate.TaskerProfile.DTO.ChangeBioDTO;
import com.homemate.TaskerProfile.DTO.ChangeImageDTO;
import com.homemate.TaskerProfile.DTO.DateOfBirthDTO;
import com.homemate.TaskerProfile.DTO.EmailDTO;
import com.homemate.TaskerProfile.DTO.HourRateDTO;
import com.homemate.TaskerProfile.DTO.PaginatedReviewRequest;
import com.homemate.TaskerProfile.DTO.PaginatedReviewResponse;
import com.homemate.TaskerProfile.DTO.PasswordDTO;
import com.homemate.TaskerProfile.DTO.PhoneNumberDTO;
import com.homemate.TaskerProfile.DTO.TaskerProfileDTO;
import com.homemate.TaskerProfile.DTO.UsernameDTO;
import com.homemate.TaskerProfile.models.Service;
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
        try {
            Tasker tasker = taskerDao.getByID(passwordDTO.getTaskerID());
            tasker.setPassword(passwordDTO.getNewPassword());
            taskerDao.update(tasker);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean changeUsername(UsernameDTO usernameDTO) {
        try {
            Tasker tasker = taskerDao.getByID(usernameDTO.getTaskerID());
            tasker.setUsername(usernameDTO.getNewUsername());
            taskerDao.update(tasker);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean changePhoneNumber(PhoneNumberDTO phoneNumberDTO) {
        try {
            Tasker tasker = taskerDao.getByID(phoneNumberDTO.getTaskerID());
            tasker.setPhone(phoneNumberDTO.getNewPhoneNumber());
            taskerDao.update(tasker);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean ChangeEmail(EmailDTO emailDTO) {
        try {
            Tasker tasker = taskerDao.getByID(emailDTO.getTaskerID());
            tasker.setEmail(emailDTO.getNewEmail());
            taskerDao.update(tasker);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean changeDOB(DateOfBirthDTO dateOfBirthDTO) {
        try {
            Tasker tasker = taskerDao.getByID(dateOfBirthDTO.getTaskerID());
            tasker.setBirthDate(dateOfBirthDTO.getNewDateOfBirth());
            taskerDao.update(tasker);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public TaskerProfileDTO getData(Long taskerID) {
        // Note: taskerID should come from authentication context in production
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
        try {
            Tasker tasker = taskerDao.getByID(hourRateDTO.getTaskerID());
            tasker.setHourrate(hourRateDTO.getNewHourRate());
            taskerDao.update(tasker);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean changeService(Long taskerID, Long serviceID) {
        try {
            Tasker tasker = taskerDao.getByID(taskerID);
            tasker.setServiceID(serviceID);
            taskerDao.update(tasker);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<Service> getAvailableServices() {
        return serviceDao.getAll();
    }

    public Boolean changeBio(ChangeBioDTO changeBioDTO) {
        try {
            Tasker tasker = taskerDao.getByID(changeBioDTO.getTaskerID());
            tasker.setBio(changeBioDTO.getNewBio());
            taskerDao.update(tasker);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean changeAvailablity(ChangeAvailabilityDTO changeAvailabilityDTO) {
        try {
            Tasker tasker = taskerDao.getByID(changeAvailabilityDTO.getTaskerID());
            tasker.setAvailability(changeAvailabilityDTO.getNewAvailability());
            taskerDao.update(tasker);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean changeImage(ChangeImageDTO changeImageDTO) {
        try {
            Tasker tasker = taskerDao.getByID(changeImageDTO.getTaskerID());
            tasker.setImage(changeImageDTO.getNewImage());
            taskerDao.update(tasker);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
