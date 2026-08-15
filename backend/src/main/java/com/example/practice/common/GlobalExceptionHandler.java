package com.example.practice.common;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 모든 컨트롤러에서 발생한 예외를 한 곳에서 상태코드로 변환한다.
 *
 * ⭐ 왜 만들었나
 *   예전에는 없는 회원을 삭제하거나 중복 아이디로 가입하면 예외가 그대로 밖으로 나가
 *   전부 500(Internal Server Error)이 됐다.
 *   500은 "서버가 잘못했다"는 뜻이라, 클라이언트 입장에서는
 *   "내가 잘못 보낸 건지 서버가 고장난 건지" 구분할 수 없다.
 *
 * ⭐ 왜 컨트롤러마다 try-catch 하지 않는가
 *   컨트롤러 10개에 같은 try-catch를 반복하게 되고, 새 컨트롤러에서 빠뜨리기 쉽다.
 *   @RestControllerAdvice는 "모든 컨트롤러를 감싸는 그물" 역할을 해서
 *   규칙을 한 곳에서 관리한다. (프론트의 axios 인터셉터와 같은 생각)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 없는 자원 → 404 */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", e.getMessage()));
    }

    /** 중복 → 409 */
    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<Map<String, String>> handleDuplicate(DuplicateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", e.getMessage()));
    }

    /** 자격 없음(남의 것을 건드리려 할 때) → 403 */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(ForbiddenException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", e.getMessage()));
    }

    /** 잔액 부족 → 402 */
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<Map<String, String>> handleNoBalance(InsufficientBalanceException e) {
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body(Map.of("message", e.getMessage()));
    }

    /** 잘못된 입력값(빈 파일, 허용하지 않는 형식 등) → 400 */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
    }

    /** 업로드 용량 초과 → 413 (설정: spring.servlet.multipart.max-file-size) */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleTooLarge(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE)
                .body(Map.of("message", "파일이 너무 큽니다. 2MB 이하만 올릴 수 있습니다."));
    }
}
