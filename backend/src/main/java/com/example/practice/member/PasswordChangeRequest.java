package com.example.practice.member;

/**
 * 비밀번호 변경 요청.
 *
 * ⭐ 현재 비밀번호를 함께 받는 이유
 *   로그인한 상태여도, 자리를 비운 사이 남이 화면을 만질 수 있다.
 *   비밀번호가 바뀌면 계정을 통째로 빼앗기므로, 되돌릴 수 없는 작업 앞에서
 *   "본인이 맞는지" 한 번 더 확인한다(재인증).
 *   토큰만으로 통과시키면 토큰이 유출됐을 때 곧바로 계정을 잃는다.
 */
public record PasswordChangeRequest(
        String currentPassword,
        String newPassword
) {
}
