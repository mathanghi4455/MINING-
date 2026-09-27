package com.mine.haulsys.dto;

import com.mine.haulsys.models.enums.ContactType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ContactDto {
    private Long id;
    @NotNull
    private ContactType type;
    @NotBlank
    private String name;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private String taxId;
}
