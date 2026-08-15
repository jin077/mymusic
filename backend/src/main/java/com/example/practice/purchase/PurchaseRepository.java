package com.example.practice.purchase;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    /** 내 구매내역 (최신순) */
    List<Purchase> findByUsernameOrderByIdDesc(String username);

    /** 이미 산 곡인지 */
    boolean existsByUsernameAndTrackId(String username, String trackId);

    /** 탈퇴 시 정리 */
    void deleteByUsername(String username);
}
