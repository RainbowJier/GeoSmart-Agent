package com.smartdoc.api.dto.req;


import jakarta.validation.constraints.NotBlank;

public record ChatReq(

        @NotBlank(message = "Message must not be blank")
        String message,

        String sessionId
) {
}
