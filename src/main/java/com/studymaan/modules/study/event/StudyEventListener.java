package com.studymaan.modules.study.event;

import com.studymaan.infra.config.AppProperties;
import com.studymaan.infra.mail.EmailMessage;
import com.studymaan.infra.mail.EmailService;
import com.studymaan.modules.account.Account;
import com.studymaan.modules.account.AccountPredicates;
import com.studymaan.modules.account.AccountRepository;
import com.studymaan.modules.notification.Notification;
import com.studymaan.modules.notification.NotificationRepository;
import com.studymaan.modules.notification.NotificationType;
import com.studymaan.modules.study.Study;
import com.studymaan.modules.study.StudyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;

@Slf4j
@Async
@Component
@Transactional
@RequiredArgsConstructor
public class StudyEventListener {

    private final StudyRepository studyRepository;
    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;
    private final NotificationRepository notificationRepository;

    @EventListener
    public void handlerStudyCreatedEvent(StudyCreatedEvent studyCreatedEvent) {
        Study study = studyRepository.findStudyWithTagsAndZonesById(studyCreatedEvent.getStudy().getId());
        Iterable<Account> accounts = accountRepository.findAll(AccountPredicates.findByTagsAndZones(study.getTags(), study.getZones()));
        accounts.forEach(account -> {
            if(account.isStudyCreatedByEmail()) {
                // 이메일 전송
                sendStudyCreatedEmail(account, study);
            }

            if(account.isStudyCreatedByWeb()) {
                // 웹 알림
                saveStudyCreatedNotification(account, study);
            }
        });
    }

    private void sendStudyCreatedEmail(Account account, Study study) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link", "/study/" + study.getStudyPath(study.getPath()));
        context.setVariable("linkName", study.getTitle());
        context.setVariable("message", "새로운 스터디가 생겼습니다");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .subject("스터디올래, '" + study.getTitle() + "' 스터디가 생겼습니다.")
                .to(account.getEmail())
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }

    private void saveStudyCreatedNotification(Account account, Study study) {
        Notification notification = new Notification();
        notification.setTitle(study.getTitle());
        notification.setLink("/study/" + study.getStudyPath(study.getPath()));
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(study.getShortDescription());
        notification.setAccount(account);
        notification.setNotificationType(NotificationType.STUDY_CREATED);
        notificationRepository.save(notification);
    }
}
