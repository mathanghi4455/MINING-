package com.mine.haulsys.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "analytic_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;
}
