package com.example.practice.message;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 쪽지 API.
 *
 * 권한
 *   GET  /api/messages          내가 받은 쪽지 (로그인)
 *   GET  /api/messages/unread   안 읽은 개수 (로그인)
 *   PUT  /api/messages/3/read   읽음 처리 (받은 사람만)
 *   DELETE /api/messages/3      삭제 (받은 사람만)
 *   POST /api/messages          보내기 (로그인한 회원 누구나, 자기 자신 제외)
 */
@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public List<MessageDto> inbox(Authentication authentication) {
        return messageService.inbox(authentication.getName());
    }

    @GetMapping("/unread")
    public Map<String, Long> unread(Authentication authentication) {
        return Map.of("count", messageService.unreadCount(authentication.getName()));
    }

    /** 보내기 */
    @PostMapping
    public ResponseEntity<MessageDto> send(@RequestBody MessageRequest request,
                                           Authentication authentication) {
        MessageDto sent = messageService.send(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(sent);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> read(@PathVariable Long id, Authentication authentication) {
        messageService.markRead(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        messageService.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
