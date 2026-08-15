package com.example.practice.auth;

/**
 * 로그인 요청 형식.
 *
 * 예전에는 Member 엔티티로 직접 받았다. 동작은 했지만
 * 로그인에 필요 없는 id·role까지 받을 수 있는 형태였고,
 * "DB용 클래스"를 요청 창구에 그대로 노출하는 셈이었다.
 * → 필요한 두 개만 받는다.
 */
public record LoginRequest(
        String username,
        String password
) {
}
