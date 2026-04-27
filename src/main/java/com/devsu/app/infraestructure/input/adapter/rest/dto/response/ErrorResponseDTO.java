package com.devsu.app.infraestructure.input.adapter.rest.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder(toBuilder = true)
public class ErrorResponseDTO {
    private String errorCode;
    private String message;
    private List<String> details;
    private LocalDateTime timestamp;
    private String path;
}
