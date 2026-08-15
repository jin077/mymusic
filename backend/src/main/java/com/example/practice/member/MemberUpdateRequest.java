package com.example.practice.member;

/**
 * 내 정보 수정 요청 형식.
 *
 * 아이디(username)와 권한(role)은 일부러 넣지 않았다.
 *   - 아이디는 로그인 식별자이자 토큰의 주체라 바꾸면 발급된 토큰이 어긋난다.
 *   - 권한은 본인이 바꿀 수 있으면 누구나 관리자가 된다.
 * → "바꿀 수 있는 것만" 요청 형식에 담는다.
 */
public record MemberUpdateRequest(
        String nickname,
        String email
) {
}
