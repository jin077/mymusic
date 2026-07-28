package com.example.practice;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 개인정보 CRUD API.
 * CRUD = Create(생성)/Read(조회)/Update(수정)/Delete(삭제).
 *
 * @RequestMapping("/info") : 이 컨트롤러의 모든 주소는 /info 로 시작함.
 */
@RestController
@RequestMapping("/info")
public class InfoController {

    // 리포지토리를 생성자로 주입받음 (스프링이 자동으로 넣어줌)
    private final InfoRepository infoRepository;

    public InfoController(InfoRepository infoRepository) {
        this.infoRepository = infoRepository;
    }

    // [Read] 전체 조회  →  GET  http://localhost:8080/info
    @GetMapping
    public List<Info> findAll() {
        return infoRepository.findAll();
    }

    // [Read] 단건 조회  →  GET  http://localhost:8080/info/1
    @GetMapping("/{id}")
    public Info findById(@PathVariable Long id) {
        return infoRepository.findById(id).orElse(null);
    }

    // [Create] 생성  →  POST http://localhost:8080/info  (JSON 본문 전송)
    @PostMapping
    public Info create(@RequestBody Info info) {
        return infoRepository.save(info);  // id 없으면 INSERT
    }

    // [Update] 수정  →  PUT  http://localhost:8080/info/1  (JSON 본문 전송)
    @PutMapping("/{id}")
    public Info update(@PathVariable Long id, @RequestBody Info info) {
        info.setId(id);                    // id를 지정하면 save가 UPDATE로 동작
        return infoRepository.save(info);
    }

    // [Delete] 삭제  →  DELETE http://localhost:8080/info/1
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        infoRepository.deleteById(id);
        return "삭제 완료: id=" + id;
    }
}
