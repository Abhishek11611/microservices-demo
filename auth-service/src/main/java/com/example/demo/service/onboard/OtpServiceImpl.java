package com.example.demo.service.onboard;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
public class OtpServiceImpl implements OtpService{

    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final Duration RESEND_COOLDOWN = Duration.ofMinutes(1);
    private static final Duration SEND_WINDOW = Duration.ofMinutes(15);
    private static final int MAX_SENDS = 5;
    private static final int MAX_VERIFY_ATTEMPTS = 3;


    private static final String OTP_KEY_PREFIX = "otp:";
    private static final String OTP_COOLDOWN_PREFIX = "otp:cooldown:";
    private static final String OTP_SEND_COUNT_PREFIX = "otp:send_count:";
    private static final String OTP_VERIFY_ATTEMPTS_PREFIX = "otp:verify_attempts:";

    private final RedisTemplate<String,Object> redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    public OtpServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    @Override
    public void sendOTP(String identifier) {

        String cooldownKey = cooldownKey(identifier);
        String sendCountKey = sendCountKey(identifier);
        String otpKey = otpKey(identifier);

        Boolean cooldownExists  = redisTemplate.hasKey(cooldownKey);

        if (Boolean.TRUE.equals(cooldownExists )){
            throw new RuntimeException("Please wait 60s requesting another OTP");
        }

        Long sendCount = redisTemplate.opsForValue().increment(sendCountKey);

        if (sendCount  !=null && sendCount  == 1){
            redisTemplate.expire(sendCountKey,SEND_WINDOW);
        }

        if (sendCount  !=null && sendCount  > MAX_SENDS){
            throw new RuntimeException( "Maximum OTP requests exceeded");
        }

        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        redisTemplate.opsForValue().set(otpKey,otp,OTP_TTL);
        redisTemplate.opsForValue().set(cooldownKey,"1",RESEND_COOLDOWN);

    }

    @Override
    public Boolean verifyOtp(String identifier, String requestOTP) {

       final String key = otpKey(identifier);
        String verifyAttemptsKey = otpVerifyAttemptsKey(identifier);
        final String normalizedRequestOtp = requestOTP == null ? null : requestOTP.trim();

        if (normalizedRequestOtp == null || normalizedRequestOtp.isEmpty()){
            return false;
        }
        Object storedOTP = redisTemplate.opsForValue().get(key);
        if (storedOTP == null){
            throw new RuntimeException("Please Send OTP.");
        }

        String normalizedStoredOtp = String.valueOf(storedOTP).trim();
        boolean isValidOTP = normalizedRequestOtp.equals(normalizedStoredOtp);

        if (isValidOTP){
            redisTemplate.delete(key);
            redisTemplate.delete(verifyAttemptsKey);
            return true;
        }

        Long attempt = redisTemplate.opsForValue().increment(verifyAttemptsKey);

        if (attempt!= null && attempt == 1){
            redisTemplate.expire(verifyAttemptsKey,OTP_TTL);
        }

        if (attempt != null && attempt > MAX_VERIFY_ATTEMPTS){
            redisTemplate.delete(key);
            redisTemplate.delete(verifyAttemptsKey);
            throw new RuntimeException("Maximum OTP verification attempts exceeded");
        }

        return false;
    }


    private String otpKey(String identifier) {
        return OTP_KEY_PREFIX + identifier;
    }

    private String cooldownKey(String identifier) {
        return OTP_COOLDOWN_PREFIX + identifier;
    }

    private String sendCountKey(String identifier) {
        return OTP_SEND_COUNT_PREFIX + identifier;
    }

    private String otpVerifyAttemptsKey(String identifier){
        return OTP_VERIFY_ATTEMPTS_PREFIX + identifier;
    }
}
