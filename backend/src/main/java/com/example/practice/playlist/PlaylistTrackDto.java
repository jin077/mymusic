package com.example.practice.playlist;

/**
 * 즐겨찾기 곡 응답 형식.
 *
 * TrackDto와 필드가 같고 folderId만 더 있다.
 *   → 화면이 "이 곡이 지금 어느 폴더에 있는지"를 알아야 폴더 선택 칸에 표시할 수 있다.
 * 곡 정보 자체는 TrackDto와 모양이 같아서 기존 목록 컴포넌트를 그대로 쓸 수 있다.
 */
public record PlaylistTrackDto(
        String id,
        String title,
        String artist,
        String album,
        String albumImage,
        String previewUrl,
        Long folderId       // null이면 미분류
) {
    public static PlaylistTrackDto from(PlaylistItem item) {
        return new PlaylistTrackDto(
                item.getTrackId(),
                item.getTitle(),
                item.getArtist(),
                item.getAlbum(),
                item.getAlbumImage(),
                item.getPreviewUrl(),
                item.getFolderId());
    }
}
