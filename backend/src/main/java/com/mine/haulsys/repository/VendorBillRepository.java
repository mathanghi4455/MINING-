package com.mine.haulsys.repository;

import com.mine.haulsys.models.VendorBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VendorBillRepository extends JpaRepository<VendorBill, Long> {
}
