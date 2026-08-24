package com.example.userservice.service.utility;

import com.example.userservice.dtos.UserExistenceRequest;
import com.example.userservice.dtos.UserExistenceResponse;
import com.example.userservice.repositories.UsersRepository;

import java.util.HashMap;
import java.util.Map;

public class UserLookupServiceImpl implements UserLookupService{

    private final UsersRepository usersRepository;

    public UserLookupServiceImpl(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public Map<String, Boolean> checkUserDetailsExists(UserExistenceRequest userExistenceRequest) {

        Map<String,Boolean> userDetails = new HashMap<>();

        if (userExistenceRequest.email() != null && !userExistenceRequest.email().isBlank()){
            boolean email = usersRepository.existsByEmail(userExistenceRequest.email());
            userDetails.put("emailExists",email);
        }
        if (userExistenceRequest.mobileNumber() != null && !userExistenceRequest.mobileNumber().isBlank()) {
            boolean mobile = usersRepository.existsByMobileNumber(userExistenceRequest.mobileNumber());
            userDetails.put("mobileNumberExists",mobile);
        }

        return userDetails;
    }
}
