package com.example.practice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 앱의 시작점.
 *
 * @EnableScheduling : @Scheduled가 붙은 메서드를 정해진 시각에 실행하게 켜는 스위치.
 *   이 한 줄이 없으면 ChartScheduler의 cron이 아무 일도 하지 않는다.
 */
@SpringBootApplication
@EnableScheduling
public class PracticeApplication {

	public static void main(String[] args) {
		SpringApplication.run(PracticeApplication.class, args);
	}

}
