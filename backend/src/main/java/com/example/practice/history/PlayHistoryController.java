package com.example.practice.history;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.practice.music.TrackDto;

/**
 * 재생 기록 API. 로그인한 사람 본인의 기록만 다룬다.
 *
 * 기록은 사용자가 직접 남기는 것이 아니라, 곡을 재생하면 화면이 자동으로 보낸다.
 *   → 로그인하지 않은 사람의 재생은 기록되지 않는다(누구 것인지 알 수 없으므로).
 */
@RestController
@RequestMapping("/history")
public class PlayHistoryController {

    private final PlayHistoryService historyService;

    public PlayHistoryController(PlayHistoryService historyService) {
        this.historyService = historyService;
    }

    /**
     * 재생 기록 남기기  →  POST /api/history
     *
     * 응답 본문이 없다(204). 화면은 이 요청의 결과를 기다리지 않고 바로 재생을 시작한다.
     * 기록이 실패해도 음악 재생은 계속되어야 하기 때문이다.
     */
    @PostMapping
    public ResponseEntity<Void> record(@RequestBody TrackDto track,
                                       Authentication authentication) {
        historyService.record(authentication.getName(), track);
        return ResponseEntity.noContent().build();
    }

    /** 최근 재생한 곡  →  GET /api/history/recent */
    @GetMapping("/recent")
    public List<TrackDto> recent(Authentication authentication) {
        return historyService.recent(authentication.getName());
    }

    /** 많이 들은 곡  →  GET /api/history/top */
    @GetMapping("/top")
    public List<TopTrackDto> top(Authentication authentication) {
        return historyService.top(authentication.getName());
    }

    /** 기록 전체 삭제  →  DELETE /api/history */
    @DeleteMapping
    public ResponseEntity<Void> clear(Authentication authentication) {
        historyService.clear(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
