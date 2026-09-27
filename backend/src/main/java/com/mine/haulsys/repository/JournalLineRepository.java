package com.mine.haulsys.repository;

import com.mine.haulsys.models.JournalLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JournalLineRepository extends JpaRepository<JournalLine, Long> {
    List<JournalLine> findByEntryId(Long entryId);
    List<JournalLine> findByAccountId(Long accountId);
    List<JournalLine> findByAccountIdAndAnalyticAccountId(Long accountId, Long analyticAccountId);
}
