package com.example.practice.comment;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 댓글 API.
 *
 * ⭐ 주소를 /api/notices/{id}/comments 로 하지 않은 이유
 *   SecurityConfig에 "GET 외의 /api/notices/** 는 ADMIN만"이라는 규칙이 있다.
 *   댓글을 그 아래 두면 일반 회원이 댓글을 못 쓴다.
 *   → 권한 규칙이 다른 기능은 주소도 분리하는 편이 안전하다.
 *
 * 권한
 *   GET  /api/comments?noticeId=3  누구나 (공지를 로그인 없이 볼 수 있으므로 댓글도)
 *   POST /api/comments             로그인한 회원
 *   GET  /api/comments/me          본인
 *   DELETE /api/comments/3         본인 또는 관리자 (서버에서 작성자 확인)
 */
@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /** 한 공지의 댓글 목록 */
    @GetMapping
    public List<CommentDto> list(@RequestParam Long noticeId) {
        return commentService.findByNotice(noticeId);
    }

    /** 내가 쓴 댓글 (어느 글에 썼는지 제목 포함) */
    @GetMapping("/me")
    public List<CommentDto> mine(Authentication authentication) {
        return commentService.findMine(authentication.getName());
    }

    /** 댓글 작성 */
    @PostMapping
    public ResponseEntity<CommentDto> create(@RequestBody CommentRequest request,
                                             Authentication authentication) {
        Comment saved = commentService.create(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(CommentDto.from(saved));
    }

    /** 댓글 삭제 (본인 또는 관리자) */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        commentService.delete(id, authentication.getName(), isAdmin);
        return ResponseEntity.noContent().build();
    }
}
