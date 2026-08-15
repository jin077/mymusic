package com.example.practice.ticket;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 이용권 구매 기록.
 *
 * ⭐ @Enumerated(EnumType.STRING) 인 이유
 *   기본값(ORDINAL)은 enum의 "순서 번호"를 저장한다.
 *   나중에 상품 순서를 바꾸거나 중간에 하나 끼워 넣으면
 *   이미 저장된 기록의 뜻이 통째로 바뀐다(1번이 다른 상품이 됨).
 *   이름으로 저장하면 순서를 바꿔도 안전하다.
 */
@Entity
@Table(name = "ticket_purchases",
       indexes = @Index(name = "idx_ticket_user", columnList = "username"))
@Getter
@Setter
public class TicketPurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketPlan plan;

    /** 거래 시점의 가격 — 나중에 값이 바뀌어도 지난 기록은 그대로여야 한다 */
    @Column(nullable = false)
    private long price;

    /** 이 결제로 늘어난 만료일 */
    @Column(nullable = false)
    private LocalDate expiresAt;

    @Column(nullable = false)
    private LocalDateTime purchasedAt = LocalDateTime.now();
}
