package com.example.commoncore.dto;

public class BaseAPIResponse<T> extends EmptyAPIResponse {

    protected T data;
    protected String message;
    protected Boolean status;

    public BaseAPIResponse() {
        super();
    }
    public T getData() {
        return data;
    }
    public void setData(T data) {
        this.data = data;
    }

    public BaseAPIResponse(T data, String message, Boolean status) {
        super(message, status);
        this.data = data;

    }
}
