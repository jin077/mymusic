package com.example.practice.playlist;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 플레이리스트 리포지토리.
 *
 * 모든 조회에 username 조건이 들어간다.
 *   → "내 것만" 다루기 위해서다. id만으로 찾으면 남의 담은 곡을 건드릴 수 있다.
 */
public interface PlaylistRepository extends JpaRepository<PlaylistItem, Long> {

    List<PlaylistItem> findByUsernameOrderByIdAsc(String username);

    /** 특정 폴더의 곡 */
    List<PlaylistItem> findByUsernameAndFolderIdOrderByIdAsc(String username, Long folderId);

    /** 폴더에 안 들어간 곡(미분류) — folderId가 null인 것 */
    List<PlaylistItem> findByUsernameAndFolderIdIsNullOrderByIdAsc(String username);

    boolean existsByUsernameAndTrackId(String username, String trackId);

    Optional<PlaylistItem> findByUsernameAndTrackId(String username, String trackId);

    /** 폴더별 곡 수를 한 번에 센다. [folderId, count] 배열로 돌아온다. */
    @Query("""
            select i.folderId, count(i) from PlaylistItem i
            where i.username = :username and i.folderId is not null
            group by i.folderId
            """)
    List<Object[]> countByFolder(@Param("username") String username);

    /**
     * 폴더를 지울 때, 그 폴더에 있던 곡을 미분류로 되돌린다.
     *
     * @Modifying : 조회가 아니라 변경(UPDATE/DELETE) 쿼리임을 알려준다.
     *   없으면 스프링이 조회로 취급해 실행하지 못한다.
     */
    @Modifying
    @Query("update PlaylistItem i set i.folderId = null where i.username = :username and i.folderId = :folderId")
    void clearFolder(@Param("username") String username, @Param("folderId") Long folderId);

    /** 탈퇴 시 정리용 */
    void deleteByUsername(String username);
}
