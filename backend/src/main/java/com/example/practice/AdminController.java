package com.example.practice;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자(ADMIN) 전용 API.
 * 이 컨트롤러의 모든 주소(/admin/**)는 SecurityConfig에서
 * "ROLE_ADMIN 권한이 있어야만 접근 가능"하도록 막을 것임.
 * → 일반 USER가 접근하면 403(Forbidden)으로 거부됨.
 *
 * 커뮤니티로 치면 "매니저가 유저를 밴하는" 딱 그 기능.
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final MemberService memberService;

    public AdminController(MemberService memberService) {
        this.memberService = memberService;
    }

    // 전체 회원 목록  →  GET  /admin/members   (관리자만)
    //
    // ⭐ 반환형을 List<Member> → List<MemberDto> 로 바꿨다.
    //   기존에는 Member 엔티티를 그대로 돌려줘서 응답 JSON에 password 해시가 노출됐다.
    //   MemberDto는 id·username·role만 담으므로 비밀번호가 절대 밖으로 나가지 않는다.
    //   (엔티티 = DB용 / DTO = 응답용 으로 분리하는 것이 실무의 기본)
    @GetMapping("/members")
    public List<MemberDto> members() {
        return memberService.findAll().stream()
                .map(MemberDto::from)
                .toList();
    }

    // 회원 밴(삭제)  →  DELETE /admin/members/3   (관리자만)
    @DeleteMapping("/members/{id}")
    public String ban(@PathVariable Long id) {
        memberService.ban(id);
        return "밴(삭제) 완료: id=" + id;
    }
}
