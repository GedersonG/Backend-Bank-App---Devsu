package com.devsu.app.infraestructure.input.adapter.rest.dto.response.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class CustomerResponseDTO {
    private String clientId;
    private String name;
    private String identification;
    private String phone;
    private String address;
    private String gender;
    private String age;
    private Short status;
}
