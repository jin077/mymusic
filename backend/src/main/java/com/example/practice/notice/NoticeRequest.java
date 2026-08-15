package com.example.practice.notice;

/**
 * 공지 작성 요청 형식.
 *
 * writer를 받지 않는다. 작성자는 토큰에서 꺼내 서버가 채운다.
 *   → 클라이언트가 writer를 보내게 하면 남의 이름으로 글을 쓸 수 있다.
 */
public record NoticeRequest(
        String title,
        String content
) {
}
