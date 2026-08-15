package com.example.practice.ticket;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketPurchaseRepository extends JpaRepository<TicketPurchase, Long> {

    List<TicketPurchase> findByUsernameOrderByIdDesc(String username);

    /** 탈퇴 시 정리 */
    void deleteByUsername(String username);
}
