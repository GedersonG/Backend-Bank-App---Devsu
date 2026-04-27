package com.devsu.app.infraestructure.output.persistence.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AccountFilterDTO {
    private String accountNumber;
    private String accountType;
    private String customerIdentification;
}
