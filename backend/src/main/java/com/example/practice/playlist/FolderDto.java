package com.example.practice.playlist;

/** 폴더 응답 형식. count는 그 폴더에 담긴 곡 수. */
public record FolderDto(
        Long id,
        String name,
        long count
) {
}
