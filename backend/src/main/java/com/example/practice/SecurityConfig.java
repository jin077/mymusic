package com.example.practice;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * 시큐리티 설정 (3단계: JWT 인증).
 *
 * 이전(세션 방식): 로그인 페이지(formLogin) + 서버가 세션에 로그인 상태 저장.
 * 이번(JWT 방식): 서버는 상태를 저장하지 않고(stateless), 요청마다 토큰으로 인증.
 */
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 비밀번호 암호화기(BCrypt). 회원가입 시 암호화, 로그인 시 대조에 사용.
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager를 빈으로 꺼내 등록.
     *   → AuthController가 로그인 검증에 쓸 수 있도록.
     *   내부적으로 CustomUserDetailsService + PasswordEncoder를 이용해 인증한다.
     */
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // ⭐ CORS 켜기 → 아래 corsConfigurationSource() 빈의 규칙을 사용함
            //   (React(3000)에서 백엔드(8080)로 오는 요청을 허용하기 위함)
            .cors(Customizer.withDefaults())
            // JWT는 브라우저 세션 쿠키를 안 쓰므로 CSRF 불필요 → 끔
            .csrf(AbstractHttpConfigurer::disable)
            // ⭐ 세션을 만들지 않음(STATELESS) = JWT의 핵심. 서버가 로그인 상태를 기억 안 함.
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/signup", "/api/login").permitAll() // 가입·로그인은 토큰 없이 허용
                // ⭐ /error 를 반드시 열어둘 것.
                //   스프링은 요청 처리 중 오류가 나면 내부적으로 /error 로 다시 넘긴다(ERROR 디스패치).
                //   이때 JWT 필터는 기본적으로 다시 실행되지 않아 '비로그인' 상태가 되고,
                //   /error 가 인증을 요구하면 원래 오류(500·502 등)가 403으로 덮여버린다.
                //   → 실제 오류 원인을 못 보게 되므로 반드시 permitAll 로 열어둔다.
                .requestMatchers("/error").permitAll()
                // ⭐ 음악 조회(차트·검색·앨범)는 공개 정보 → 로그인 없이 허용
                //    회원 기능만 인증을 요구하는 것이 실제 음악 서비스의 방식이다.
                .requestMatchers("/api/music/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")    // /api/admin/** 은 관리자만
                .anyRequest().authenticated()                     // 그 외는 인증 필요
            )
            // ⭐ 우리 JWT 문지기 필터를, 기본 로그인 필터보다 "앞"에 끼워넣음.
            //    → 요청이 들어오면 우리 필터가 먼저 토큰을 보고 인증 상태를 세팅.
            .addFilterBefore(jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS 규칙: "어떤 출처(프론트)를, 어떤 방식으로 허용할지" 정의.
     *   위 filterChain의 .cors(...)가 이 빈을 찾아서 사용한다.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 허용할 프론트 주소 (React 개발서버). 여러 개면 리스트에 추가.
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        // 허용할 HTTP 방식
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 허용할 요청 헤더 (Authorization 헤더 등 전부 허용)
        config.setAllowedHeaders(List.of("*"));
        // 인증정보(쿠키/Authorization) 허용
        config.setAllowCredentials(true);

        // 모든 경로(/**)에 위 규칙 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
