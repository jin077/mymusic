package com.example.practice.member;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    private String nickname; // 화면에 보여줄 이름 (없으면 아이디를 대신 표시)

    private String email;    // 이메일 (선택 입력)

    /**
     * 프로필 사진 — 저장된 "파일 이름"만 담는다. (예: 3f9c...a1.png)
     *
     * ⭐ "/uploads/3f9c...png" 같은 전체 경로를 저장하지 않는 이유
     *   나중에 저장 위치나 주소 규칙이 바뀌면(예: S3로 이전) DB에 쌓인 값을 전부 고쳐야 한다.
     *   파일 이름만 두고, 밖으로 내보낼 때 MemberDto가 주소를 조립한다.
     */
    private String profileImage;

    /**
     * 성인 인증 여부.
     * 실제 본인인증(PASS·아이핀 등)은 외부 서비스를 붙여야 하므로 지금은 값만 들고 있다.
     * 19금 음원 재생 제한 같은 기능을 붙일 때 이 값을 검사하게 된다.
     */
    private boolean adultVerified = false;

    /**
     * 보유 캐시(원).
     *
     * ⭐ 금액을 실수(double)가 아니라 정수로 두는 이유
     *   실수는 0.1 + 0.2 가 정확히 0.3이 되지 않는다(부동소수점 오차).
     *   돈은 1원이라도 어긋나면 안 되므로 정수(원 단위)나 BigDecimal을 쓴다.
     *   우리는 원 단위 정수만 다루므로 long이면 충분하다.
     */
    private long balance = 0;

    /** 지금 쓰고 있는 이용권 이름 (없으면 null) */
    private String ticketName;

    /**
     * 이용권 만료일 (없으면 null).
     *
     * "며칠 남았는지"를 저장하지 않고 만료일을 저장하는 이유:
     *   남은 일수를 저장하면 매일 누군가 1씩 줄여줘야 한다(배치 필요).
     *   만료일만 두면 볼 때마다 오늘과 빼서 계산하면 되고, 값이 저절로 정확하다.
     */
    private LocalDate ticketExpiresAt;

    /** 가입 시각. 한 번 정해지면 바뀌지 않으므로 updatable=false 로 잠근다. */
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
