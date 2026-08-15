package com.example.practice.history;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.practice.music.TrackDto;

/** 재생 기록 로직. 모든 메서드가 username을 받아 "내 기록만" 다룬다. */
@Service
public class PlayHistoryService {

    private static final int SCAN_SIZE = 200;   // 최근 기록을 훑어볼 범위
    private static final int RECENT_SIZE = 30;  // 화면에 보여줄 최근 곡 수
    private static final int TOP_SIZE = 20;     // 많이 들은 곡 수

    private final PlayHistoryRepository repository;

    public PlayHistoryService(PlayHistoryRepository repository) {
        this.repository = repository;
    }

    /** 재생할 때마다 한 줄 기록 */
    @Transactional
    public void record(String username, TrackDto track) {
        if (track == null || track.id() == null) return;

        PlayHistory history = new PlayHistory();
        history.setUsername(username);
        history.setTrackId(track.id());
        history.setTitle(track.title());
        history.setArtist(track.artist());
        history.setAlbum(track.album());
        history.setAlbumImage(track.albumImage());
        history.setPreviewUrl(track.previewUrl());
        repository.save(history);
    }

    /**
     * 최근 재생한 곡.
     *
     * ⭐ 같은 곡을 반복해서 들으면 기록이 여러 줄 남는다.
     *   그대로 보여주면 화면이 같은 곡으로 도배되므로, 곡별로 가장 최근 한 줄만 남긴다.
     *   LinkedHashMap은 넣은 순서(=최근 순)를 유지하므로 putIfAbsent로 첫 번째만 담긴다.
     */
    public List<TrackDto> recent(String username) {
        List<PlayHistory> rows =
                repository.findByUsernameOrderByPlayedAtDesc(username, PageRequest.of(0, SCAN_SIZE));

        Map<String, TrackDto> unique = new LinkedHashMap<>();
        for (PlayHistory h : rows) {
            unique.putIfAbsent(h.getTrackId(), toTrack(h));
            if (unique.size() >= RECENT_SIZE) break;
        }
        return List.copyOf(unique.values());
    }

    /** 많이 들은 곡 (재생 횟수 순) */
    public List<TopTrackDto> top(String username) {
        return repository.findTopTracks(username, PageRequest.of(0, TOP_SIZE));
    }

    /** 기록 전체 삭제 */
    @Transactional
    public void clear(String username) {
        repository.deleteByUsername(username);
    }

    private static TrackDto toTrack(PlayHistory h) {
        return new TrackDto(
                h.getTrackId(), h.getTitle(), h.getArtist(),
                h.getAlbum(), h.getAlbumImage(), h.getPreviewUrl());
    }
}
