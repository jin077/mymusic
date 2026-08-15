package com.example.practice.playlist;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistFolderRepository extends JpaRepository<PlaylistFolder, Long> {

    List<PlaylistFolder> findByUsernameOrderByIdAsc(String username);

    /**
     * id로만 찾지 않고 username까지 함께 거는 것이 중요하다.
     * id만으로 찾으면 남의 폴더 번호를 넣어 이름을 바꾸거나 지울 수 있다.
     */
    Optional<PlaylistFolder> findByIdAndUsername(Long id, String username);

    void deleteByUsername(String username);
}
