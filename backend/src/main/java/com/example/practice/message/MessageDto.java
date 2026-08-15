package com.example.practice.message;

import java.time.format.DateTimeFormatter;

/** 쪽지 응답 형식. */
public record MessageDto(
        Long id,
        String sender,
        String title,
        String content,
        boolean read,
        String date
) {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static MessageDto from(Message m) {
        return new MessageDto(
                m.getId(), m.getSender(), m.getTitle(), m.getContent(),
                m.isRead(), m.getCreatedAt().format(FORMAT));
    }
}
