package com.example.practice.music;

/**
 * 프론트에 돌려줄 "곡" 응답 형식 (DTO = Data Transfer Object).
 *
 * ⭐ 왜 외부 API 응답을 그대로 넘기지 않고 이 그릇에 옮겨 담는가?
 *   1) iTunes 응답에는 필드가 30개가 넘는다. 화면에 필요한 6개만 골라 보낸다.
 *      → 응답 크기가 줄고, 프론트가 외부 API 형식에 묶이지 않는다.
 *   2) 나중에 외부 API가 바뀌어도(오늘 Spotify처럼) 이 형식만 유지하면
 *      프론트 코드는 한 줄도 고칠 필요가 없다.
 *   3) 외부에서 온 불필요한 정보(가격, 국가코드, 장르ID 등)를 우리 응답에서 차단한다.
 *
 * record : 값만 담는 클래스를 짧게 만드는 자바 문법(Java 16+).
 *          getter·생성자·equals·toString이 자동으로 만들어진다. (Lombok 없이도 됨)
 */
public record TrackDto(
        String id,          // 곡 고유번호
        String title,       // 곡 제목
        String artist,      // 가수
        String album,       // 앨범명
        String albumImage,  // 앨범 이미지 주소
        String previewUrl   // 30초 미리듣기 음원 주소 (없으면 null)
) {
}
