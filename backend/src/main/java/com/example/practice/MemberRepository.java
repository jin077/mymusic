package com.example.practice;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 회원 리포지토리.
 * findByUsername : 아이디로 회원을 찾는 쿼리 메서드.
 *   → 메서드 이름만 규칙대로 지으면 "WHERE username = ?" SQL이 자동 생성됨.
 *   → 로그인할 때 "이 아이디 가진 회원 있어?" 를 찾는 데 씀.
 * Optional : 결과가 없을 수도 있음을 표현하는 안전한 상자 (null 대신).
 */
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username);
}
