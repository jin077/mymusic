package com.example.practice.common;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

/**
 * 목록 응답의 공통 형식 (페이징).
 *
 * ⭐ 왜 목록을 통째로 주지 않는가
 *   글이 1000개면 1000개가 전부 내려간다. DB도, 네트워크도, 브라우저도 모두 부담이다.
 *   "한 번에 몇 개씩" 잘라 주고, 화면이 필요할 때 다음 쪽을 요청하게 한다.
 *
 * ⭐ 왜 스프링의 Page를 그대로 돌려주지 않는가
 *   Page는 내부 구조가 그대로 JSON이 되어 프론트가 스프링 형식에 묶인다.
 *   (스프링 버전이 올라가며 형식이 바뀐 적도 있다)
 *   → 우리가 정한 형식으로 감싸서 내보낸다. 응답에 DTO를 쓰는 것과 같은 이유다.
 */
public record PageResponse<T>(
        List<T> content,     // 이번 쪽의 목록
        int page,            // 지금 쪽 번호 (0부터)
        int size,            // 한 쪽에 몇 개
        long totalElements,  // 전체 개수
        int totalPages,      // 전체 쪽 수
        boolean first,
        boolean last
) {
    /** 엔티티 Page를 DTO Page로 바꾼다. mapper는 엔티티 → DTO 변환 함수. */
    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast());
    }
}
