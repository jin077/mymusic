package com.example.practice.ticket;

import java.time.format.DateTimeFormatter;

/** 이용권 구매내역 응답 형식. */
public record TicketPurchaseDto(
        Long id,
        String plan,        // 상품 이름
        long price,
        String expiresAt,   // 이 결제 후의 만료일
        String purchasedAt
) {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static TicketPurchaseDto from(TicketPurchase p) {
        return new TicketPurchaseDto(
                p.getId(),
                p.getPlan().label(),
                p.getPrice(),
                p.getExpiresAt().toString(),
                p.getPurchasedAt().format(FORMAT));
    }
}
