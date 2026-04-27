package com.devsu.app.infraestructure.input.adapter.rest.dto.request.customer;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CustomerPatchRequestDTO {

    @Size(max = 100)
    private String name;

    @Pattern(regexp = "^(Male|Female|Other)$")
    private String gender;

    @Min(0) @Max(150)
    private Integer age;

    @Size(max = 50)
    private String identification;

    @Size(max = 255)
    private String address;

    @Pattern(regexp = "^[0-9+\\-\\s]{7,20}$")
    private String phone;

    @Size(min = 8)
    private String password;

    private Short status;
}
