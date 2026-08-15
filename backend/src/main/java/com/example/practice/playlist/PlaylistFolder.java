package com.example.practice.playlist;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 즐겨찾기 폴더. 담아둔 곡을 분류하는 상자다.
 *
 * ⭐ 폴더 이름에 유니크 제약을 걸지 않은 이유
 *   "내 폴더 안에서만" 유일하면 되는데, 다른 사람도 같은 이름을 쓸 수 있어야 한다.
 *   (username, name) 조합으로 걸 수도 있지만, 같은 이름 폴더를 두 개 만드는 것이
 *   특별히 문제되는 상황이 아니라 제약 없이 두었다.
 */
@Entity
@Table(name = "playlist_folders")
@Getter
@Setter
public class PlaylistFolder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;   // 폴더 주인 (토큰에서 꺼낸 아이디)

    @Column(nullable = false)
    private String name;
}
