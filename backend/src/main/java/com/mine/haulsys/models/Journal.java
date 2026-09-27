package com.mine.haulsys.models;

import com.mine.haulsys.models.enums.JournalType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "journals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Journal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private JournalType type;
}
