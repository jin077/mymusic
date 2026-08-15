package com.example.practice.music;

/**
 * 주간 차트 한 곡.
 *
 * TrackDto와 필드가 같고, 평균 순위와 며칠 동안 차트에 있었는지가 더 붙는다.
 * → 화면의 곡 목록 컴포넌트를 그대로 쓰면서 부가 정보만 덧붙일 수 있다.
 */
public record WeeklyTrackDto(
        String id,
        String title,
        String artist,
        String album,
        String albumImage,
        String previewUrl,
        double avgRank,     // 기간 평균 순위 (낮을수록 상위)
        long days           // 기간 중 차트에 오른 날 수
) {
}
