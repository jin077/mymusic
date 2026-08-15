package com.example.practice.common;

/**
 * 이미 있는 값을 또 만들려 할 때 던지는 예외 → 409 Conflict
 *
 * 예) 이미 존재하는 아이디로 회원가입
 *
 * 왜 500이 아니라 409인가?
 *   500은 "서버가 잘못했다"는 뜻이다. 중복 아이디는 서버 잘못이 아니라
 *   클라이언트가 보낸 값이 현재 상태와 충돌한 것이므로 4xx가 맞다.
 *   400(잘못된 요청)보다 409(충돌)가 원인을 더 정확히 알려준다.
 */
public class DuplicateException extends RuntimeException {
    public DuplicateException(String message) {
        super(message);
    }
}
