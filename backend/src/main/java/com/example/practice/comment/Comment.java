package com.example.practice.comment;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 공지사항에 달리는 댓글(문의).
 *
 * ⭐ noticeId를 연관관계(@ManyToOne Notice)가 아니라 숫자로 둔 이유
 *   목록을 뿌릴 때 공지를 함께 읽어올 필요가 없고,
 *   "내가 쓴 댓글"처럼 공지를 거치지 않고 조회하는 경우도 있기 때문이다.
 *   (학습 목적으로도, 연관관계 없이 id로 잇는 방식을 한 번 보는 편이 도움이 된다)
 */
@Entity
@Table(name = "comments",
       indexes = {
           @Index(name = "idx_comment_notice", columnList = "noticeId"),
           @Index(name = "idx_comment_writer", columnList = "writer")
       })
@Getter
@Setter
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long noticeId;      // 어느 공지에 달린 댓글인지

    @Column(nullable = false)
    private String writer;      // 작성자 아이디 (토큰에서 꺼낸 값)

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
