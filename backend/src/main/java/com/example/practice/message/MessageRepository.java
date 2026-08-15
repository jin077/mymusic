package com.example.practice.message;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {

    /** 내가 받은 쪽지 (최신순) */
    List<Message> findByReceiverOrderByIdDesc(String receiver);

    /** 안 읽은 개수 — 목록을 다 읽지 않고 세기만 한다 */
    long countByReceiverAndIsReadFalse(String receiver);

    /** 탈퇴 시 정리 (받은 것·보낸 것 모두) */
    void deleteByReceiver(String receiver);
    void deleteBySender(String sender);
}
