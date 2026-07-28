package com.example.practice;

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
        String role
) {
    /** Member 엔티티에서 필요한 필드만 골라 담는다 (password는 의도적으로 제외) */
    public static MemberDto from(Member member) {
        return new MemberDto(member.getId(), member.getUsername(), member.getRole());
    }
}
