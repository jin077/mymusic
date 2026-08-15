package com.example.practice.music;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 음악 API.
 *
 * WebConfig가 모든 @RestController에 /api 접두사를 붙이므로 실제 주소는 아래와 같다:
 *   GET /api/music/chart          인기곡 차트
 *   GET /api/music/search?q=아이유  곡 검색
 *   GET /api/music/albums         인기 앨범
 *   GET /api/music/albums/new     최신 앨범
 *
 * 이 API들은 로그인 없이 열어둔다(SecurityConfig에서 permitAll).
 *   → 음악 검색·차트는 누구나 볼 수 있어야 하는 공개 정보이기 때문.
 *     회원 기능(마이페이지·관리자)만 인증을 요구한다.
 */
@RestController
@RequestMapping("/music")
public class MusicController {

    private final MusicService musicService;
    private final ChartSnapshotService snapshotService;

    public MusicController(MusicService musicService, ChartSnapshotService snapshotService) {
        this.musicService = musicService;
        this.snapshotService = snapshotService;
    }

    /** 실시간 차트 — 외부 API를 그대로(캐시 10분) */
    @GetMapping("/chart")
    public List<TrackDto> chart() {
        return musicService.chart();
    }

    /** 일간 차트 — 오늘 저장해 둔 기록 (없으면 가장 최근 저장분) */
    @GetMapping("/chart/daily")
    public List<TrackDto> daily() {
        return snapshotService.daily();
    }

    /** 주간 차트 — 최근 7일 평균 순위 */
    @GetMapping("/chart/weekly")
    public List<WeeklyTrackDto> weekly() {
        return snapshotService.weekly();
    }

    /** q : 검색어. 값이 없으면 빈 목록을 돌려준다(=외부 API를 부르지 않음). */
    @GetMapping("/search")
    public List<TrackDto> search(@RequestParam(name = "q", defaultValue = "") String q) {
        if (q.isBlank()) return List.of();
        return musicService.search(q);
    }

    /** 인기 앨범 (Apple 앨범 차트 순위 순) */
    @GetMapping("/albums")
    public List<AlbumDto> albums() {
        return musicService.albums();
    }

    /** 최신 앨범 (같은 목록을 발매일 순으로 정렬) */
    @GetMapping("/albums/new")
    public List<AlbumDto> newAlbums() {
        return musicService.newAlbums();
    }

    /**
     * 앨범 수록곡  →  GET /api/music/albums/1234567890/tracks
     *
     * 주소를 /albums/{id} 로 하지 않고 뒤에 /tracks를 붙인 이유:
     *   /albums/new 와 /albums/{id} 가 같은 모양이라 "new"가 id로 잡힐 수 있다.
     *   경로를 한 단계 더 두면 헷갈릴 일이 없다.
     */
    @GetMapping("/albums/{albumId}/tracks")
    public List<TrackDto> albumTracks(@PathVariable String albumId) {
        return musicService.albumTracks(albumId);
    }
}
