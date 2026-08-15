package com.example.practice.music;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChartSnapshotRepository extends JpaRepository<ChartSnapshot, Long> {

    /** 그날 이미 저장했는지 (스케줄러가 두 번 돌아도 중복 저장하지 않게) */
    boolean existsBySnapshotDate(LocalDate date);

    /** 특정 날짜의 차트 (순위 순) */
    List<ChartSnapshot> findBySnapshotDateOrderByRankAsc(LocalDate date);

    /** 저장된 가장 최근 날짜 — 오늘 것이 아직 없을 때 대신 보여주려고 */
    Optional<ChartSnapshot> findFirstByOrderBySnapshotDateDesc();

    /**
     * 주간 차트 — 최근 며칠간의 순위를 곡별로 묶어 평균을 낸다.
     *
     * ⭐ 왜 평균 순위인가
     *   "3일은 1위, 4일은 50위"인 곡과 "7일 내내 10위"인 곡 중 후자가 더 꾸준히 사랑받은 곡이다.
     *   등장 횟수만 세면 하루 반짝 1위 한 곡이 밀려나고,
     *   순위만 보면 하루치가 전체를 대표해 버린다. 평균이 둘을 함께 반영한다.
     *
     * 곡 정보(제목·가수 등)는 같은 trackId면 같은 값이라 group by에 함께 넣어도 결과가 같다.
     */
    @Query("""
            select new com.example.practice.music.WeeklyTrackDto(
                s.trackId, s.title, s.artist, s.album, s.albumImage, s.previewUrl,
                avg(s.rank), count(s))
            from ChartSnapshot s
            where s.snapshotDate >= :from
            group by s.trackId, s.title, s.artist, s.album, s.albumImage, s.previewUrl
            order by avg(s.rank) asc
            """)
    List<WeeklyTrackDto> findWeekly(@Param("from") LocalDate from, Pageable pageable);

    /** 오래된 기록 정리 (보관 기간이 지난 것) */
    long deleteBySnapshotDateBefore(LocalDate date);
}
