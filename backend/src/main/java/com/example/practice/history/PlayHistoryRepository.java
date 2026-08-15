package com.example.practice.history;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlayHistoryRepository extends JpaRepository<PlayHistory, Long> {

    /** 내가 최근 재생한 순서대로 (같은 곡이 여러 번 들어 있을 수 있다 → 서비스에서 정리) */
    List<PlayHistory> findByUsernameOrderByPlayedAtDesc(String username, Pageable pageable);

    /**
     * 많이 들은 곡 — 곡별로 묶어 센다.
     *
     * ⭐ 왜 이건 메서드 이름만으로 안 되고 직접 쿼리를 쓰는가
     *   findBy... 규칙은 "조건으로 걸러 정렬"까지만 만들어준다.
     *   GROUP BY로 묶어 세는 집계는 표현할 방법이 없어 JPQL을 직접 쓴다.
     *
     * ⭐ group by에 모든 필드를 넣은 이유
     *   SQL에서 select 목록의 컬럼은 group by에 있거나 집계 함수여야 한다.
     *   같은 trackId면 제목·가수도 어차피 같으므로 함께 묶어도 결과가 달라지지 않는다.
     *
     * Pageable로 개수를 제한한다(예: 상위 20곡). LIMIT을 JPQL에 직접 쓸 수 없기 때문.
     */
    @Query("""
            select new com.example.practice.history.TopTrackDto(
                h.trackId, h.title, h.artist, h.album, h.albumImage, h.previewUrl, count(h))
            from PlayHistory h
            where h.username = :username
            group by h.trackId, h.title, h.artist, h.album, h.albumImage, h.previewUrl
            order by count(h) desc
            """)
    List<TopTrackDto> findTopTracks(@Param("username") String username, Pageable pageable);

    /** 탈퇴·기록 삭제용 */
    void deleteByUsername(String username);
}
