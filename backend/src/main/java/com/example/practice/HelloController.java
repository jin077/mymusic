package com.example.practice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 서버가 잘 뜨는지 확인용 테스트 컨트롤러.
 * 브라우저에서 http://localhost:8080/hello 접속 시 문구가 보이면 성공.
 */
@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello Spring Boot + MariaDB! 🎉";
    }
}
