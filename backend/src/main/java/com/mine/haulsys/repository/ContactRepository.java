package com.mine.haulsys.repository;

import com.mine.haulsys.models.Contact;
import com.mine.haulsys.models.enums.ContactType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    List<Contact> findByType(ContactType type);
}
