package com.mine.haulsys.repository;

import com.mine.haulsys.models.Account;
import com.mine.haulsys.models.enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByCode(String code);
    List<Account> findByType(AccountType type);
}
