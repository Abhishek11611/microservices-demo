package com.example.commoncore.dto;

public class EmptyAPIResponse {
    protected String message;
    protected Boolean status;
    public EmptyAPIResponse(String message, Boolean status) {
        this.message = message;
        this.status = status;
    }
    public EmptyAPIResponse() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
