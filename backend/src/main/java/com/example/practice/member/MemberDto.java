package com.example.practice.member;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 회원 정보를 밖으로 내보낼 때 쓰는 응답 형식.
 *
 * ⭐ 이 클래스를 만든 이유 (보안 문제 해결):
 *   기존 AdminController는 Member 엔티티를 그대로 반환했다.
 *   Member에는 password 필드가 있으므로, 응답 JSON에 비밀번호 해시가 그대로 노출됐다.
 *     예) {"id":1,"username":"admin","password":"$2a$10$...","role":"ADMIN"}
 *
 *   BCrypt 해시라 바로 비밀번호를 알 수는 없지만,
 *   해시가 유출되면 공격자가 시간을 들여 대조 공격(무차별 대입)을 시도할 수 있다.
 *   → 애초에 필요 없는 정보는 응답에 담지 않는다. (최소 권한·최소 노출 원칙)
 *
 *   그래서 엔티티(DB용)와 응답(API용)을 분리한다. 실무의 기본 규칙이다.
 */
public record MemberDto(
        Long id,
        String username,
        String role,
        String nickname,
        String email,
        String profileImage,
        boolean adultVerified,
        String createdAt,       // yyyy-MM-dd (화면에 그대로 쓰도록 서버가 정리해 보낸다)
        long balance,           // 보유 캐시(원)
        String ticketName,      // 이용권 이름 (없으면 null)
        String ticketExpiresAt, // 이용권 만료일 (없으면 null)
        Long ticketDaysLeft     // 남은 일수 — 서버가 계산해 보낸다 (없거나 만료면 null)
) {
    /** 업로드된 파일이 공개되는 주소 앞부분. 규칙이 바뀌면 이 한 줄만 고치면 된다. */
    private static final String IMAGE_PATH = "/uploads/";

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** Member 엔티티에서 필요한 필드만 골라 담는다 (password는 의도적으로 제외) */
    public static MemberDto from(Member member) {
        return new MemberDto(
                member.getId(),
                member.getUsername(),
                member.getRole(),
                member.getNickname(),
                member.getEmail(),
                // DB에는 파일 이름만 있으므로 여기서 주소를 조립한다
                member.getProfileImage() == null ? null : IMAGE_PATH + member.getProfileImage(),
                member.isAdultVerified(),
                // 기존 회원은 가입 시각이 없을 수 있다(컬럼을 나중에 추가했으므로)
                member.getCreatedAt() == null ? null : member.getCreatedAt().format(FORMAT),
                member.getBalance(),
                member.getTicketName(),
                member.getTicketExpiresAt() == null ? null : member.getTicketExpiresAt().toString(),
                daysLeft(member.getTicketExpiresAt()));
    }

    /**
     * 남은 일수 계산 — 만료일이 없거나 이미 지났으면 null.
     *
     * 화면에서 날짜를 빼게 하지 않고 서버가 계산해 보낸다.
     *   기준 시각(오늘)이 사용자 PC 시계에 좌우되면 사람마다 다르게 보인다.
     */
    private static Long daysLeft(LocalDate expiresAt) {
        if (expiresAt == null) return null;
        long days = ChronoUnit.DAYS.between(LocalDate.now(), expiresAt);
        return days < 0 ? null : days;
    }
}
