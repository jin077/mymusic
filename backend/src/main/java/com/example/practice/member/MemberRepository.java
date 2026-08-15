package com.example.practice.member;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 회원 리포지토리.
 * findByUsername : 아이디로 회원을 찾는 쿼리 메서드.
 *   → 메서드 이름만 규칙대로 지으면 "WHERE username = ?" SQL이 자동 생성됨.
 *   → 로그인할 때 "이 아이디 가진 회원 있어?" 를 찾는 데 씀.
 * Optional : 결과가 없을 수도 있음을 표현하는 안전한 상자 (null 대신).
 */
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username);

    /** 아이디 중복 확인용. 존재 여부만 필요하므로 엔티티를 통째로 읽지 않는다(COUNT 쿼리). */
    boolean existsByUsername(String username);

    /** 가입일이 비어 있는 회원 수 (컬럼을 나중에 추가해서 예전 행은 NULL이다) */
    long countByCreatedAtIsNull();

    /**
     * 비어 있는 가입일을 한 번에 채운다.
     *
     * ⭐ 엔티티를 불러와 setCreatedAt()으로 고치는 방법은 통하지 않는다.
     *   Member.createdAt 에 @Column(updatable = false) 를 걸어두었기 때문에
     *   Hibernate가 UPDATE 문을 만들 때 그 컬럼을 아예 제외한다.
     *   (가입일이 실수로 바뀌지 않게 하려고 건 잠금이 여기서는 걸림돌이 된 것)
     *   → JPQL 벌크 업데이트는 우리가 적은 대로 UPDATE 문을 만들므로 이 제약을 지나간다.
     *     한 줄씩 불러오지 않아 빠르기도 하다.
     */
    @Modifying(clearAutomatically = true)
    @Query("update Member m set m.createdAt = :now where m.createdAt is null")
    int fillEmptyCreatedAt(@Param("now") LocalDateTime now);
}
