package com.example.practice.member;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;

import com.example.practice.common.DuplicateException;
import com.example.practice.common.FileStorageService;
import com.example.practice.common.ForbiddenException;
import com.example.practice.common.NotFoundException;
import com.example.practice.comment.CommentRepository;
import com.example.practice.history.PlayHistoryRepository;
import com.example.practice.playlist.PlaylistFolderRepository;
import com.example.practice.playlist.PlaylistRepository;
import com.example.practice.message.MessageRepository;
import com.example.practice.purchase.PurchaseRepository;
import com.example.practice.ticket.TicketPurchaseRepository;

/**
 * 회원 관련 "일 처리(비즈니스 로직)"를 담당하는 Service 계층.
 *
 * Controller → Service → Repository 3계층 구조에서 가운데.
 * Controller는 요청만 받고, 실제 로직(암호화, 권한 고정 등)은 여기서 처리.
 */
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final PlaylistRepository playlistRepository;
    private final PlaylistFolderRepository folderRepository;
    private final PlayHistoryRepository playHistoryRepository;
    private final CommentRepository commentRepository;
    private final PurchaseRepository purchaseRepository;
    private final TicketPurchaseRepository ticketPurchaseRepository;
    private final MessageRepository messageRepository;

    public MemberService(MemberRepository memberRepository,
                         PasswordEncoder passwordEncoder,
                         FileStorageService fileStorageService,
                         PlaylistRepository playlistRepository,
                         PlaylistFolderRepository folderRepository,
                         PlayHistoryRepository playHistoryRepository,
                         CommentRepository commentRepository,
                         PurchaseRepository purchaseRepository,
                         TicketPurchaseRepository ticketPurchaseRepository,
                         MessageRepository messageRepository) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileStorageService = fileStorageService;
        this.playlistRepository = playlistRepository;
        this.folderRepository = folderRepository;
        this.playHistoryRepository = playHistoryRepository;
        this.commentRepository = commentRepository;
        this.purchaseRepository = purchaseRepository;
        this.ticketPurchaseRepository = ticketPurchaseRepository;
        this.messageRepository = messageRepository;
    }

    /**
     * 일반 회원가입.
     *
     * ⭐ 보안 포인트: role을 무조건 "USER"로 고정함.
     *   → 가입 요청에 role="ADMIN"을 몰래 끼워넣어도 무시됨.
     *   → 관리자는 회원가입으로 못 만들고, 별도 방법으로만 만들어짐.
     *
     * ⭐ 중복 아이디는 409로 응답한다.
     *   미리 검사하지 않으면 DB 유니크 제약에 걸려 예외가 그대로 500으로 나간다.
     *   "이미 쓰는 아이디"는 서버 잘못이 아니라 요청이 현재 상태와 충돌한 것이다.
     */
    @Transactional
    public Member register(SignupRequest request) {
        if (memberRepository.existsByUsername(request.username())) {
            throw new DuplicateException("이미 사용 중인 아이디입니다: " + request.username());
        }

        Member member = new Member();
        member.setUsername(request.username());
        member.setPassword(passwordEncoder.encode(request.password())); // BCrypt 암호화
        member.setRole("USER");                                         // 권한 강제 고정
        member.setNickname(request.nickname());
        member.setEmail(request.email());
        return memberRepository.save(member);
    }

    /** 전체 회원 조회 (관리자 기능) */
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    /** 아이디로 회원 조회 — 토큰에서 꺼낸 아이디로 "내 정보"를 찾을 때 사용 */
    public Member findByUsername(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다: " + username));
    }

    /**
     * 내 정보 수정 (닉네임·이메일만).
     *
     * @Transactional 안에서 조회한 엔티티는 영속 상태라, 값만 바꿔두면
     * 트랜잭션이 끝날 때 변경 감지(dirty checking)로 UPDATE가 자동 실행된다.
     * → save()를 따로 부르지 않아도 된다.
     */
    @Transactional
    public Member updateProfile(String username, MemberUpdateRequest request) {
        Member member = findByUsername(username);
        member.setNickname(request.nickname());
        member.setEmail(request.email());
        return member;
    }

    /**
     * 프로필 사진 변경.
     *
     * 새 파일을 저장한 뒤 이전 파일을 지운다. 순서가 중요하다 —
     * 먼저 지웠다가 저장이 실패하면 사진이 사라진 상태로 남는다.
     */
    @Transactional
    public Member changeProfileImage(String username, MultipartFile file) {
        Member member = findByUsername(username);
        String old = member.getProfileImage();

        member.setProfileImage(fileStorageService.save(file));
        fileStorageService.delete(old);   // 안 쓰는 파일이 계속 쌓이지 않게 정리
        return member;
    }

    /** 프로필 사진 삭제 (기본 이미지로 되돌리기) */
    @Transactional
    public Member removeProfileImage(String username) {
        Member member = findByUsername(username);
        fileStorageService.delete(member.getProfileImage());
        member.setProfileImage(null);
        return member;
    }

    /**
     * 회원 탈퇴 (본인).
     *
     * ⭐ 관리자 삭제(delete(id))와 나누어 둔 이유
     *   삭제 대상을 id로 받으면 남의 번호를 넣어 남을 지울 수 있다.
     *   탈퇴는 대상이 언제나 "나"이므로 토큰에서 꺼낸 아이디로만 찾는다.
     *
     * ⚠️ 실무에서는 바로 지우지 않고 '탈퇴 표시'만 남기는 경우가 많다(soft delete).
     *   주문 내역·작성 글이 함께 사라지면 곤란하기 때문이다.
     *   지금은 회원에 딸린 데이터가 플레이리스트뿐이라 실제 삭제로 둔다.
     */
    @Transactional
    public void withdraw(String username) {
        Member member = findByUsername(username);

        // ⭐ 회원에 딸린 데이터를 함께 지운다.
        //   안 지우면 같은 아이디로 다시 가입했을 때 남의(예전) 기록을 물려받는다.
        //   회원 테이블과 외래키로 묶지 않고 아이디 문자열로 연결해 두었기 때문에
        //   DB가 알아서 지워주지 않는다 → 코드에서 명시적으로 정리한다.
        playlistRepository.deleteByUsername(username);
        folderRepository.deleteByUsername(username);
        playHistoryRepository.deleteByUsername(username);
        commentRepository.deleteByWriter(username);
        purchaseRepository.deleteByUsername(username);
        ticketPurchaseRepository.deleteByUsername(username);
        messageRepository.deleteByReceiver(username);
        messageRepository.deleteBySender(username);
        fileStorageService.delete(member.getProfileImage());  // 올려둔 사진도 함께 정리

        memberRepository.delete(member);
    }

    /**
     * 비밀번호 변경.
     *
     * ⭐ 현재 비밀번호를 대조하는 방법
     *   DB에는 BCrypt 해시만 있어서 "원래 비밀번호가 무엇인지"는 알 수 없다.
     *   대신 입력값을 같은 방식으로 해싱해 저장된 값과 맞는지 matches()로 확인한다.
     *   (BCrypt는 같은 비밀번호라도 매번 다른 해시를 만들기 때문에
     *    encode() 결과끼리 문자열 비교를 하면 안 된다. 반드시 matches()를 쓴다)
     *
     * ⚠️ 비밀번호를 바꿔도 이미 발급된 토큰은 만료 전까지 살아 있다.
     *   토큰을 서버가 보관하지 않는 stateless 방식의 알려진 한계다.
     *   막으려면 토큰 블랙리스트나 리프레시 토큰 구조가 필요하다.
     */
    @Transactional
    public void changePassword(String username, PasswordChangeRequest request) {
        if (request.newPassword() == null || request.newPassword().length() < 4) {
            throw new IllegalArgumentException("새 비밀번호는 4자 이상이어야 합니다.");
        }

        Member member = findByUsername(username);
        if (!passwordEncoder.matches(request.currentPassword(), member.getPassword())) {
            throw new ForbiddenException("현재 비밀번호가 일치하지 않습니다.");
        }
        member.setPassword(passwordEncoder.encode(request.newPassword()));
    }

    /**
     * 캐시 충전.
     *
     * ⚠️ 지금은 실제 결제 없이 잔액만 늘린다(학습용).
     *   실서비스라면 이 자리에 PG(결제대행) 연동이 들어간다:
     *     프론트가 결제창을 띄움 → PG가 결제 결과와 함께 우리 서버를 호출
     *     → 우리가 PG API로 "정말 결제됐는지" 다시 확인 → 그때 잔액을 올린다
     *   결제 결과를 프론트 말만 믿고 처리하면 위조된 요청으로 공짜 충전이 가능하다.
     */
    @Transactional
    public Member charge(String username, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("충전 금액은 1원 이상이어야 합니다.");
        }
        Member member = findByUsername(username);
        member.setBalance(member.getBalance() + amount);
        return member;
    }

    /** 회원 권한 변경 (관리자 기능) */
    @Transactional
    public Member changeRole(Long id, String role) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다: id=" + id));
        member.setRole(role);
        return member;
    }

    /**
     * 회원 삭제 — 관리자 기능.
     *
     * 예전에는 deleteById만 불러서, 없는 id를 지우려 하면 예외가 500으로 나갔다.
     * 존재 여부를 먼저 확인해 404로 답한다.
     */
    @Transactional
    public void delete(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다: id=" + id));

        // 탈퇴와 마찬가지로 딸린 데이터를 함께 정리한다
        playlistRepository.deleteByUsername(member.getUsername());
        folderRepository.deleteByUsername(member.getUsername());
        playHistoryRepository.deleteByUsername(member.getUsername());
        commentRepository.deleteByWriter(member.getUsername());
        purchaseRepository.deleteByUsername(member.getUsername());
        ticketPurchaseRepository.deleteByUsername(member.getUsername());
        messageRepository.deleteByReceiver(member.getUsername());
        messageRepository.deleteBySender(member.getUsername());
        fileStorageService.delete(member.getProfileImage());

        memberRepository.delete(member);
    }
}
