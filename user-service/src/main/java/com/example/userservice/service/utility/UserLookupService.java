package com.example.userservice.service.utility;

import com.example.userservice.dtos.UserExistenceRequest;

import java.util.Map;

public interface UserLookupService {

    Map<String, Boolean> checkUserDetailsExists(UserExistenceRequest userExistenceRequest);

}
