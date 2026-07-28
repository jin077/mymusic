package com.example.practice;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

/**
 * 개인정보 엔티티.
 * @Entity 를 붙이면 JPA가 이 클래스를 DB 테이블 하나로 취급함.
 * 앱을 실행하면 (ddl-auto=update 덕분에) MariaDB에 'info' 테이블이 자동 생성됨.
 * → HeidiSQL에서 practice DB 안에 info 테이블이 생긴 걸 눈으로 확인할 수 있어.
 */
@Entity
@Getter          // Lombok: 모든 필드의 getter 메서드를 자동 생성
@Setter          // Lombok: 모든 필드의 setter 메서드를 자동 생성
public class Info {

    @Id                                                 // 기본키(PK) 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 값을 DB가 자동 증가(auto_increment)로 채움
    private Long id;

    private String name;    // 이름

    private String phone;   // 전화번호

    private String email;   // 이메일
}
