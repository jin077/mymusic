package com.example.practice.purchase;

import java.time.format.DateTimeFormatter;

/**
 * 구매내역 응답 형식.
 *
 * 곡 정보 필드는 TrackDto와 모양이 같아서 화면의 곡 목록을 그대로 쓸 수 있고,
 * 가격·구매일시가 더 붙는다.
 */
public record PurchaseDto(
        String id,          // 곡 번호 (화면에서 key로 쓴다)
        String title,
        String artist,
        String album,
        String albumImage,
        String previewUrl,
        long price,
        String purchasedAt
) {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static PurchaseDto from(Purchase p) {
        return new PurchaseDto(
                p.getTrackId(), p.getTitle(), p.getArtist(), p.getAlbum(),
                p.getAlbumImage(), p.getPreviewUrl(),
                p.getPrice(), p.getPurchasedAt().format(FORMAT));
    }
}
