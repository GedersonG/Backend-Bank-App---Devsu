package com.devsu.app.domain.model;

import com.devsu.app.infraestructure.input.adapter.rest.dto.request.customer.CustomerPatchRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Customer extends Person {
    private String clientId;
    private String password;
    private Short status;

    public Customer applyPatch(CustomerPatchRequestDTO patch) {
        return this.toBuilder()
                .name(isPresent(patch.getName()) ? patch.getName() : this.getName())
                .gender(isPresent(patch.getGender()) ? patch.getGender() : this.getGender())
                .age(patch.getAge() != null ? patch.getAge() : this.getAge())
                .identification(isPresent(patch.getIdentification()) ? patch.getIdentification() : this.getIdentification())
                .address(isPresent(patch.getAddress()) ? patch.getAddress() : this.getAddress())
                .phone(isPresent(patch.getPhone()) ? patch.getPhone() : this.getPhone())
                .status(patch.getStatus() != null ? patch.getStatus() : this.getStatus())
                .password(isPresent(patch.getPassword()) ? patch.getPassword() : this.getPassword())
                .build();
    }

    private boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }

    public Customer applyUpdate(Customer incoming) {
        return this.toBuilder()
                .name(incoming.getName())
                .gender(incoming.getGender())
                .age(incoming.getAge())
                .identification(incoming.getIdentification())
                .address(incoming.getAddress())
                .phone(incoming.getPhone())
                .status(incoming.getStatus())
                .password(incoming.getPassword() != null ? incoming.getPassword() : this.getPassword())
                .build();
    }
}