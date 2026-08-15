package com.example.practice.auth;

import com.example.practice.member.Member;
import com.example.practice.member.MemberRepository;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 스프링 시큐리티와 우리 DB(members 테이블)를 이어주는 다리.
 *
 * 사용자가 로그인 시도하면 → 시큐리티가 이 클래스의 loadUserByUsername()를 자동 호출함
 * → 우리는 그 아이디로 DB에서 회원을 찾아 → 시큐리티가 아는 형태(UserDetails)로 변환해서 돌려줌
 * → 그럼 시큐리티가 입력한 비번과 DB의 (암호화된) 비번을 대조해서 로그인 성공/실패를 판단.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    public CustomUserDetailsService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없음: " + username));

        // 우리 Member → 시큐리티가 이해하는 User(UserDetails)로 변환
        return User.builder()
                .username(member.getUsername())
                .password(member.getPassword()) // 암호화된 비번 그대로 전달 (시큐리티가 대조함)
                .roles(member.getRole())        // "USER" → 내부적으로 "ROLE_USER" 로 취급
                .build();
    }
}
