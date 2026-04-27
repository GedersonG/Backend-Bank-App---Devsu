package com.devsu.app.infraestructure.output.persistence.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class MovementFilterDTO {
    private String accountNumber;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
