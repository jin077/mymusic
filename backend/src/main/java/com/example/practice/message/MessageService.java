package com.example.practice.message;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.practice.common.ForbiddenException;
import com.example.practice.common.NotFoundException;
import com.example.practice.member.MemberRepository;

/** 쪽지 로직. */
@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final MemberRepository memberRepository;

    public MessageService(MessageRepository messageRepository, MemberRepository memberRepository) {
        this.messageRepository = messageRepository;
        this.memberRepository = memberRepository;
    }

    /** 내가 받은 쪽지 */
    public List<MessageDto> inbox(String username) {
        return messageRepository.findByReceiverOrderByIdDesc(username).stream()
                .map(MessageDto::from)
                .toList();
    }

    /** 안 읽은 개수 (홈 화면의 "쪽지 N"에 쓴다) */
    public long unreadCount(String username) {
        return messageRepository.countByReceiverAndIsReadFalse(username);
    }

    /** 보내기 — 받는 사람이 실제로 있어야 한다 */
    @Transactional
    public MessageDto send(String sender, MessageRequest request) {
        if (request.receiver() == null || !memberRepository.existsByUsername(request.receiver())) {
            throw new NotFoundException("받는 사람을 찾을 수 없습니다: " + request.receiver());
        }
        if (sender.equals(request.receiver())) {
            throw new IllegalArgumentException("자기 자신에게는 보낼 수 없습니다.");
        }
        if (request.title() == null || request.title().isBlank()
                || request.content() == null || request.content().isBlank()) {
            throw new IllegalArgumentException("제목과 내용을 입력해 주세요.");
        }

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(request.receiver());
        message.setTitle(request.title().trim());
        message.setContent(request.content().trim());
        return MessageDto.from(messageRepository.save(message));
    }

    /**
     * 읽음 처리 — 받은 사람만 할 수 있다.
     *
     * id만으로 찾아 바꾸면 남의 쪽지를 읽음으로 만들 수 있으므로 받는 사람을 확인한다.
     */
    @Transactional
    public void markRead(Long id, String username) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("쪽지를 찾을 수 없습니다: id=" + id));
        if (!message.getReceiver().equals(username)) {
            throw new ForbiddenException("내가 받은 쪽지가 아닙니다.");
        }
        message.setRead(true);
    }

    /** 삭제 — 받은 사람만 */
    @Transactional
    public void delete(Long id, String username) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("쪽지를 찾을 수 없습니다: id=" + id));
        if (!message.getReceiver().equals(username)) {
            throw new ForbiddenException("내가 받은 쪽지가 아닙니다.");
        }
        messageRepository.delete(message);
    }
}
