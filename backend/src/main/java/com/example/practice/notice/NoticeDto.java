package com.example.practice.notice;

import java.time.format.DateTimeFormatter;

/**
 * 공지 응답 형식.
 *
 * date를 문자열("2026-08-15")로 만들어 보내는 이유:
 *   LocalDateTime을 그대로 보내면 "2026-08-15T21:03:11.123"처럼 나가서
 *   화면에서 다시 잘라 써야 한다. 표시 형식이 정해져 있다면 서버가 정리해 보낸다.
 */
public record NoticeDto(
        Long id,
        String title,
        String content,
        String writer,
        String date
) {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static NoticeDto from(Notice notice) {
        return new NoticeDto(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getWriter(),
                notice.getCreatedAt().format(FORMAT));
    }
}
