package com.example.practice.message;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 쪽지 한 통.
 *
 * ⭐ 지금은 관리자만 보낼 수 있다.
 *   회원끼리 주고받게 하면 스팸·차단·신고 같은 것들이 줄줄이 따라온다.
 *   공지와 성격이 다른 "개인 안내"만 필요하므로 발신자를 관리자로 제한했다.
 *   회원 간 발송으로 넓히려면 이 엔티티는 그대로 두고 권한 규칙만 풀면 된다.
 *
 * ⭐ 읽음 표시를 받는 사람 쪽에 두는 이유
 *   같은 쪽지를 여러 명에게 보내면 사람마다 읽은 시점이 다르다.
 *   지금은 한 통에 받는 사람이 하나라 이 구조로 충분하다.
 */
@Entity
@Table(name = "messages",
       indexes = @Index(name = "idx_message_receiver", columnList = "receiver, isRead"))
@Getter
@Setter
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sender;      // 보낸 사람 아이디

    @Column(nullable = false)
    private String receiver;    // 받는 사람 아이디

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    /** 'read'는 여러 DB에서 예약어라 컬럼 이름을 피했다 */
    @Column(nullable = false)
    private boolean isRead = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
