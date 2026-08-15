package com.example.practice.comment;

import java.time.format.DateTimeFormatter;

/**
 * 댓글 응답 형식.
 *
 * noticeTitle은 "내가 쓴 댓글" 목록에서만 채운다.
 *   공지 상세에서는 이미 제목이 화면에 있으므로 다시 보낼 필요가 없다.
 *   → 같은 DTO를 쓰되, 상황에 따라 채우는 필드가 다르다.
 */
public record CommentDto(
        Long id,
        Long noticeId,
        String noticeTitle,
        String writer,
        String content,
        String date
) {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static CommentDto from(Comment comment) {
        return from(comment, null);
    }

    public static CommentDto from(Comment comment, String noticeTitle) {
        return new CommentDto(
                comment.getId(),
                comment.getNoticeId(),
                noticeTitle,
                comment.getWriter(),
                comment.getContent(),
                comment.getCreatedAt().format(FORMAT));
    }
}
