package com.example.practice.notice;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.practice.comment.CommentRepository;
import com.example.practice.common.NotFoundException;

/** 공지사항 로직. */
@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final CommentRepository commentRepository;

    public NoticeService(NoticeRepository noticeRepository, CommentRepository commentRepository) {
        this.noticeRepository = noticeRepository;
        this.commentRepository = commentRepository;
    }

    /** 목록 (최신순) */
    public List<Notice> findAll() {
        return noticeRepository.findAllByOrderByIdDesc();
    }

    /** 쪽 단위 목록 (최신순) */
    public Page<Notice> findPage(int page, int size) {
        return noticeRepository.findAllByOrderByIdDesc(PageRequest.of(page, size));
    }

    /** 수정 — 제목·내용만 바꾼다. 작성자와 작성일은 그대로 둔다. */
    @Transactional
    public Notice update(Long id, NoticeRequest request) {
        if (request.title() == null || request.title().isBlank()
                || request.content() == null || request.content().isBlank()) {
            throw new IllegalArgumentException("제목과 내용을 입력해 주세요.");
        }
        Notice notice = findById(id);
        notice.setTitle(request.title().trim());
        notice.setContent(request.content().trim());
        return notice;   // 영속 상태라 트랜잭션이 끝날 때 UPDATE가 나간다
    }

    /** 글 하나 — 없으면 404 */
    public Notice findById(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("공지를 찾을 수 없습니다: id=" + id));
    }

    /** 작성 — writer는 요청이 아니라 토큰에서 온 값을 넣는다 */
    @Transactional
    public Notice create(NoticeRequest request, String writer) {
        Notice notice = new Notice();
        notice.setTitle(request.title());
        notice.setContent(request.content());
        notice.setWriter(writer);
        return noticeRepository.save(notice);
    }

    /** 삭제 — 없으면 404. 딸린 댓글도 함께 지운다. */
    @Transactional
    public void delete(Long id) {
        if (!noticeRepository.existsById(id)) {
            throw new NotFoundException("공지를 찾을 수 없습니다: id=" + id);
        }
        commentRepository.deleteByNoticeId(id);   // 글이 사라지면 댓글도 갈 곳이 없다
        noticeRepository.deleteById(id);
    }
}
