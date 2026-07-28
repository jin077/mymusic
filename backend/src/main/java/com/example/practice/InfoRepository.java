package com.example.practice;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Info 테이블에 접근하는 리포지토리.
 *
 * JpaRepository<Info, Long> 만 상속하면
 *   - Info : 다룰 엔티티
 *   - Long : 그 엔티티의 @Id(기본키) 타입
 * 이것만으로 save(저장), findAll(전체조회), findById(단건조회),
 * deleteById(삭제) 같은 메서드가 "자동으로" 만들어짐. (SQL을 직접 안 짜도 됨!)
 */
public interface InfoRepository extends JpaRepository<Info, Long> {
}
