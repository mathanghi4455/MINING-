package com.mine.haulsys.dto;

import com.mine.haulsys.models.enums.JournalType;
import lombok.Data;

@Data
public class JournalDto {
    private Long id;
    private String name;
    private JournalType type;
}
