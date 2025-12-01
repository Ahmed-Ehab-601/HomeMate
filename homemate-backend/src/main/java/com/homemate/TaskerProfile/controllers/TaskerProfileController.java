package com.homemate.TaskerProfile.controllers;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.homemate.TaskerProfile.DTO.AddressCityDTO;
import com.homemate.TaskerProfile.DTO.ChangeAvailabilityDTO;
import com.homemate.TaskerProfile.DTO.ChangeBioDTO;
import com.homemate.TaskerProfile.DTO.ChangeImageDTO;
import com.homemate.TaskerProfile.DTO.EmailDTO;
import com.homemate.TaskerProfile.DTO.HourRateDTO;
import com.homemate.TaskerProfile.DTO.NameDTO;
import com.homemate.TaskerProfile.DTO.PaginatedReviewRequest;
import com.homemate.TaskerProfile.DTO.PaginatedReviewResponse;
import com.homemate.TaskerProfile.DTO.PasswordDTO;
import com.homemate.TaskerProfile.DTO.PhoneNumberDTO;
import com.homemate.TaskerProfile.DTO.TaskerProfileDTO;
import com.homemate.TaskerProfile.DTO.UsernameDTO;
import com.homemate.TaskerProfile.models.Services;
import com.homemate.TaskerProfile.services.TaskerProfileService;
import com.homemate.security.model.AppUserDetails;

@RestController
@RequestMapping("/api/tasker-profile")
public class TaskerProfileController {

    private final TaskerProfileService taskerProfileService;

    public TaskerProfileController(TaskerProfileService taskerProfileService) {
        this.taskerProfileService = taskerProfileService;
    }

    @PutMapping("/image")
    @PreAuthorize("hasRole('ROLE_TASKER')")
    public ResponseEntity<Map<String, String>> changeImage(@AuthenticationPrincipal AppUserDetails taskerDetails,
                                                           @RequestBody ChangeImageDTO changeImageDTO) {
        try{
            changeImageDTO.setTaskerID(taskerDetails.getId());
            taskerProfileService.changeImage(changeImageDTO);
            return okStatus("Image updated");
        }catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    Map.of("error", e.getMessage()),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('ROLE_TASKER')")
    public ResponseEntity<TaskerProfileDTO> getProfile(@AuthenticationPrincipal AppUserDetails taskerDetails) {
        return ResponseEntity.ok(taskerProfileService.getData(taskerDetails.getId()));
    }

    @PostMapping("/reviews")
    @PreAuthorize("hasRole('ROLE_TASKER')")
    public ResponseEntity<PaginatedReviewResponse> getReviews(@AuthenticationPrincipal AppUserDetails taskerDetails,
                                                              @RequestBody PaginatedReviewRequest reviewRequest) {
        reviewRequest.setTaskerID(taskerDetails.getId());
        return ResponseEntity.ok(taskerProfileService.getReviews(reviewRequest));
    }

    @PutMapping("/hour-rate")
    @PreAuthorize("hasRole('ROLE_TASKER')")
    public ResponseEntity<Map<String, String>> changeHourRate(@AuthenticationPrincipal AppUserDetails taskerDetails,
                                                              @RequestBody HourRateDTO hourRateDTO) {
        
        hourRateDTO.setTaskerID(taskerDetails.getId());
        boolean flag =taskerProfileService.changeHourRate(hourRateDTO);
        if (flag){
            return okStatus("Hour rate updated");
        }else{
            return new ResponseEntity<>(
                    Map.of("error", "Hour Rate must be postive."),
                    HttpStatus.BAD_REQUEST
            );
        }
        
    }

    @PutMapping("/service")
    @PreAuthorize("hasRole('ROLE_TASKER')")
    public ResponseEntity<Map<String, String>> changeService(@AuthenticationPrincipal AppUserDetails taskerDetails,
                                                             @RequestParam Long serviceId) {
        taskerProfileService.changeService(taskerDetails.getId(), serviceId);
        return okStatus("Service updated");
    }

    @GetMapping("/services")
    public ResponseEntity<List<Services>> getAvailableServices() {
        return ResponseEntity.ok(taskerProfileService.getAvailableServices());
    }

    @PutMapping("/bio")
    @PreAuthorize("hasRole('ROLE_TASKER')")
    public ResponseEntity<Map<String, String>> changeBio(@AuthenticationPrincipal AppUserDetails taskerDetails,
                                                         @RequestBody ChangeBioDTO changeBioDTO) {
        try{                                                    
            changeBioDTO.setTaskerID(taskerDetails.getId());
            taskerProfileService.changeBio(changeBioDTO);
            return okStatus("Bio updated");
        }catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    Map.of("error", e.getMessage()),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PutMapping("/availability")
    @PreAuthorize("hasRole('ROLE_TASKER')")
    public ResponseEntity<Map<String, String>> changeAvailability(@AuthenticationPrincipal AppUserDetails taskerDetails,
                                                                  @RequestBody ChangeAvailabilityDTO availabilityDTO) {
        availabilityDTO.setTaskerID(taskerDetails.getId());
        taskerProfileService.changeAvailablity(availabilityDTO);
        return okStatus("Availability updated");
    }

    @PutMapping("/password")
    @PreAuthorize("hasRole('ROLE_TASKER')")
    public ResponseEntity<Map<String, String>> changePassword(@AuthenticationPrincipal AppUserDetails taskerDetails,
                                                              @RequestBody PasswordDTO passwordDTO) {
        try {
            passwordDTO.setTaskerID(taskerDetails.getId());
            taskerProfileService.changePassword(passwordDTO);
            return okStatus("Password updated");
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    Map.of("error", e.getMessage()),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PutMapping("/email")
    @PreAuthorize("hasRole('ROLE_TASKER')")
    public ResponseEntity<Map<String, String>> changeEmail(@AuthenticationPrincipal AppUserDetails taskerDetails,
                                                           @RequestBody EmailDTO emailDTO) {
        try{                                                    
            emailDTO.setTaskerID(taskerDetails.getId());
            taskerProfileService.ChangeEmail(emailDTO);
            return okStatus("Email updated");
        }catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    Map.of("error", e.getMessage()),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PutMapping("/username")
    @PreAuthorize("hasRole('ROLE_TASKER')")
    public ResponseEntity<Map<String, String>> changeUsername(@AuthenticationPrincipal AppUserDetails taskerDetails,
                                                              @RequestBody UsernameDTO usernameDTO) {
        try{
            usernameDTO.setTaskerID(taskerDetails.getId());
            taskerProfileService.changeUsername(usernameDTO);
            return okStatus("Username updated");
        }catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    Map.of("error", e.getMessage()),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PutMapping("/phone")
    @PreAuthorize("hasRole('ROLE_TASKER')")
    public ResponseEntity<Map<String, String>> changePhone(@AuthenticationPrincipal AppUserDetails taskerDetails,
                                                           @RequestBody PhoneNumberDTO phoneNumberDTO) {
        try{                                                    
            phoneNumberDTO.setTaskerID(taskerDetails.getId());
            taskerProfileService.changePhoneNumber(phoneNumberDTO);
            return okStatus("Phone number updated");
        }catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    Map.of("error", e.getMessage()),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PutMapping("/name")
    @PreAuthorize("hasRole('ROLE_TASKER')")
    public ResponseEntity<Map<String, String>> changeName(@AuthenticationPrincipal AppUserDetails taskerDetails,
                                                          @RequestBody NameDTO nameDTO) {
        try{
            nameDTO.setTaskerID(taskerDetails.getId());
            taskerProfileService.changeName(nameDTO);
            return okStatus("Name updated");
        }catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    Map.of("error", e.getMessage()),
                    HttpStatus.BAD_REQUEST
            );
        }
    }
    @PutMapping("/city")
    @PreAuthorize("hasRole('ROLE_TASKER')")
    public ResponseEntity<Map<String, String>> changeCity(@AuthenticationPrincipal AppUserDetails taskerDetails,
                                                          @RequestBody AddressCityDTO addressCityDTO) {
        try{
            addressCityDTO.setTaskerID(taskerDetails.getId());
            taskerProfileService.changeAddressCity(addressCityDTO);
            return okStatus("City updated");
        }catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    Map.of("error", e.getMessage()),
                    HttpStatus.BAD_REQUEST
            );
        }
    }


    
    private ResponseEntity<Map<String, String>> okStatus(String message) {
        return ResponseEntity.ok(Collections.singletonMap("status", message));
    }
}
