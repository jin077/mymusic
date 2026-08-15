package com.example.practice.message;

/**
 * 쪽지 보내기 요청.
 *
 * 보내는 사람은 받지 않는다 — 토큰에서 꺼낸다.
 *   받게 하면 남의 이름으로 쪽지를 보낼 수 있다.
 */
public record MessageRequest(
        String receiver,
        String title,
        String content
) {
}
