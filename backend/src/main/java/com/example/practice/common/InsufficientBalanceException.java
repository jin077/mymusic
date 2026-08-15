package com.example.practice.common;

/**
 * 잔액이 모자랄 때 → 402 Payment Required
 *
 * ⭐ 왜 400이 아니라 402인가
 *   400(잘못된 요청)은 "보낸 값이 이상하다"는 뜻이다.
 *   그런데 이 경우 요청 자체는 멀쩡하다. 돈이 모자랄 뿐이다.
 *   402는 원래 규격에 "미래를 위해 예약됨"으로 남아 있었지만,
 *   지금은 결제·잔액 관련 API에서 이 뜻으로 널리 쓰인다.
 *   → 프론트가 "충전하러 가시겠어요?"로 정확히 안내할 수 있다.
 */
public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
