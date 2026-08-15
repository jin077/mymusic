package com.example.practice.member;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 회원 관련 API (일반 사용자용).
 * Controller는 요청만 받고, 실제 처리는 MemberService에 맡김.
 *   Controller(요청받기) → Service(로직) → Repository(DB)
 */
@RestController
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * 회원가입  →  POST /api/signup
     *
     * ⭐ 201 Created 로 응답한다.
     *   200(OK)도 동작은 하지만, 201은 "새 자원이 만들어졌다"는 뜻이라 의미가 더 정확하다.
     *   중복 아이디면 서비스가 409를 던진다.
     */
    @PostMapping("/signup")
    public ResponseEntity<MemberDto> signup(@RequestBody SignupRequest request) {
        Member saved = memberService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(MemberDto.from(saved));
    }

    /**
     * 내 정보 조회  →  GET /api/members/me
     *
     * ⭐ 주소에 id를 받지 않고 "me"로 고정한 이유
     *   /members/3 처럼 id를 받으면 남의 번호를 넣어 조회를 시도할 수 있다.
     *   대상을 요청에서 받지 않고 토큰에서 꺼내면 그런 시도 자체가 불가능하다.
     *   Authentication은 JwtAuthenticationFilter가 토큰을 검증한 뒤 넣어둔 것이다.
     */
    @GetMapping("/members/me")
    public MemberDto me(Authentication authentication) {
        return MemberDto.from(memberService.findByUsername(authentication.getName()));
    }

    /** 내 정보 수정 (닉네임·이메일)  →  PUT /api/members/me */
    @PutMapping("/members/me")
    public MemberDto updateMe(Authentication authentication,
                              @RequestBody MemberUpdateRequest request) {
        return MemberDto.from(
                memberService.updateProfile(authentication.getName(), request));
    }

    /**
     * 비밀번호 변경  →  PUT /api/members/me/password
     * 본문 {"currentPassword":"...", "newPassword":"..."}
     *
     * ⭐ 아이디(username) 변경 API는 일부러 만들지 않았다.
     *   아이디는 로그인 식별자이자 토큰의 주체(sub)이고,
     *   플레이리스트·재생기록·댓글·구매내역이 모두 아이디 문자열로 연결돼 있다.
     *   바꾸면 그 데이터들이 전부 주인을 잃고, 이미 발급된 토큰도 어긋난다.
     *   → 실제 서비스에서도 아이디는 대개 변경 불가다. 바꾸고 싶으면 닉네임을 쓴다.
     *
     * 실패 응답
     *   403 현재 비밀번호 불일치
     *   400 새 비밀번호가 너무 짧음
     */
    @PutMapping("/members/me/password")
    public ResponseEntity<Void> changePassword(Authentication authentication,
                                               @RequestBody PasswordChangeRequest request) {
        memberService.changePassword(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }

    /**
     * 캐시 충전  →  POST /api/members/me/charge   본문 {"amount": 1000}
     *
     * 실제 결제 연동(PG)은 붙이지 않았다. 이 자리에 결제 검증이 들어간다.
     */
    @PostMapping("/members/me/charge")
    public MemberDto charge(Authentication authentication,
                            @RequestBody Map<String, Long> body) {
        Long amount = body.get("amount");
        return MemberDto.from(
                memberService.charge(authentication.getName(), amount == null ? 0 : amount));
    }

    /**
     * 프로필 사진 업로드  →  POST /api/members/me/profile-image
     *
     * ⭐ 이 요청만 JSON이 아니라 multipart/form-data 로 온다.
     *   JSON은 글자만 담을 수 있어 이미지 같은 이진 데이터를 그대로 넣지 못한다.
     *   (Base64로 바꿔 넣을 수는 있지만 용량이 33% 늘어난다)
     *   그래서 파일 전송에는 multipart 형식을 쓰고, @RequestParam으로 받는다.
     */
    @PostMapping("/members/me/profile-image")
    public MemberDto uploadProfileImage(Authentication authentication,
                                        @RequestParam("file") MultipartFile file) {
        return MemberDto.from(
                memberService.changeProfileImage(authentication.getName(), file));
    }

    /** 프로필 사진 삭제  →  DELETE /api/members/me/profile-image */
    @DeleteMapping("/members/me/profile-image")
    public MemberDto deleteProfileImage(Authentication authentication) {
        return MemberDto.from(
                memberService.removeProfileImage(authentication.getName()));
    }

    /**
     * 회원 탈퇴  →  DELETE /api/members/me
     *
     * 관리자용 삭제(/api/admin/members/{id})와 주소가 다르다.
     *   관리자 : 아무나 지울 수 있어야 하므로 id를 받는다 → ADMIN 권한 필요
     *   본인   : 대상이 언제나 자기 자신이라 id를 받지 않는다 → 로그인만 하면 된다
     */
    @DeleteMapping("/members/me")
    public ResponseEntity<Void> withdraw(Authentication authentication) {
        memberService.withdraw(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
