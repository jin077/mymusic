package com.example.practice.member;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자(ADMIN) 전용 API.
 * 이 컨트롤러의 모든 주소(/admin/**)는 SecurityConfig에서
 * "ROLE_ADMIN 권한이 있어야만 접근 가능"하도록 막혀 있다.
 * → 일반 USER가 접근하면 403(Forbidden)으로 거부됨.
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final MemberService memberService;

    public AdminController(MemberService memberService) {
        this.memberService = memberService;
    }

    // 전체 회원 목록  →  GET  /api/admin/members
    //
    // ⭐ 반환형이 List<Member>가 아니라 List<MemberDto>인 이유
    //   엔티티를 그대로 돌려주면 응답 JSON에 password 해시가 노출된다.
    //   MemberDto는 id·username·role·nickname·email만 담는다.
    @GetMapping("/members")
    public List<MemberDto> members() {
        return memberService.findAll().stream()
                .map(MemberDto::from)
                .toList();
    }

    /**
     * 회원 권한 변경  →  PUT /api/admin/members/3   본문 {"role":"ADMIN"}
     *
     * 관리자만 권한을 바꿀 수 있다. 본인 수정(PUT /api/members/me)에는
     * role 항목이 아예 없으므로, 일반 회원은 스스로 관리자가 될 수 없다.
     */
    @PutMapping("/members/{id}")
    public MemberDto changeRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return MemberDto.from(memberService.changeRole(id, body.get("role")));
    }

    /**
     * 회원 삭제  →  DELETE /api/admin/members/3
     *
     * ⭐ 204 No Content 로 응답한다.
     *   삭제 성공에는 돌려줄 내용이 없다. 예전에는 "밴(삭제) 완료: id=3" 문자열을
     *   돌려줬지만 프론트가 쓰지 않고 버리고 있었다.
     *   없는 id면 서비스가 404를 던진다.
     */
    @DeleteMapping("/members/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        memberService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
