package com.example.practice;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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

    @Override
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
    }
}
