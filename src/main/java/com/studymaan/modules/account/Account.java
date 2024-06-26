package com.studymaan.modules.account;

import com.studymaan.modules.study.Study;
import com.studymaan.modules.tag.Tag;
import com.studymaan.modules.zone.Zone;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter @Setter
@EqualsAndHashCode(of = "id") // 연관 관계가 복잡해질 시 서로 다른 연관 관계가 무한로프 방지를 방지하기 위해 보통 id 값만 주로 사용
@Builder // 빌더 패턴 생성
@AllArgsConstructor // 모든 필드 값을 파라미터로 받는 생성자 생성
@NoArgsConstructor // 파라미터가 없는 기본 생성자 생성
public class Account {

    @Id @GeneratedValue
    private Long id;

    private String email;

    private String nickname;

    private String password;

    private boolean emailVerified;

    private String emailCheckToken;

    private LocalDateTime emailCheckTokenGeneratedAt;

    private LocalDateTime joinedAt;

    private String bio;

    private String url;

    private String occupation;

    private String location;

    @Lob @Basic(fetch = FetchType.EAGER)
    // Lob : varchar(255)가 넘어가는 경우 선언
    private String profileImage;

    private boolean studyCreatedByEmail; // 스터디 생성 알람 이메일로 받을 것인지 flag

    private boolean studyCreatedByWeb = true; // 스터디 생성 알람 웹으로 받을 것인지 flag

    private boolean studyEnrollmentResultByEmail; // 스터디 가입 신청 알람 이메일로 받을 것인지 flag

    private boolean studyEnrollmentResultByWeb = true; // 스터디 가입 신청 알람 웹으로 받을 것인지 flag

    private boolean studyUpdatedByEmail; // 스터디 갱신 정보 알람 이메일로 받을 것인지 flag

    private boolean studyUpdatedByWeb = true; // 스터디 갱신 정보 알람 웹으로 받을 것인지 flag

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    private Set<Zone> zones = new HashSet<>();

    public void generateEmailCheckToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
        this.emailCheckTokenGeneratedAt = LocalDateTime.now();
    }

    public void completeSignUp() {
        this.emailVerified = true;
        this.joinedAt = LocalDateTime.now();
    }

    public boolean isValidToken(String token) {
        return this.emailCheckToken.equals(token);
    }

    public boolean canSendConfirmEmail() {
        // 현 시간 부로 힌시간 전 time return
        return this.emailCheckTokenGeneratedAt.isBefore(LocalDateTime.now().minusHours(1));
    }
}
