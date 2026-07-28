package com.example.practice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 회원(로그인 계정) 엔티티.
 *
 * ⚠️ 클래스 이름을 'User'가 아니라 'Member'로 지은 이유:
 *   1) 'user'는 여러 DB에서 예약어라 테이블 이름으로 쓰면 충돌 위험이 있음
 *   2) 스프링 시큐리티에도 이미 'User'라는 클래스가 있어서 헷갈림
 * → 그래서 실무에서 회원 엔티티는 보통 Member 로 짓고, 테이블도 members 로 둠.
 */
@Entity
@Table(name = "members")
@Getter
@Setter
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)   // 아이디는 중복 불가 (DB에 유니크 제약 생성)
    private String username; // 로그인 아이디

    private String password; // 비밀번호 (BCrypt로 암호화되어 저장됨)

    private String role = "USER"; // 권한. 지금은 USER만, 나중에 ADMIN 등으로 확장
}
