package com.example.practice.notice;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 공지사항 글 엔티티.
 *
 * writer 를 Member와 연관관계(@ManyToOne)로 묶지 않고 아이디 문자열로 둔 이유:
 *   작성자가 탈퇴해도 글은 남아야 하고, 목록을 뿌릴 때 회원 테이블을 join할 필요가 없다.
 *   (관리자만 쓰는 공지라 작성자 정보를 더 파고들 일이 없다)
 */
@Entity
@Table(name = "notices")
@Getter
@Setter
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob                     // 긴 글이 들어갈 수 있으므로 TEXT 타입으로 만든다
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String writer;   // 작성자 아이디 (토큰에서 꺼낸 값)

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
