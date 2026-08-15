package com.example.practice.history;

/**
 * "많이 들은 곡" 응답 형식.
 *
 * TrackDto와 필드가 같고 playCount(재생 횟수)만 더 있다.
 * → 화면의 곡 목록 컴포넌트를 그대로 쓸 수 있고, 횟수만 추가로 보여주면 된다.
 */
public record TopTrackDto(
        String id,
        String title,
        String artist,
        String album,
        String albumImage,
        String previewUrl,
        long playCount
) {
}
