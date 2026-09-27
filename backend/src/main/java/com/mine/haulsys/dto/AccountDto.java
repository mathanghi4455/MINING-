package com.mine.haulsys.dto;

import com.mine.haulsys.models.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccountDto {
    private Long id;
    @NotBlank
    private String code;
    @NotBlank
    private String name;
    @NotNull
    private AccountType type;
    private Long parentAccountId;
}
