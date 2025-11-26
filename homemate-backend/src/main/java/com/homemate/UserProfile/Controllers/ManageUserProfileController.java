package com.homemate.UserProfile.Controllers;

import java.util.Collections;
import java.util.Map;

import com.homemate.security.model.AppUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.homemate.UserProfile.DTO.AddressDTO;
import com.homemate.UserProfile.DTO.DeleteAccountRequestDTO;
import com.homemate.UserProfile.DTO.NameDTO;
import com.homemate.UserProfile.DTO.PasswordDTO;
import com.homemate.UserProfile.DTO.PhoneNumberDTO;
import com.homemate.UserProfile.DTO.RemoveAddressDTO;
import com.homemate.UserProfile.DTO.UserProfileDTO;
import com.homemate.UserProfile.DTO.UserRequestTaskerDTO;
import com.homemate.UserProfile.Services.UserService;

@RestController
@RequestMapping("/api/users")
public class ManageUserProfileController {

    private final UserService userService;

    public ManageUserProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserProfileDTO> getProfile(@AuthenticationPrincipal AppUserDetails userDetails) {
        return ResponseEntity.ok(userService.getProfile(userDetails.getId()));
    }

    @PostMapping("/addresses")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Map<String, Long>> addAddress(@AuthenticationPrincipal AppUserDetails userDetails, @RequestBody AddressDTO newAddress) {
        Long userId = userDetails.getId();
        newAddress.setUserId(userId);
        userService.addAddress(newAddress);
        return ResponseEntity.ok(Collections.singletonMap("userId", userId));
    }

    @DeleteMapping("/addresses/{addressId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Map<String, String>> removeAddress(@AuthenticationPrincipal AppUserDetails userDetails, @PathVariable Long addressId) {
        RemoveAddressDTO removeAddressDTO = new RemoveAddressDTO();
        removeAddressDTO.setUserId(userDetails.getId());
        removeAddressDTO.setAddressId(addressId);
        userService.removeAddress(removeAddressDTO);
        return ResponseEntity.ok(Collections.singletonMap("status", "Address removed"));
    }

    @PutMapping("/addresses/{addressId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Map<String, String>> updateAddress(@AuthenticationPrincipal AppUserDetails userDetails,
                                                             @PathVariable Long addressId,
                                                             @RequestBody AddressDTO updateAddress) {
        updateAddress.setUserId(userDetails.getId());
        updateAddress.setAddressId(addressId);
        userService.updateAddress(updateAddress);
        return ResponseEntity.ok(Collections.singletonMap("status", "Address updated"));
    }

    @DeleteMapping("/account")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Map<String, String>> deleteAccount(@AuthenticationPrincipal AppUserDetails userDetails) {
        DeleteAccountRequestDTO request = new DeleteAccountRequestDTO();
        request.setUserId(userDetails.getId());
        userService.deleteAccount(request);
        return ResponseEntity.ok(Collections.singletonMap("status", "Account deleted"));
    }

    // @PostMapping("/signup")
    // public ResponseEntity<Map<String, String>> signup(@RequestBody SignupUserDTO userSignupDTO) {
    //     String message = userService.signup(userSignupDTO);
    //     return ResponseEntity.status(HttpStatus.CREATED).body(Collections.singletonMap("message", message));
    // }

    @GetMapping("/taskers/{taskerId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<UserRequestTaskerDTO> getTaskerProfile(@PathVariable Long taskerId) {
        UserRequestTaskerDTO tasker = userService.getTaskerProfile(taskerId);
        if (tasker == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tasker);
    }


//    @GetMapping(value = "/addresses", params = "userId")
//    public ResponseEntity<AddressDTO[]> getAddresses(@RequestParam("userId") Integer userID) {
//        return ResponseEntity.ok(userService.getAddresses(userID));
//    }

    @GetMapping("/addresses")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<AddressDTO[]> getAddressesForUser(@AuthenticationPrincipal AppUserDetails userDetails) {
        return ResponseEntity.ok(userService.getAddresses(userDetails.getId()));
    }

    @PutMapping("/name")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Map<String, String>> changeName(@AuthenticationPrincipal AppUserDetails userDetails,
                                                          @RequestBody NameDTO nameDTO) {
        nameDTO.setUserId(userDetails.getId());
        userService.changeName(nameDTO);
        return ResponseEntity.ok(Collections.singletonMap("status", "Name updated"));
    }

    @PutMapping("/password")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Map<String, String>> changePassword(@AuthenticationPrincipal AppUserDetails userDetails,
                                                              @RequestBody PasswordDTO passwordDTO) {
        if (passwordDTO.getOldPassword() == null || passwordDTO.getOldPassword().isEmpty()) {
            throw new IllegalArgumentException("Old password is required.");
        }
        if (passwordDTO.getNewPassword() == null || passwordDTO.getNewPassword().isEmpty()) {
            throw new IllegalArgumentException("New password is required.");
        }
        passwordDTO.setUserId(userDetails.getId());
        userService.changePassword(passwordDTO);
        return ResponseEntity.ok(Collections.singletonMap("status", "Password updated"));
    }

    @PutMapping("/phone")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Map<String, String>> changePhone(@AuthenticationPrincipal AppUserDetails userDetails,
                                                            @RequestBody PhoneNumberDTO phoneNumberDTO) {
        phoneNumberDTO.setUserId(userDetails.getId());
        userService.changePhoneNumber(phoneNumberDTO);
        return ResponseEntity.ok(Collections.singletonMap("status", "Phone number updated"));
    }
}
