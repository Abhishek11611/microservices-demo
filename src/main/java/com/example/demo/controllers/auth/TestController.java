package com.example.demo.controllers.auth;

import com.example.demo.service.auth.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    private final AuthenticationService authenticationService;

    public TestController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }


    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/check-access")
    public ResponseEntity<String> checkAccess(){
        String accessTest = authenticationService.testAccess();
        return new ResponseEntity<>(accessTest, HttpStatus.OK);
    }
}
