package com.example.practice.playlist;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * 내가 담은 곡 한 줄.
 *
 * ⭐ 곡 정보를 통째로 저장하는 이유 (id만 저장하지 않는 이유)
 *   곡 데이터는 우리 DB가 아니라 Apple에 있다. trackId만 저장하면
 *   목록을 보여줄 때마다 담은 곡 수만큼 외부 API를 다시 불러야 하고,
 *   Apple에서 곡이 내려가면 아예 표시할 수 없게 된다.
 *   → 담는 시점의 정보를 복사해 둔다. (외부 의존을 끊는 선택)
 *
 * ⭐ uniqueConstraints
 *   같은 사람이 같은 곡을 두 번 담지 못하게 DB 차원에서 막는다.
 *   코드에서 먼저 검사하지만, 동시에 두 번 눌리는 경우까지 막으려면 제약이 필요하다.
 */
@Entity
@Table(name = "playlist_items",
       uniqueConstraints = @UniqueConstraint(columnNames = {"username", "trackId"}))
@Getter
@Setter
public class PlaylistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;   // 담은 사람 (토큰에서 꺼낸 아이디)

    @Column(nullable = false)
    private String trackId;    // 곡 고유번호 (iTunes)

    private String title;
    private String artist;
    private String album;

    @Column(length = 500)
    private String albumImage;

    @Column(length = 500)
    private String previewUrl;

    /**
     * 어느 폴더에 들어 있는지. null이면 '미분류'.
     *
     * 폴더를 지워도 곡은 남아야 하므로(미분류로 이동) null을 허용한다.
     */
    private Long folderId;
}
