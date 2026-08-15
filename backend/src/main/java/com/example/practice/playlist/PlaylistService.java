package com.example.practice.playlist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.practice.common.DuplicateException;
import com.example.practice.common.NotFoundException;
import com.example.practice.music.TrackDto;

/** 내 플레이리스트(즐겨찾기) 로직. 모든 메서드가 username을 받아 "내 것만" 다룬다. */
@Service
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistFolderRepository folderRepository;

    public PlaylistService(PlaylistRepository playlistRepository,
                           PlaylistFolderRepository folderRepository) {
        this.playlistRepository = playlistRepository;
        this.folderRepository = folderRepository;
    }

    // ───────── 곡 ─────────

    /**
     * 내가 담은 곡 목록.
     *
     * @param folderId null이면 전체, 0이면 미분류, 그 외에는 해당 폴더
     *   (0을 '미분류'로 정한 이유 : 주소 파라미터로 null을 표현하기 번거로워서다.
     *    폴더 id는 1부터 시작하므로 0과 겹치지 않는다)
     */
    public List<PlaylistTrackDto> findMine(String username, Long folderId) {
        List<PlaylistItem> items;
        if (folderId == null) {
            items = playlistRepository.findByUsernameOrderByIdAsc(username);
        } else if (folderId == 0L) {
            items = playlistRepository.findByUsernameAndFolderIdIsNullOrderByIdAsc(username);
        } else {
            items = playlistRepository.findByUsernameAndFolderIdOrderByIdAsc(username, folderId);
        }
        return items.stream().map(PlaylistTrackDto::from).toList();
    }

    /** 담기 — 이미 담은 곡이면 409 */
    @Transactional
    public PlaylistTrackDto add(String username, TrackDto track) {
        if (playlistRepository.existsByUsernameAndTrackId(username, track.id())) {
            throw new DuplicateException("이미 담은 곡입니다: " + track.title());
        }

        PlaylistItem item = new PlaylistItem();
        item.setUsername(username);
        item.setTrackId(track.id());
        item.setTitle(track.title());
        item.setArtist(track.artist());
        item.setAlbum(track.album());
        item.setAlbumImage(track.albumImage());
        item.setPreviewUrl(track.previewUrl());

        return PlaylistTrackDto.from(playlistRepository.save(item));
    }

    /**
     * 빼기 — 없으면 404
     *
     * 지울 대상을 "행 번호(id)"가 아니라 "내 아이디 + 곡번호"로 찾는 것이 핵심이다.
     * 행 번호로 찾으면 남의 번호를 넣어 남의 목록을 지울 수 있다.
     */
    @Transactional
    public void remove(String username, String trackId) {
        PlaylistItem item = playlistRepository.findByUsernameAndTrackId(username, trackId)
                .orElseThrow(() -> new NotFoundException("담긴 곡이 아닙니다: " + trackId));
        playlistRepository.delete(item);
    }

    /** 곡을 폴더로 옮기기. folderId가 null이면 미분류로 되돌린다. */
    @Transactional
    public void moveToFolder(String username, String trackId, Long folderId) {
        PlaylistItem item = playlistRepository.findByUsernameAndTrackId(username, trackId)
                .orElseThrow(() -> new NotFoundException("담긴 곡이 아닙니다: " + trackId));

        // 내 폴더가 맞는지 확인한다. 남의 폴더 번호를 넣어 옮기지 못하게.
        if (folderId != null) {
            folderRepository.findByIdAndUsername(folderId, username)
                    .orElseThrow(() -> new NotFoundException("폴더를 찾을 수 없습니다: id=" + folderId));
        }
        item.setFolderId(folderId);
    }

    // ───────── 폴더 ─────────

    /** 내 폴더 목록 (각 폴더의 곡 수 포함) */
    public List<FolderDto> folders(String username) {
        // 폴더별 곡 수를 한 번의 쿼리로 세어 Map으로 만든다 (폴더마다 세면 N+1)
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : playlistRepository.countByFolder(username)) {
            counts.put((Long) row[0], (Long) row[1]);
        }

        List<FolderDto> result = new ArrayList<>();
        for (PlaylistFolder f : folderRepository.findByUsernameOrderByIdAsc(username)) {
            result.add(new FolderDto(f.getId(), f.getName(), counts.getOrDefault(f.getId(), 0L)));
        }
        return result;
    }

    @Transactional
    public FolderDto createFolder(String username, String name) {
        String trimmed = requireName(name);

        PlaylistFolder folder = new PlaylistFolder();
        folder.setUsername(username);
        folder.setName(trimmed);
        folderRepository.save(folder);
        return new FolderDto(folder.getId(), folder.getName(), 0);
    }

    @Transactional
    public FolderDto renameFolder(String username, Long id, String name) {
        PlaylistFolder folder = folderRepository.findByIdAndUsername(id, username)
                .orElseThrow(() -> new NotFoundException("폴더를 찾을 수 없습니다: id=" + id));
        folder.setName(requireName(name));
        return new FolderDto(folder.getId(), folder.getName(), 0);
    }

    /**
     * 폴더 삭제.
     *
     * ⭐ 폴더를 지워도 곡은 지우지 않는다.
     *   사용자는 "분류만 없앤 것"이라고 생각하는데 곡까지 사라지면 당황한다.
     *   → 안에 있던 곡은 미분류로 되돌린다.
     */
    @Transactional
    public void deleteFolder(String username, Long id) {
        PlaylistFolder folder = folderRepository.findByIdAndUsername(id, username)
                .orElseThrow(() -> new NotFoundException("폴더를 찾을 수 없습니다: id=" + id));

        playlistRepository.clearFolder(username, id);
        folderRepository.delete(folder);
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("폴더 이름을 입력해 주세요.");
        }
        return name.trim();
    }
}
