package com.example.demo.service.onboard;

import org.springframework.data.redis.core.RedisTemplate;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Objects;

public class OtpServiceImpl implements OtpService{

    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final String OTP_KEY_PREFIX = "otp:";
    private final RedisTemplate<String,Object> redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    public OtpServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    @Override
    public String generateAndStore(String journeyId) {
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        redisTemplate.opsForValue().set(OTP_KEY_PREFIX+journeyId,otp,OTP_TTL);
        return otp;
    }

    @Override
    public Boolean verifyOtp(String journeyId, String requestOTP) {

        if (requestOTP == null || requestOTP.isEmpty()){
            return false;
        }
        Object storedOTP = redisTemplate.opsForValue().get(OTP_KEY_PREFIX + journeyId);
        if (storedOTP == null){
            throw new RuntimeException("Invalid OTP");
        }

        return requestOTP.equals(storedOTP);
    }
}
