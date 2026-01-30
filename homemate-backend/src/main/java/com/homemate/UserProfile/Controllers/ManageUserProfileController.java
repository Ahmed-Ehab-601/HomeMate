package com.homemate.UserProfile.Controllers;

import java.util.Collections;
import java.util.Map;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
import com.homemate.security.model.AppUserDetails;

@RestController
@RequestMapping("/api/users")
public class ManageUserProfileController {
    private static final String ADDRESS_IN_USE_ERROR =
            "This address is used in a task and cannot be deleted.";
    private static final String ACCOUNT_IN_USE_ERROR =
            "This account have related tasks and cannot be deleted.";
    private static final String ADDRESS_MAX_LENGHT_ERROR =
            "ALL ADDRESS ATTRIBUTES MUST BE < 50 ";

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
    public ResponseEntity<?> addAddress(@AuthenticationPrincipal AppUserDetails userDetails, @RequestBody AddressDTO newAddress) {
        Long userId = userDetails.getId();
        newAddress.setUserID(userId);
        boolean flag = userService.addAddress(newAddress);
        
        
        if (flag){
               return ResponseEntity.ok(Collections.singletonMap("userId", userId));
        }else{
            return new ResponseEntity<>(
                                    Map.of("error",ADDRESS_MAX_LENGHT_ERROR ),
                                    HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/addresses/{addressId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Map<String, String>> removeAddress(@AuthenticationPrincipal AppUserDetails userDetails, @PathVariable Long addressId) {
        RemoveAddressDTO removeAddressDTO = new RemoveAddressDTO();
        removeAddressDTO.setUserId(userDetails.getId());
        removeAddressDTO.setAddressId(addressId);
        boolean flag =userService.removeAddress(removeAddressDTO);
        
        if (flag){
                return ResponseEntity.ok(Collections.singletonMap("status", "Address removed"));
        }else{
            return new ResponseEntity<>(
                                    Map.of("error",ADDRESS_IN_USE_ERROR ),
                                    HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/addresses/{addressId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Map<String, String>> updateAddress(@AuthenticationPrincipal AppUserDetails userDetails,
                                                             @PathVariable Long addressId,
                                                             @RequestBody AddressDTO updateAddress) {
        updateAddress.setUserID(userDetails.getId());
        updateAddress.setAddressId(addressId);
        Boolean flag =userService.updateAddress(updateAddress);
        
        if (flag.booleanValue()){
                return ResponseEntity.ok(Collections.singletonMap("status", "Address updated"));
        }else{
            return new ResponseEntity<>(
                                    Map.of("error",ADDRESS_MAX_LENGHT_ERROR ),
                                    HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/account")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Map<String, String>> deleteAccount(@AuthenticationPrincipal AppUserDetails userDetails) {
        DeleteAccountRequestDTO request = new DeleteAccountRequestDTO();
        request.setUserId(userDetails.getId());
        Boolean flag =userService.deleteAccount(request);
    
        if (flag.booleanValue()){
                return ResponseEntity.ok(Collections.singletonMap("status", "Account deleted"));
        }else{
            return new ResponseEntity<>(
                                    Map.of("error", ACCOUNT_IN_USE_ERROR),
                                    HttpStatus.BAD_REQUEST);
        }
    }

    // @PostMapping("/signup")
    // public ResponseEntity<Map<String, String>> signup(@RequestBody SignupUserDTO userSignupDTO) {
    //     String message = userService.signup(userSignupDTO);
    //     return ResponseEntity.status(HttpStatus.CREATED).body(Collections.singletonMap("message", message));
    // }

    @GetMapping("/taskers/{taskerId}")
   
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
        try{                                                     
            nameDTO.setUserId(userDetails.getId());
            userService.changeName(nameDTO);
            return ResponseEntity.ok(Collections.singletonMap("status", "Name updated"));
        }catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    Map.of("error", e.getMessage()),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PutMapping("/password")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Map<String, String>> changePassword(@AuthenticationPrincipal AppUserDetails userDetails,
                                                              @RequestBody @Valid PasswordDTO passwordDTO) {
        try{                                                      
            passwordDTO.setUserId(userDetails.getId());
            userService.changePassword(passwordDTO);
            return ResponseEntity.ok(Collections.singletonMap("status", "Password updated"));
        }catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    Map.of("error", e.getMessage()),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @PutMapping("/phone")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Map<String, String>> changePhone(@AuthenticationPrincipal AppUserDetails userDetails,
                                                            @RequestBody PhoneNumberDTO phoneNumberDTO) {
        try{                                                         
            phoneNumberDTO.setUserId(userDetails.getId());
            userService.changePhoneNumber(phoneNumberDTO);
            return ResponseEntity.ok(Collections.singletonMap("status", "Phone number updated"));
        }catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    Map.of("error", e.getMessage()),
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}
