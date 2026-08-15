package com.example.practice.notice;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 공지 리포지토리.
 *
 * findAllByOrderByIdDesc : 메서드 이름만으로 "ORDER BY id DESC" SQL이 만들어진다.
 *   → 최신 글이 목록 맨 위에 오게 한다.
 */
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    /** 전체 목록 (최신순) — 페이징 없이 쓸 때 */
    List<Notice> findAllByOrderByIdDesc();

    /**
     * 쪽 단위 목록.
     *
     * Pageable을 넘기면 스프링이 LIMIT/OFFSET을 붙여주고,
     * 전체 개수를 세는 COUNT 쿼리도 함께 실행해 Page에 담아준다.
     */
    Page<Notice> findAllByOrderByIdDesc(Pageable pageable);
}
