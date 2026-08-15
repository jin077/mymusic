package com.example.practice.music;

/**
 * 프론트에 돌려줄 "앨범" 응답 형식.
 *
 * ⭐ rank(순위)를 필드로 따로 담는 이유
 *   예전에는 순위가 "배열의 순서"에만 있었다. 그러면 화면에서 index+1로 계산해야 하고,
 *   목록을 다시 정렬하거나 일부만 걸러내는 순간 순위가 어긋난다.
 *   (실제로 '최신 앨범'을 만들려고 발매일순으로 정렬하니 이 문제가 드러났다)
 *   → 순위를 값으로 들고 다니면 어떻게 정렬해도 원래 인기 순위를 잃지 않는다.
 */
public record AlbumDto(
        String id,          // 앨범 고유번호 (Apple)
        int rank,           // 인기 순위 (1위부터)
        String album,       // 앨범명
        String artist,      // 가수
        String albumImage,  // 앨범 이미지 주소
        String releaseDate  // 발매일 (yyyy-MM-dd)
) {
}
