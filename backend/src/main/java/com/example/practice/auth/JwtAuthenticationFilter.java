package com.example.practice.auth;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 요청이 컨트롤러에 닿기 전에 먼저 토큰을 검사하는 "문지기" 필터.
 *
 * 흐름(요청 1건마다):
 *   1) Authorization 헤더에서 "Bearer 토큰" 꺼내기
 *   2) 토큰이 유효하면 → 안에서 username·role을 꺼내
 *      "이 사람은 로그인된 상태(+권한)야"를 SecurityContext에 등록
 *   3) (토큰이 없거나 이상하면 그냥 통과 → 뒤에서 인증 필요한 곳이면 시큐리티가 막음)
 *
 * OncePerRequestFilter: 요청 1건당 딱 한 번만 실행되도록 보장하는 필터 베이스.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // "Bearer "로 시작하는 헤더가 있을 때만 토큰 검사
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7); // "Bearer " 7글자 잘라내기

            if (jwtUtil.isValid(token)) {
                String username = jwtUtil.getUsername(token);
                String role = jwtUtil.getRole(token);

                // 시큐리티는 권한을 "ROLE_USER" 형태로 다루므로 접두사 붙여서 등록
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

                // "인증 완료" 상태의 토큰 객체를 만들어 SecurityContext에 넣어줌
                //  → 이후 컨트롤러/시큐리티가 "로그인된 사용자"로 인식
                var authentication = new UsernamePasswordAuthenticationToken(
                        username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 다음 필터(또는 컨트롤러)로 요청을 넘김 — 이 줄이 빠지면 요청이 멈춤!
        filterChain.doFilter(request, response);
    }
}
