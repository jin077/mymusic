package com.example.practice.comment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    /** 한 공지에 달린 댓글 (오래된 순 = 대화 순서) */
    List<Comment> findByNoticeIdOrderByIdAsc(Long noticeId);

    /** 내가 쓴 댓글 (최신순) */
    List<Comment> findByWriterOrderByIdDesc(String writer);

    /** 공지를 지울 때 딸린 댓글도 정리 */
    void deleteByNoticeId(Long noticeId);

    /** 탈퇴 시 정리 */
    void deleteByWriter(String writer);
}
