package com.example.practice.music;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 차트를 매일 저장하고, 일간·주간으로 꺼내주는 서비스.
 *
 * ⭐ 전체 그림
 *   Apple은 "지금" 차트만 준다 → 매일 한 번 받아서 날짜와 함께 저장
 *   → 일간 = 그날 저장분 그대로
 *   → 주간 = 최근 7일치를 곡별 평균 순위로 다시 정렬
 *   → 오래된 기록은 자동으로 지운다(무한정 쌓이지 않게)
 */
@Service
public class ChartSnapshotService {

    private static final Logger log = LoggerFactory.getLogger(ChartSnapshotService.class);

    /** 주간 집계에 쓸 기간(일) */
    private static final int WEEK_DAYS = 7;
    /** 주간 차트에 보여줄 곡 수 */
    private static final int WEEKLY_SIZE = 30;
    /** 기록 보관 기간(일). 이보다 오래된 것은 지운다. */
    private static final int KEEP_DAYS = 90;

    private final MusicService musicService;
    private final ChartSnapshotRepository repository;

    public ChartSnapshotService(MusicService musicService, ChartSnapshotRepository repository) {
        this.musicService = musicService;
        this.repository = repository;
    }

    /**
     * 오늘 차트를 저장한다. 이미 저장했으면 아무 것도 하지 않는다.
     *
     * ⭐ 멱등하게 만든 이유
     *   서버를 하루에 여러 번 재시작할 수도 있고, 스케줄러가 두 번 돌 수도 있다.
     *   그때마다 또 저장하면 같은 날짜에 곡이 두 번씩 들어가 평균이 어긋난다.
     *   "이미 있으면 건너뛴다"로 만들어 두면 몇 번을 불러도 결과가 같다.
     */
    @Transactional
    public int saveToday() {
        LocalDate today = LocalDate.now();
        if (repository.existsBySnapshotDate(today)) {
            return 0;
        }

        List<TrackDto> chart = musicService.chart();
        if (chart.isEmpty()) {
            log.warn("chart snapshot skipped: external chart was empty");
            return 0;
        }

        List<ChartSnapshot> rows = new java.util.ArrayList<>();
        for (int i = 0; i < chart.size(); i++) {
            TrackDto t = chart.get(i);
            ChartSnapshot s = new ChartSnapshot();
            s.setSnapshotDate(today);
            s.setRank(i + 1);          // 배열 순서가 그날의 순위
            s.setTrackId(t.id());
            s.setTitle(t.title());
            s.setArtist(t.artist());
            s.setAlbum(t.album());
            s.setAlbumImage(t.albumImage());
            s.setPreviewUrl(t.previewUrl());
            rows.add(s);
        }
        repository.saveAll(rows);
        log.info("chart snapshot saved: {} ({} tracks)", today, rows.size());
        return rows.size();
    }

    /**
     * 일간 차트 — 오늘 저장분.
     *
     * 아직 오늘 것이 없으면(서버를 막 켰거나 저장 전) 가장 최근 저장분을 보여준다.
     * 빈 화면을 주는 것보다 "어제 기준"이라도 보여주는 편이 낫다.
     */
    public List<TrackDto> daily() {
        List<ChartSnapshot> rows = repository.findBySnapshotDateOrderByRankAsc(LocalDate.now());

        if (rows.isEmpty()) {
            LocalDate latest = repository.findFirstByOrderBySnapshotDateDesc()
                    .map(ChartSnapshot::getSnapshotDate)
                    .orElse(null);
            if (latest == null) return List.of();      // 저장된 기록이 아예 없음
            rows = repository.findBySnapshotDateOrderByRankAsc(latest);
        }
        return rows.stream().map(ChartSnapshotService::toTrack).toList();
    }

    /** 주간 차트 — 최근 7일 평균 순위 순 */
    public List<WeeklyTrackDto> weekly() {
        LocalDate from = LocalDate.now().minusDays(WEEK_DAYS - 1L);
        return repository.findWeekly(from, PageRequest.of(0, WEEKLY_SIZE));
    }

    /**
     * 보관 기간이 지난 기록 삭제.
     *
     * 하루 30줄이라 당장은 작지만, 정리하지 않으면 계속 쌓이기만 한다.
     * "쌓이기만 하는 테이블"은 정리 규칙을 처음부터 정해두는 편이 좋다.
     */
    @Transactional
    public long deleteOld() {
        long deleted = repository.deleteBySnapshotDateBefore(LocalDate.now().minusDays(KEEP_DAYS));
        if (deleted > 0) log.info("old chart snapshots deleted: {}", deleted);
        return deleted;
    }

    private static TrackDto toTrack(ChartSnapshot s) {
        return new TrackDto(s.getTrackId(), s.getTitle(), s.getArtist(),
                s.getAlbum(), s.getAlbumImage(), s.getPreviewUrl());
    }
}
