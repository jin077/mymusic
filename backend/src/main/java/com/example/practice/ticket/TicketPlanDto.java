package com.example.practice.ticket;

/** 이용권 상품 응답 형식. 화면이 가격표를 그릴 때 쓴다. */
public record TicketPlanDto(
        String id,          // 코드값 (구매 요청에 이 값을 보낸다)
        String name,
        long price,
        int days,
        String description
) {
    public static TicketPlanDto from(TicketPlan plan) {
        return new TicketPlanDto(
                plan.name(), plan.label(), plan.price(), plan.days(), plan.description());
    }
}
