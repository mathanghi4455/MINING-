package com.mine.haulsys.repository;

import com.mine.haulsys.models.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
    List<JournalEntry> findByJournalId(Long journalId);
    List<JournalEntry> findByEntryDateLessThanEqual(LocalDate asOf);
    List<JournalEntry> findByEntryDateBetween(LocalDate from, LocalDate to);
}
