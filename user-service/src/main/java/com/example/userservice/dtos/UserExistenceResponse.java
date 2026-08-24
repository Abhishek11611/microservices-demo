package com.example.userservice.dtos;

public record UserExistenceResponse(boolean emailExists,
                                    boolean mobileNumberExists) {
}
