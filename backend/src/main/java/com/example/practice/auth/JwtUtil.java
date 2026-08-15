package com.example.practice.auth;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * JWT 토큰을 "만들고 / 검증하고 / 내용을 꺼내는" 도구 상자.
 *
 * JWT 한 줄 요약: 서버가 "너는 admin이고 권한은 ADMIN이야"라는 정보를
 *   비밀키로 서명(도장 찍기)해서 만든 문자열. 위조하면 서명이 안 맞아 바로 걸림.
 *
 * 토큰 구조(점 2개로 3부분): header.payload.signature
 *   - payload 안에 subject(누구), role(권한), 만료시각 등이 들어감.
 */
@Component
public class JwtUtil {

    private final SecretKey key;       // 서명에 쓸 비밀키 (설정값으로 만듦)
    private final long expirationMs;   // 토큰 유효시간(ms)

    /**
     * 생성자에서 application.properties의 값을 주입받음.
     *   @Value("${jwt.secret}") → 설정 파일의 jwt.secret 값을 꽂아줌.
     */
    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration}") long expirationMs) {
        // 문자열 비밀키 → 시큐리티가 쓰는 SecretKey 객체로 변환
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * 토큰 발급: 로그인 성공 시 호출됨.
     * @param username 누구인지
     * @param role     권한("USER"/"ADMIN")
     * @return 서명된 JWT 문자열
     */
    public String createToken(String username, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)      // 주인(누구인지)
                .claim("role", role)    // 추가 정보(권한)
                .issuedAt(now)          // 발급 시각
                .expiration(expiry)     // 만료 시각
                .signWith(key)          // 비밀키로 서명 → 위조 방지 도장
                .compact();             // 최종 문자열로 압축
    }

    /** 토큰에서 username(subject) 꺼내기 */
    public String getUsername(String token) {
        return parse(token).getSubject();
    }

    /** 토큰에서 role 꺼내기 */
    public String getRole(String token) {
        return parse(token).get("role", String.class);
    }

    /**
     * 토큰이 유효한지 검사(서명 위조·만료 여부).
     * 문제가 있으면 parse에서 예외가 터지므로, 잡아서 false로 돌려줌.
     */
    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 토큰을 열어서(서명 검증 포함) 내용(Claims)을 꺼내는 내부 공통 메서드.
     * 서명이 안 맞거나 만료됐으면 여기서 예외가 발생함.
     */
    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)          // 이 비밀키로 서명 검증
                .build()
                .parseSignedClaims(token) // 파싱(+검증)
                .getPayload();            // payload(내용) 부분
    }
}
