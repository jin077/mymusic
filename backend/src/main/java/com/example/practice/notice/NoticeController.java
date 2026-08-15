package com.example.practice.notice;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.practice.common.PageResponse;

/**
 * 공지사항 API.
 *
 * ⭐ 읽기와 쓰기의 권한이 다르다.
 *   조회(GET)  : 누구나 (로그인 없이도 공지는 봐야 한다)
 *   작성·삭제  : ADMIN만
 *   → 이 규칙은 SecurityConfig에서 HTTP 메서드별로 지정한다.
 *     화면에서 글쓰기 버튼을 숨기는 것은 안내일 뿐, 실제 차단은 여기서 이뤄진다.
 */
@RestController
@RequestMapping("/notices")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    /**
     * 목록  →  GET /api/notices?page=0&size=10
     *
     * page·size를 안 보내면 기본값(0쪽, 10개)이 쓰인다.
     */
    @GetMapping
    public PageResponse<NoticeDto> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return PageResponse.of(noticeService.findPage(page, size), NoticeDto::from);
    }

    /** 글 하나  →  GET /api/notices/3 */
    @GetMapping("/{id}")
    public NoticeDto detail(@PathVariable Long id) {
        return NoticeDto.from(noticeService.findById(id));
    }

    /** 작성 (ADMIN)  →  POST /api/notices */
    @PostMapping
    public ResponseEntity<NoticeDto> create(@RequestBody NoticeRequest request,
                                            Authentication authentication) {
        Notice saved = noticeService.create(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(NoticeDto.from(saved));
    }

    /** 수정 (ADMIN)  →  PUT /api/notices/3 */
    @PutMapping("/{id}")
    public NoticeDto update(@PathVariable Long id, @RequestBody NoticeRequest request) {
        return NoticeDto.from(noticeService.update(id, request));
    }

    /** 삭제 (ADMIN)  →  DELETE /api/notices/3 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        noticeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
