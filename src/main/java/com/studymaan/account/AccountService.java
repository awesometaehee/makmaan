package com.studymaan.account;

import com.studymaan.config.AppProperties;
import com.studymaan.domain.Tag;
import com.studymaan.domain.Zone;
import com.studymaan.mail.EmailMessage;
import com.studymaan.mail.EmailService;
import com.studymaan.settings.form.NicknameForm;
import com.studymaan.settings.form.Notifications;
import com.studymaan.settings.form.Profile;
import com.studymaan.domain.Account;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;

    private final SecurityContextHolderStrategy securityContextHolderStrategy;
    private final SecurityContextRepository securityContextRepository;

    private final ModelMapper modelMapper;

    public Account processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        // 회원 가입 메일 송신
        sendSignUpConfirmEmail(newAccount);
        return newAccount;
    }

    private Account saveNewAccount(SignUpForm signUpForm) {
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        Account account = modelMapper.map(signUpForm, Account.class);
        account.generateEmailCheckToken();
        return accountRepository.save(account);
    }

    public void sendSignUpConfirmEmail(Account newAccount) {
        Context context = new Context();
        context.setVariable("link", "/check-email-token?token=" + newAccount.getEmailCheckToken() +
                "&email=" + newAccount.getEmail());
        context.setVariable("nickName", newAccount.getNickname());
        context.setVariable("linkName", "이메일 인증하기");
        context.setVariable("message", "스터디올래 서비스를 이용하려면 클릭하세요.");
        context.setVariable("host", appProperties.getHost());

        String message = templateEngine.process("/mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(newAccount.getEmail())
                .subject("스터디올레 회원 가입 인증")
                .message(message)
                .build();
        emailService.sendEmail(emailMessage);
    }

    public void login(Account account, HttpServletRequest request, HttpServletResponse response) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account),
                account.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(token);
        securityContextHolderStrategy.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(emailOrNickname);
        if(account == null) {
            account = accountRepository.findByNickname(emailOrNickname);
        }

        if(account == null) {
            throw new UsernameNotFoundException(emailOrNickname);
        }

        // Principal 객체 리턴
        return new UserAccount(account);
    }

    public void completeSignUp(Account account, HttpServletRequest request, HttpServletResponse response) {
        account.completeSignUp();
        login(account, request, response);
    }

    public void updateProfile(Account account, Profile profile) {
        /* modelMapper 사용 시 아래와 같이 사용 가능
        account.setBio(profile.getBio());
        account.setLocation(profile.getLocation());
        account.setOccupation(profile.getOccupation());
        account.setUrl(profile.getUrl());
        account.setProfileImage(profile.getProfileImage());
        */
        modelMapper.map(profile, account); // 프로필에 있는 데이터를 어카운트로(에) 변환, 프로퍼티들이 매핑이 된다는 가정하에 사용
        accountRepository.save(account);
    }

    public void updatePassword(Account account, String newPassword) {
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    public void updateNotifications(Account account, Notifications notifications) {
        /*
        account.setStudyCreatedByWeb(notifications.isStudyCreatedByWeb());
        account.setStudyCreatedByEmail(notifications.isStudyCreatedByEmail());
        account.setStudyUpdatedByWeb(notifications.isStudyUpdatedByWeb());
        account.setStudyUpdatedByEmail(notifications.isStudyUpdatedByEmail());
        account.setStudyEnrollmentResultByEmail(notifications.isStudyEnrollmentResultByEmail());
        account.setStudyEnrollmentResultByWeb(notifications.isStudyEnrollmentResultByWeb());
        */
        modelMapper.map(notifications, account);
        accountRepository.save(account);
    }

    public void updateAccount(Account account, NicknameForm nicknameForm, HttpServletRequest request, HttpServletResponse response) {
        account.setNickname(nicknameForm.getNickname());
        accountRepository.save(account);
        login(account, request, response);
    }

    public void sendLoginLink(Account account) {
        Context context = new Context();
        context.setVariable("link", "/login-by-email?token=" + account.getEmailCheckToken() +
                "&email=" + account.getEmail());
        context.setVariable("nickName", account.getNickname());
        context.setVariable("linkName", "이메일로 로그인하기");
        context.setVariable("message", "스터디올래 로그인하려면 클릭하세요.");
        context.setVariable("host", appProperties.getHost());

        String message = templateEngine.process("/mail/simple-link", context);

        account.generateEmailCheckToken();
        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("스터디올래, 로그인 링크")
                .message(message)
                .build();
        emailService.sendEmail(emailMessage);
    }

    public void addTag(Account account, Tag tag) {
        // Optional<T> : null이 올 수 있는 값을 감싸는 wrapper class
        Optional<Account> byId = accountRepository.findById(account.getId());
        // ifPresent : void, Optional 객체가 값을 가지고 있다면 실행, 없다면 넘어감
        byId.ifPresent(a -> a.getTags().add(tag));
    }

    public Set<Tag> getTags(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        // orElseThrows : Optional의 인자가 null일 경우 예외처리
        return byId.orElseThrow().getTags();
    }

    public void removeTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().remove(tag));
    }

    public Set<Zone> getZones(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        return byId.orElseThrow().getZones();
    }

    public void addZone(Account account, Zone zone) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getZones().add(zone));
    }

    public void removeZone(Account account, Zone zone) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getZones().remove(zone));
    }
}
