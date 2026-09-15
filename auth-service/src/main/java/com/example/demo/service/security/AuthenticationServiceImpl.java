package com.example.demo.service.security;

import com.example.commoncore.exception.NotFoundException;
import com.example.demo.dtos.authentication.SendOTPDTO;
import com.example.demo.dtos.authentication.TokenResponseDTO;
import com.example.demo.dtos.authentication.VerifyOtpRequest;
import com.example.demo.entities.auth.RefreshToken;
import com.example.demo.entities.users.AuthUser;
import com.example.demo.entities.users.Role;
import com.example.demo.entities.users.UserRoles;
import com.example.demo.enums.RecipientType;
import com.example.demo.repositories.auth.RefreshTokenRepository;
import com.example.demo.repositories.users.AuthUsersRepository;
import com.example.demo.service.onboard.OtpService;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final JwtService jwtService;
    private final AuthUsersRepository authUsersRepository;
    private final OtpService otpService;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthenticationServiceImpl(JwtService jwtService, AuthUsersRepository authUsersRepository, OtpService otpService, RefreshTokenRepository refreshTokenRepository) {
        this.jwtService = jwtService;
        this.authUsersRepository = authUsersRepository;
        this.otpService = otpService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public String sendOtp(SendOTPDTO sendOTPDTO) {
        String recipient = sendOTPDTO.getRecipient();
        RecipientType recipientType = sendOTPDTO.getRecipientType();

        AuthUser user = findUserByRecipient(recipient, recipientType);
        otpService.sendOTP(user.getMobileNumber());

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

        return  generateToken(users,servletRequest,null);
    }

    private TokenResponseDTO generateToken(AuthUser users,HttpServletRequest servletRequest,RefreshToken existingRefreshToken)throws JOSEException{

        String email = users.getEmail();

        String role = users.getUserRoles().stream()
                .map(UserRoles::getRole)
                .findFirst()
                .map(Role::getName)
                .orElseThrow(() -> new NotFoundException("Role Not Found"));

        Long userId = users.getId();
        String userCode = users.getUserCode();

        String accessToken = jwtService.generateAccessToken(email,role,userId,userCode);

        long expirationMs = jwtService.ACCESS_TOKEN_TTL_MIN;
        long refreshTokenTtlMin = jwtService.REFRESH_TOKEN_TTL_MIN;

        LocalDateTime issuedAt = LocalDateTime.now();
        LocalDateTime expiresAt = issuedAt.plusMinutes(expirationMs);
        LocalDateTime refreshTokenExpiresAt = issuedAt.plusDays(refreshTokenTtlMin);

        if (existingRefreshToken != null && !existingRefreshToken.shouldShuffleToken()){
            existingRefreshToken.setExpiresAt(refreshTokenExpiresAt);
            refreshTokenRepository.save(existingRefreshToken);
            return new TokenResponseDTO(existingRefreshToken.getToken(),accessToken, expiresAt,email);
        }

        String refreshToken = UUID.randomUUID().toString();

        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setToken(refreshToken);
        refreshTokenEntity.setCreatedByIpAddress(servletRequest.getRemoteAddr());
        refreshTokenEntity.setUserId(userId);
        refreshTokenEntity.setExpiresAt(refreshTokenExpiresAt);
        refreshTokenRepository.save(refreshTokenEntity);


        return new TokenResponseDTO(refreshToken,accessToken, expiresAt,email);
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
