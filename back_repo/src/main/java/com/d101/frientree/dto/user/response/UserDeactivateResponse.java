package com.d101.frientree.dto.user.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDeactivateResponse {

    private String message;
    private Boolean data;

    public static UserDeactivateResponse createUserDeactivateResponse(String message, Boolean data) {
        return UserDeactivateResponse.builder()
                .message(message)
                .data(data)
                .build();
    }
}
