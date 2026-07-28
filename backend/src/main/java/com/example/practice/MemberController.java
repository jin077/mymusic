package com.example.practice;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 회원 관련 API (일반 사용자용).
 * 이제 Controller는 요청만 받고, 실제 처리는 MemberService에 맡김.
 *   Controller(요청받기) → Service(로직) → Repository(DB)
 */
@RestController
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // 회원가입  →  POST /signup  (JSON: {"username":"...","password":"..."})
    @PostMapping("/signup")
    public String signup(@RequestBody Member member) {
        memberService.register(member.getUsername(), member.getPassword());
        return "회원가입 완료: " + member.getUsername();
    }
}
