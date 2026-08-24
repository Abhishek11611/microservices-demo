package com.example.demo.dtos.authentication;

import com.example.demo.enums.RecipientType;

public class SendOTPDTO {

    private String recipient;

    private RecipientType recipientType;

    public SendOTPDTO() {
    }

    public SendOTPDTO(String recipient, RecipientType recipientType) {
        this.recipient = recipient;
        this.recipientType = recipientType;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public RecipientType getRecipientType() {
        return recipientType;
    }

    public void setRecipientType(RecipientType recipientType) {
        this.recipientType = recipientType;
    }
}
