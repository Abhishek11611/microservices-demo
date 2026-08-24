package com.example.demo.dtos.authentication;

public class SendOTPResponseDTO {

    private Long resendAfterSeconds;
    private Boolean isMaxLimitReached;

    public SendOTPResponseDTO() {
    }

    public SendOTPResponseDTO(Long resendAfterSeconds, Boolean isMaxLimitReached) {
        this.resendAfterSeconds = resendAfterSeconds;
        this.isMaxLimitReached = isMaxLimitReached;
    }

    public Long getResendAfterSeconds() {
        return resendAfterSeconds;
    }

    public void setResendAfterSeconds(Long resendAfterSeconds) {
        this.resendAfterSeconds = resendAfterSeconds;
    }

    public Boolean getMaxLimitReached() {
        return isMaxLimitReached;
    }

    public void setMaxLimitReached(Boolean maxLimitReached) {
        isMaxLimitReached = maxLimitReached;
    }
}
