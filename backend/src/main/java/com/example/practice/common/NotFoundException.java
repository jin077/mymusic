package com.example.practice.common;

/**
 * 요청한 자원이 없을 때 던지는 예외 → 404 Not Found
 *
 * 예) 없는 회원 id로 삭제 요청, 없는 공지 번호 조회
 *
 * RuntimeException을 상속한 이유:
 *   검사 예외(Exception)로 만들면 호출하는 쪽마다 try-catch나 throws를 강제당한다.
 *   서비스 로직 중간에서 "없으면 중단"만 하면 되므로 비검사 예외가 알맞다.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
