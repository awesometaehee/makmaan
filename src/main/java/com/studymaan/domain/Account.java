package com.studymaan.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Account {

    @Id @GeneratedValue
    private Long id;

    private String email;

    private String nickname;

    private String password;

    private boolean emailVerified;

    private String emailCheckToken;

    private LocalDateTime joinedAt;

    private String bio;

    private String url;

    private String occupation;

    private String location;

    @Lob @Basic(fetch = FetchType.EAGER)
    // Lob : varchar(255)가 넘어가는 경우 선언
    private String profileImage;

    private boolean studyCreatedByEmail; // 스터디 생성 알람 이메일로 받을 것인지 flag

    private boolean studyCreatedByWeb; // 스터디 생성 알람 웹으로 받을 것인지 flag

    private boolean studyEnrollmentResultByEmail; // 스터디 가입 신청 알람 이메일로 받을 것인지 flag

    private boolean studyEnrollmentResultByWeb; // 스터디 가입 신청 알람 웹으로 받을 것인지 flag

    private boolean studyUpdatedByEmail; // 스터디 갱신 정보 알람 이메일로 받을 것인지 flag

    private boolean studyUpdatedByWeb; // 스터디 갱신 정보 알람 웹으로 받을 것인지 flag

    public void generateEmailCheckToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
    }

    public void completeSignUp() {
        this.emailVerified = true;
        this.joinedAt = LocalDateTime.now();
    }

    public boolean isValidToken(String token) {
        return this.emailCheckToken.equals(token);
    }
}
