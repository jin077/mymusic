package com.example.practice.common;

/**
 * 로그인은 했지만 그 일을 할 자격이 없을 때 → 403 Forbidden
 *
 * 예) 남이 쓴 댓글을 지우려 할 때
 *
 * 401과의 차이 (SecurityConfig에 적어둔 것과 같은 구분)
 *   401 : 네가 누군지 모르겠다 (토큰 없음·만료)
 *   403 : 누군지는 알겠는데 그건 네 것이 아니다
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
