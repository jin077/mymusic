package com.example.practice;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 웹(MVC) 공통 설정.
 *
 * 여기서 하는 일: 모든 REST API 주소 앞에 "/api" 를 자동으로 붙인다.
 *   예) 컨트롤러에 /login 이라고 적어둬도 → 실제 주소는 /api/login 이 된다.
 *
 * 왜? 배포 시 nginx가 "이 요청은 화면(정적파일)인가, API인가"를 구분해야 하는데,
 *     API를 전부 /api 로 시작하게 묶어두면 nginx는 규칙 딱 한 줄로 처리할 수 있다.
 *     ("/api 로 시작하면 백엔드로, 아니면 화면 파일로")
 *
 * 컨트롤러를 하나하나 안 고쳐도 되게, @RestController가 붙은 클래스 전체에 일괄 적용한다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/api",
                HandlerTypePredicate.forAnnotation(RestController.class));
    }
}
