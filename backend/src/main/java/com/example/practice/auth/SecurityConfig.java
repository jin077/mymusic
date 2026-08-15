package com.example.practice.auth;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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

import jakarta.servlet.http.HttpServletResponse;

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
                // ⭐ 업로드된 프로필 사진은 열어둔다.
                //   브라우저가 <img src="/uploads/...">로 불러올 때는 Authorization 헤더를
                //   보내지 않기 때문에, 막아두면 로그인한 사용자에게도 사진이 깨져 보인다.
                .requestMatchers("/uploads/**").permitAll()
                // ⭐ 음악 조회(차트·검색·앨범)는 공개 정보 → 로그인 없이 허용
                //    회원 기능만 인증을 요구하는 것이 실제 음악 서비스의 방식이다.
                .requestMatchers("/api/music/**").permitAll()
                // ⭐ 공지사항은 "읽기"와 "쓰기"의 권한이 다르다.
                //    조회는 로그인 없이도 가능해야 하지만, 작성·삭제는 관리자만이어야 한다.
                //    → 같은 주소라도 HTTP 메서드로 규칙을 나눈다.
                //    (프론트에서 글쓰기 버튼을 숨기는 것은 안내일 뿐, 실제 차단은 여기서 한다)
                .requestMatchers(HttpMethod.GET, "/api/notices/**").permitAll()
                .requestMatchers("/api/notices/**").hasRole("ADMIN")
                // ⭐ 댓글 조회는 공개, 작성·삭제는 로그인 필요.
                //   주소를 정확히 "/api/comments" 로만 열어야 한다.
                //   "/api/comments/**" 로 열면 내 댓글 목록(/me)까지 공개돼 버린다.
                .requestMatchers(HttpMethod.GET, "/api/comments").permitAll()
                // 이용권 가격표는 로그인 전에도 보여야 한다 (구매는 로그인 필요)
                .requestMatchers(HttpMethod.GET, "/api/tickets/plans").permitAll()
                // 쪽지는 로그인한 회원끼리 주고받는다(문의·안내 용도).
                //   받은 쪽지 조회·읽음·삭제는 아래 anyRequest().authenticated() 로 본인만 가능하고,
                //   "누구 것인지"는 서버가 토큰에서 꺼내 판단한다.
                //   ⚠️ 회원 간 발송을 열면 스팸 문제가 따라온다.
                //      실서비스라면 하루 발송 수 제한·차단·신고 기능이 함께 필요하다.
                .requestMatchers("/api/admin/**").hasRole("ADMIN")    // /api/admin/** 은 관리자만
                // 내 정보(/api/members/me)·플레이리스트(/api/playlist/**)는
                // 아래 anyRequest().authenticated() 에 걸려 로그인만 하면 접근된다.
                .anyRequest().authenticated()                     // 그 외는 인증 필요
            )
            // ⭐ 인증 실패(401) 와 인가 실패(403) 를 제대로 구분해서 응답한다.
            //
            //   왜 필요한가?
            //     시큐리티는 "인증 실패 시 뭘 할지" 담당자(AuthenticationEntryPoint)를 하나 갖는다.
            //     formLogin()이면 로그인 페이지로 보내고, httpBasic()이면 401을 낸다.
            //     그런데 JWT로 바꾸면서 둘 다 제거했으므로 기본값인 Http403ForbiddenEntryPoint가
            //     쓰이고, 그 결과 "비번 틀림"·"토큰 없음"까지 전부 403으로 나와버렸다.
            //     → 401과 403이 구분되지 않아 프론트가 대응을 못 한다.
            //
            //   401 : 네가 누군지 모르겠다 (토큰 없음·만료·위조, 비번 틀림)
            //         → 프론트는 로그인 페이지로 보내야 함
            //   403 : 누군진 알겠는데 자격이 없다 (USER가 /admin 접근)
            //         → 다시 로그인해도 소용없음. "권한 없음" 안내만 하면 됨
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((req, res, e) ->
                            res.sendError(HttpServletResponse.SC_UNAUTHORIZED))  // 401
                    .accessDeniedHandler((req, res, e) ->
                            res.sendError(HttpServletResponse.SC_FORBIDDEN))     // 403
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
