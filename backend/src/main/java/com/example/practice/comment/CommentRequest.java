package com.example.practice.comment;

/**
 * 댓글 작성 요청.
 *
 * writer를 받지 않는다. 작성자는 토큰에서 꺼내 서버가 채운다.
 *   → 받게 하면 남의 이름으로 댓글을 쓸 수 있다.
 */
public record CommentRequest(
        Long noticeId,
        String content
) {
}
