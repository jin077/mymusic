package com.example.practice;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 회원 관련 "일 처리(비즈니스 로직)"를 담당하는 Service 계층.
 *
 * Controller → Service → Repository 3계층 구조에서 가운데.
 * Controller는 요청만 받고, 실제 로직(암호화, 권한 고정 등)은 여기서 처리.
 */
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 일반 회원가입.
     * ⭐ 보안 포인트: role을 무조건 "USER"로 고정함.
     *   → 해커가 회원가입 요청에 role="ADMIN"을 몰래 끼워넣어도 무시됨.
     *   → 관리자는 회원가입으로 못 만들고, 별도 방법으로만 만들어짐.
     */
    public Member register(String username, String rawPassword) {
        Member member = new Member();
        member.setUsername(username);
        member.setPassword(passwordEncoder.encode(rawPassword)); // BCrypt 암호화
        member.setRole("USER");                                  // 권한 강제 고정
        return memberRepository.save(member);
    }

    /** 전체 회원 조회 (관리자 기능) */
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    /** 회원 밴(삭제) — 관리자 기능 */
    public void ban(Long id) {
        memberRepository.deleteById(id);
    }
}
