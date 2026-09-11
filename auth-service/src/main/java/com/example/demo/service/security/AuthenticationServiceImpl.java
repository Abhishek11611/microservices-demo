package com.example.demo.service.security;

import com.example.commoncore.exception.NotFoundException;
import com.example.demo.dtos.authentication.SendOTPDTO;
import com.example.demo.dtos.authentication.TokenResponseDTO;
import com.example.demo.dtos.authentication.VerifyOtpRequest;
import com.example.demo.entities.users.AuthUser;
import com.example.demo.enums.RecipientType;
import com.example.demo.repositories.users.AuthUsersRepository;
import com.example.demo.service.onboard.OtpService;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final JwtService jwtService;
    private final AuthUsersRepository authUsersRepository;
    private final OtpService otpService;

    public AuthenticationServiceImpl(JwtService jwtService, AuthUsersRepository authUsersRepository, OtpService otpService) {
        this.jwtService = jwtService;
        this.authUsersRepository = authUsersRepository;
        this.otpService = otpService;
    }

    @Override
    public String sendOtp(SendOTPDTO sendOTPDTO) {
        String recipient = sendOTPDTO.getRecipient();
        RecipientType recipientType = sendOTPDTO.getRecipientType();

        AuthUser user = findUserByRecipient(recipient, recipientType);
        otpService.sendOTP(String.valueOf(user.getId()));

        return "OTP Send Successfully";
    }

    @Override
    public TokenResponseDTO verifyOTP(VerifyOtpRequest verifyOtpRequest, HttpServletRequest servletRequest) throws JOSEException {

        String recipient = verifyOtpRequest.recipient();
        Boolean isVerifyOtp = otpService.verifyOtp(recipient, verifyOtpRequest.otpCode());

        if (!isVerifyOtp){
            throw new RuntimeException("OTP is Invalid");
        }

        AuthUser users = findUserByRecipient(verifyOtpRequest.recipient(), RecipientType.PHONE);

        return  generateToken(users,servletRequest);
    }

    private TokenResponseDTO generateToken(AuthUser users,HttpServletRequest servletRequest)throws JOSEException{

        String email = users.getEmail();

        String accessToken = jwtService.generateAccessToken(email,"ADMIN");

        String refreshToken = UUID.randomUUID().toString();

        return new TokenResponseDTO(refreshToken,accessToken, LocalDateTime.now(),email);
    }

    private AuthUser findUserByRecipient(String recipient, RecipientType recipientType){

        return switch (recipientType) {
            case EMAIL ->
                    authUsersRepository.findByEmail(recipient).orElseThrow(() -> new NotFoundException("User With Email " + recipient + " does not exist"));

            case PHONE ->
                    authUsersRepository.findByMobileNumber(recipient).orElseThrow(() -> new NotFoundException("User With MobileNumber " + recipient + " does not exist"));

        };

    }

    @Override
    public String testAccess(){
        return "Hi Admin";
    }
}
