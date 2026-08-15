package com.example.practice.common;

import java.time.LocalDateTime;

import com.example.practice.member.Member;
import com.example.practice.member.MemberRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 앱이 시작될 때 딱 한 번 실행되는 초기화 코드 (CommandLineRunner).
 *
 * 관리자 계정은 회원가입(/signup)으로는 만들 수 없게 막아뒀으므로,
 * 여기서 앱 시작 시 admin 계정이 없으면 자동으로 하나 만들어줌.
 *   → 아이디: admin / 비번: admin1234 / 권한: ADMIN
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * ⭐ @Transactional을 여기(run)에 붙인 이유 — 자기 호출(self-invocation) 함정
     *   스프링의 @Transactional은 프록시(대리 객체)가 앞에서 트랜잭션을 열어주는 방식이다.
     *   그런데 같은 클래스 안에서 backfillCreatedAt()을 직접 부르면 프록시를 거치지 않는다.
     *   → 그 메서드에 @Transactional을 붙여도 아무 일도 일어나지 않는다.
     *   run()은 스프링이 바깥에서 호출하므로 프록시를 타고, 트랜잭션이 실제로 열린다.
     */
    @Override
    @Transactional
    public void run(String... args) {
        // 이미 admin이 있으면 또 만들지 않음 (중복 방지)
        if (memberRepository.findByUsername("admin").isEmpty()) {
            Member admin = new Member();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin1234"));
            admin.setRole("ADMIN");
            memberRepository.save(admin);
            System.out.println(">>> 관리자 계정 자동 생성: admin / admin1234");
        }

        backfillCreatedAt();
    }

    /**
     * 가입일이 비어 있는 회원을 채운다.
     *
     * ⭐ 왜 필요한가
     *   createdAt 컬럼을 나중에 추가했다. ddl-auto=update는 컬럼을 "만들어주기만" 하고
     *   기존 행의 값은 비워 둔다(NULL). 그래서 예전 회원은 가입일이 없다.
     *
     * ⭐ 실무에서는 이런 작업을 어떻게 하나
     *   보통 Flyway·Liquibase 같은 마이그레이션 도구로 SQL 스크립트를 버전 관리한다.
     *   지금은 학습용이라 앱 시작 시 한 번 채우는 방식으로 대신했다.
     *   (이미 값이 있으면 건드리지 않으므로 여러 번 실행해도 안전하다 = 멱등)
     *
     * ⚠️ 진짜 가입 시각은 알 수 없으므로 현재 시각으로 채운다.
     */
    private void backfillCreatedAt() {
        if (memberRepository.countByCreatedAtIsNull() == 0) return;

        int filled = memberRepository.fillEmptyCreatedAt(LocalDateTime.now());
        System.out.println(">>> 가입일이 비어 있던 회원 " + filled + "명을 채웠습니다.");
    }
}
