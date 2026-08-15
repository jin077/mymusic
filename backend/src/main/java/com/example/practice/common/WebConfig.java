package com.example.practice.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * 웹(MVC) 공통 설정.
 *
 * 하는 일 두 가지
 *   1) 모든 REST API 주소 앞에 "/api" 를 자동으로 붙인다.
 *   2) 업로드된 파일이 담긴 폴더를 "/uploads/**" 주소로 공개한다.
 *
 * 왜 /api 접두사인가?
 *   배포 시 nginx가 "이 요청은 화면(정적파일)인가, API인가"를 구분해야 하는데,
 *   API를 전부 /api 로 시작하게 묶어두면 nginx는 규칙 딱 한 줄로 처리할 수 있다.
 *   컨트롤러를 하나하나 안 고쳐도 되게, @RestController가 붙은 클래스 전체에 일괄 적용한다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String uploadDir;

    public WebConfig(@Value("${app.upload.dir}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/api",
                HandlerTypePredicate.forAnnotation(RestController.class));
    }

    /**
     * 업로드 폴더를 웹에서 볼 수 있게 연결한다.
     *   파일:  {app.upload.dir}/3f9c...png
     *   주소:  /uploads/3f9c...png
     *
     * ⭐ 이 경로에는 /api 접두사가 붙지 않는다.
     *   위 configurePathMatch는 @RestController에만 적용되기 때문이다.
     *   정적 파일은 컨트롤러를 거치지 않고 바로 내려간다(빠르다).
     *
     * ⭐ SecurityConfig에서 /uploads/** 를 permitAll 로 열어야 한다.
     *   브라우저가 <img src="/uploads/...">로 불러올 때는 Authorization 헤더를 보내지 않는다.
     *   막아두면 로그인한 사용자에게도 사진이 깨져 보인다.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
