package com.mine.haulsys.models;

import com.mine.haulsys.models.enums.ContactType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contacts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ContactType type;

    private String name;

    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private String taxId;
}
