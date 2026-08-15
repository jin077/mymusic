package com.example.practice.comment;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.practice.common.ForbiddenException;
import com.example.practice.common.NotFoundException;
import com.example.practice.notice.Notice;
import com.example.practice.notice.NoticeRepository;

/** 댓글 로직. */
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final NoticeRepository noticeRepository;

    public CommentService(CommentRepository commentRepository, NoticeRepository noticeRepository) {
        this.commentRepository = commentRepository;
        this.noticeRepository = noticeRepository;
    }

    /** 한 공지의 댓글 목록 */
    public List<CommentDto> findByNotice(Long noticeId) {
        return commentRepository.findByNoticeIdOrderByIdAsc(noticeId).stream()
                .map(CommentDto::from)
                .toList();
    }

    /**
     * 내가 쓴 댓글 — 어느 글에 썼는지 제목도 함께 보여준다.
     *
     * ⭐ 댓글마다 공지를 하나씩 조회하면(N+1) 댓글 20개에 쿼리가 21번 나간다.
     *   필요한 공지 번호를 모아 한 번에 조회하고(findAllById), Map으로 만들어 붙인다.
     *   → 쿼리 2번으로 끝난다.
     */
    public List<CommentDto> findMine(String writer) {
        List<Comment> comments = commentRepository.findByWriterOrderByIdDesc(writer);
        if (comments.isEmpty()) return List.of();

        List<Long> noticeIds = comments.stream().map(Comment::getNoticeId).distinct().toList();
        Map<Long, String> titles = noticeRepository.findAllById(noticeIds).stream()
                .collect(Collectors.toMap(Notice::getId, Notice::getTitle));

        return comments.stream()
                .map(c -> CommentDto.from(c, titles.getOrDefault(c.getNoticeId(), "(삭제된 글)")))
                .toList();
    }

    /** 작성 — 없는 공지에는 달 수 없다 */
    @Transactional
    public Comment create(CommentRequest request, String writer) {
        if (request.noticeId() == null || !noticeRepository.existsById(request.noticeId())) {
            throw new NotFoundException("공지를 찾을 수 없습니다: id=" + request.noticeId());
        }
        if (request.content() == null || request.content().isBlank()) {
            throw new IllegalArgumentException("내용을 입력해 주세요.");
        }

        Comment comment = new Comment();
        comment.setNoticeId(request.noticeId());
        comment.setWriter(writer);
        comment.setContent(request.content().trim());
        return commentRepository.save(comment);
    }

    /**
     * 삭제 — 본인 또는 관리자만.
     *
     * ⭐ 화면에서 삭제 버튼을 숨기는 것만으로는 막을 수 없다.
     *   주소로 직접 요청하면 남의 댓글 번호를 넣을 수 있으므로 서버가 작성자를 확인한다.
     */
    @Transactional
    public void delete(Long id, String requester, boolean isAdmin) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("댓글을 찾을 수 없습니다: id=" + id));

        if (!isAdmin && !comment.getWriter().equals(requester)) {
            throw new ForbiddenException("본인이 쓴 댓글만 삭제할 수 있습니다.");
        }
        commentRepository.delete(comment);
    }

    /** 공지가 지워질 때 딸린 댓글도 정리 */
    @Transactional
    public void deleteByNotice(Long noticeId) {
        commentRepository.deleteByNoticeId(noticeId);
    }

    /** 탈퇴 시 정리 */
    @Transactional
    public void deleteByWriter(String writer) {
        commentRepository.deleteByWriter(writer);
    }
}
