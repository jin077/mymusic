package com.example.practice;

/**
 * 프론트에 돌려줄 "앨범" 응답 형식.
 */
public record AlbumDto(
        String album,       // 앨범명
        String artist,      // 가수
        String albumImage   // 앨범 이미지 주소
) {
}
