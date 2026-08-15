package com.example.practice.member;

/**
 * 회원가입 요청 형식.
 *
 * ⭐ 왜 Member 엔티티로 직접 받지 않는가
 *   엔티티로 받으면 클라이언트가 {"username":"x","role":"ADMIN","id":99} 처럼
 *   서버가 정해야 할 값까지 보낼 수 있다. 지금은 서비스에서 role을 덮어써 막고 있지만,
 *   "받을 수 있는 항목" 자체를 좁히는 편이 안전하고 의도도 분명하다.
 *
 *   응답을 MemberDto로 분리한 것과 같은 이유이며, 방향만 반대다.
 *     요청 DTO ← 클라이언트가 보낼 수 있는 것만 정의
 *     응답 DTO → 클라이언트에게 보여줄 것만 정의
 */
public record SignupRequest(
        String username,
        String password,
        String nickname,
        String email
) {
}
