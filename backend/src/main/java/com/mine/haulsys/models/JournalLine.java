package com.mine.haulsys.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "journal_lines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long entryId;

    private Long accountId;

    @Builder.Default
    private BigDecimal debit = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal credit = BigDecimal.ZERO;

    private Long analyticAccountId;
    private String description;
}
