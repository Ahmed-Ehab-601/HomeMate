package com.homemate.UserProfile.Controllers;

import java.util.Collections;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.homemate.UserProfile.DTO.AddressDTO;
import com.homemate.UserProfile.DTO.DeleteAccountRequestDTO;
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

    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileDTO> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @PostMapping("/{userId}/addresses")
    public ResponseEntity<Map<String, Long>> addAddress(@PathVariable Long userId, @RequestBody AddressDTO newAddress) {
        newAddress.setUserId(userId);
        userService.addAddress(newAddress);
        return ResponseEntity.ok(Collections.singletonMap("userId", userId));
    }

    @DeleteMapping("/{userId}/addresses/{addressId}")
    public ResponseEntity<Map<String, String>> removeAddress(@PathVariable Long userId, @PathVariable Long addressId) {
        RemoveAddressDTO removeAddressDTO = new RemoveAddressDTO();
        removeAddressDTO.setUserId(userId);
        removeAddressDTO.setAddressId(addressId);
        userService.removeAddress(removeAddressDTO);
        return ResponseEntity.ok(Collections.singletonMap("status", "Address removed"));
    }

    @PutMapping("/{userId}/addresses/{addressId}")
    public ResponseEntity<Map<String, String>> updateAddress(@PathVariable Long userId,
                                                             @PathVariable Long addressId,
                                                             @RequestBody AddressDTO updateAddress) {
        updateAddress.setUserId(userId);
        updateAddress.setAddressId(addressId);
        userService.updateAddress(updateAddress);
        return ResponseEntity.ok(Collections.singletonMap("status", "Address updated"));
    }

    @DeleteMapping("/{userId}/account")
    public ResponseEntity<Map<String, String>> deleteAccount(@PathVariable Long userId) {
        DeleteAccountRequestDTO request = new DeleteAccountRequestDTO();
        request.setUserId(userId);
        userService.deleteAccount(request);
        return ResponseEntity.ok(Collections.singletonMap("status", "Account deleted"));
    }

    // @PostMapping("/signup")
    // public ResponseEntity<Map<String, String>> signup(@RequestBody SignupUserDTO userSignupDTO) {
    //     String message = userService.signup(userSignupDTO);
    //     return ResponseEntity.status(HttpStatus.CREATED).body(Collections.singletonMap("message", message));
    // }

    @GetMapping("/taskers/{taskerId}")
    public ResponseEntity<UserRequestTaskerDTO> getTaskerProfile(@PathVariable Long taskerId) {
        return ResponseEntity.ok(userService.getTaskerProfile(taskerId));
    }

    @GetMapping(value = "/addresses", params = "userId")
    public ResponseEntity<AddressDTO[]> getAddresses(@RequestParam("userId") Integer userID) {
        return ResponseEntity.ok(userService.getAddresses(userID));
    }

   

    @GetMapping("/{userId}/addresses")
    public ResponseEntity<AddressDTO[]> getAddressesForUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(userService.getAddresses(userId));
    }
}
