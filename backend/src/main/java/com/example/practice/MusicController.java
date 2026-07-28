package com.example.practice;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 음악 API.
 *
 * WebConfig가 모든 @RestController에 /api 접두사를 붙이므로 실제 주소는 아래와 같다:
 *   GET /api/music/chart          인기곡 차트
 *   GET /api/music/search?q=아이유  곡 검색
 *   GET /api/music/albums         앨범 목록
 *
 * 이 API들은 로그인 없이 열어둔다(SecurityConfig에서 permitAll).
 *   → 음악 검색·차트는 누구나 볼 수 있어야 하는 공개 정보이기 때문.
 *     회원 기능(마이페이지·관리자)만 인증을 요구한다.
 */
@RestController
@RequestMapping("/music")
public class MusicController {

    private final MusicService musicService;

    public MusicController(MusicService musicService) {
        this.musicService = musicService;
    }

    @GetMapping("/chart")
    public List<TrackDto> chart() {
        return musicService.chart();
    }

    /** q : 검색어. 값이 없으면 빈 목록을 돌려준다(=외부 API를 부르지 않음). */
    @GetMapping("/search")
    public List<TrackDto> search(@RequestParam(name = "q", defaultValue = "") String q) {
        if (q.isBlank()) return List.of();
        return musicService.search(q);
    }

    @GetMapping("/albums")
    public List<AlbumDto> albums() {
        return musicService.albums();
    }
}
