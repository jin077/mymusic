package com.example.practice.common;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 업로드된 파일을 서버에 저장·삭제한다.
 *
 * ⭐ 파일을 DB가 아니라 파일 시스템에 두는 이유
 *   이미지를 DB에 통째로 넣으면(BLOB) 백업이 무거워지고 조회할 때마다 DB를 거친다.
 *   파일은 파일 시스템에, DB에는 "파일 이름"만 두는 것이 일반적이다.
 *
 * ⭐ 저장 폴더를 코드에 박지 않고 설정값(app.upload.dir)으로 받는 이유
 *   내 PC에서는 ./uploads, 컨테이너 안에서는 /app/uploads 로 달라진다.
 *   경로가 코드에 박혀 있으면 환경이 바뀔 때마다 코드를 고쳐야 한다.
 */
@Service
public class FileStorageService {

    /** 허용할 확장자. 목록에 없는 것은 아예 받지 않는다(화이트리스트 방식). */
    private static final Set<String> ALLOWED = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final Path root;

    public FileStorageService(@Value("${app.upload.dir}") String uploadDir) {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);   // 폴더가 없으면 만들어 둔다
        } catch (IOException e) {
            throw new UncheckedIOException("업로드 폴더를 만들 수 없습니다: " + root, e);
        }
    }

    /**
     * 저장하고 "새로 붙인 파일 이름"을 돌려준다.
     *
     * ⭐ 원본 파일명을 그대로 쓰지 않는 이유 (두 가지 문제)
     *   1) 충돌 — 서로 다른 사용자가 둘 다 profile.png를 올리면 하나가 덮어써진다
     *   2) 보안 — 파일명에 "../../"가 들어오면 엉뚱한 폴더에 쓰일 수 있다(경로 조작)
     *   → 확장자만 가져오고 이름은 UUID로 새로 만든다.
     */
    public String save(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어 있습니다.");
        }
        // 내용 종류도 확인한다. 확장자는 얼마든지 바꿔 붙일 수 있기 때문.
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 올릴 수 있습니다.");
        }

        String ext = extensionOf(file.getOriginalFilename());
        if (!ALLOWED.contains(ext)) {
            throw new IllegalArgumentException("허용하지 않는 형식입니다: " + ext);
        }

        String saved = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        try {
            Files.copy(file.getInputStream(), root.resolve(saved),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("파일 저장에 실패했습니다.", e);
        }
        return saved;
    }

    /** 이전 파일 정리. 없으면 조용히 넘어간다(이미 지워졌어도 오류로 볼 일은 아니다). */
    public void delete(String filename) {
        if (filename == null || filename.isBlank()) return;
        try {
            Files.deleteIfExists(root.resolve(filename));
        } catch (IOException ignored) {
            // 삭제 실패가 요청 전체를 실패시킬 이유는 없다
        }
    }

    private static String extensionOf(String originalName) {
        if (originalName == null) return "";
        int dot = originalName.lastIndexOf('.');
        if (dot < 0 || dot == originalName.length() - 1) return "";
        return originalName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
