package com.example.practice.playlist;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.practice.music.TrackDto;

/**
 * 즐겨찾기(내 플레이리스트) API. 로그인한 사람 본인의 목록만 다룬다.
 *
 * ⭐ 주소에 사용자를 넣지 않은 이유
 *   /playlist/{username} 처럼 받으면 남의 아이디를 넣어 조회·삭제를 시도할 수 있다.
 *   대상을 요청에서 받지 않고 토큰(Authentication)에서 꺼내면 그 시도 자체가 성립하지 않는다.
 *   SecurityConfig의 anyRequest().authenticated() 에 걸려 비로그인은 401이다.
 */
@RestController
@RequestMapping("/playlist")
public class PlaylistController {

    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    // ───────── 곡 ─────────

    /**
     * 내 목록  →  GET /api/playlist
     *   전체     : /api/playlist
     *   미분류   : /api/playlist?folderId=0
     *   특정폴더 : /api/playlist?folderId=3
     */
    @GetMapping
    public List<PlaylistTrackDto> myPlaylist(Authentication authentication,
                                             @RequestParam(required = false) Long folderId) {
        return playlistService.findMine(authentication.getName(), folderId);
    }

    /** 담기  →  POST /api/playlist   본문은 곡 정보(TrackDto) 그대로 */
    @PostMapping
    public ResponseEntity<PlaylistTrackDto> add(@RequestBody TrackDto track,
                                                Authentication authentication) {
        PlaylistTrackDto saved = playlistService.add(authentication.getName(), track);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /** 빼기  →  DELETE /api/playlist/1445949267  (곡 번호) */
    @DeleteMapping("/{trackId}")
    public ResponseEntity<Void> remove(@PathVariable String trackId,
                                       Authentication authentication) {
        playlistService.remove(authentication.getName(), trackId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 곡을 폴더로 옮기기  →  PUT /api/playlist/1445949267/folder
     * 본문 {"folderId": 3} · 미분류로 되돌리려면 {"folderId": null}
     */
    @PutMapping("/{trackId}/folder")
    public ResponseEntity<Void> move(@PathVariable String trackId,
                                     @RequestBody Map<String, Long> body,
                                     Authentication authentication) {
        playlistService.moveToFolder(authentication.getName(), trackId, body.get("folderId"));
        return ResponseEntity.noContent().build();
    }

    // ───────── 폴더 ─────────

    /** 내 폴더 목록 (곡 수 포함)  →  GET /api/playlist/folders */
    @GetMapping("/folders")
    public List<FolderDto> folders(Authentication authentication) {
        return playlistService.folders(authentication.getName());
    }

    /** 폴더 만들기  →  POST /api/playlist/folders  본문 {"name":"운동할 때"} */
    @PostMapping("/folders")
    public ResponseEntity<FolderDto> createFolder(@RequestBody Map<String, String> body,
                                                  Authentication authentication) {
        FolderDto created = playlistService.createFolder(authentication.getName(), body.get("name"));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** 폴더 이름 바꾸기  →  PUT /api/playlist/folders/3 */
    @PutMapping("/folders/{id}")
    public FolderDto renameFolder(@PathVariable Long id,
                                  @RequestBody Map<String, String> body,
                                  Authentication authentication) {
        return playlistService.renameFolder(authentication.getName(), id, body.get("name"));
    }

    /** 폴더 삭제 (안의 곡은 미분류로)  →  DELETE /api/playlist/folders/3 */
    @DeleteMapping("/folders/{id}")
    public ResponseEntity<Void> deleteFolder(@PathVariable Long id,
                                             Authentication authentication) {
        playlistService.deleteFolder(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
