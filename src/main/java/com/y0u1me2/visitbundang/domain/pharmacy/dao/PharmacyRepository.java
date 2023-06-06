package com.y0u1me2.visitbundang.domain.pharmacy.dao;

import com.y0u1me2.visitbundang.domain.pharmacy.entity.Pharmacy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PharmacyRepository extends JpaRepository<Pharmacy, Integer> {
    Page<Pharmacy> findByOpenTimeLessThanEqualAndCloseTimeGreaterThanEqual(int time1, int time2, Pageable pageable);
}
