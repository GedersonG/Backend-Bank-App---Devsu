package com.devsu.app.infraestructure.input.adapter.rest.dto.request.customer;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CustomerUpdateRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String name;

    @Pattern(regexp = "^(Male|Female|Other)$", message = "El género debe ser Male, Female u Other")
    private String gender;

    @NotNull(message = "La edad es obligatoria")
    @Min(value = 0, message = "La edad no puede ser negativa")
    @Max(value = 150, message = "La edad no es válida")
    private Integer age;

    @NotBlank(message = "La identificación es obligatoria")
    @Size(max = 50)
    private String identification;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 255)
    private String address;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9+\\-\\s]{7,20}$")
    private String phone;

    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    private Short status;
}
