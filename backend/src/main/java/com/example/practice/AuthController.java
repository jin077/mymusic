package com.example.practice;

import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 로그인 API. 아이디·비번을 확인하고, 성공하면 JWT 토큰을 발급해서 돌려준다.
 *
 * 흐름:
 *   1) 클라이언트가 {username, password} 를 보냄
 *   2) AuthenticationManager가 비번이 맞는지 검증 (틀리면 예외 발생 → 401)
 *   3) 맞으면 JwtUtil로 토큰을 만들어 {token: "..."} 형태로 응답
 */
@RestController
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    // 로그인  →  POST /login  (JSON: {"username":"...","password":"..."})
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Member request) {
        // 1) 아이디+비번으로 "인증 시도" 티켓을 만들어 매니저에게 검증 요청.
        //    비번이 틀리면 여기서 예외가 발생함(→ 시큐리티가 401 처리).
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()));

        // 2) 검증 통과 → 권한 꺼내기. 시큐리티는 "ROLE_USER" 형태로 담고 있으니
        //    앞의 "ROLE_"를 떼서 순수 role("USER")만 토큰에 넣는다.
        String role = authentication.getAuthorities().iterator().next()
                .getAuthority().replace("ROLE_", "");

        // 3) 토큰 발급해서 돌려주기
        String token = jwtUtil.createToken(request.getUsername(), role);
        return Map.of("token", token);
    }
}
