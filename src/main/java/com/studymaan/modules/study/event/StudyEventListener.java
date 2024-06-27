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
import java.util.HashSet;
import java.util.Set;

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
        /**
         * studyCreatedEvent 에서 tags, zones를 가져오지 않은 객체가 들어옴
         * 첫번째 해결 방법은 tags, zones 등 필요한 정보를 패칭한 상태로 객체(studyCreatedEvent)에 담아준다
         * 두번째, 이벤트를 처리할 때 조회한다. <- 이 방법으로 선택
         */
        Study study = studyRepository.findStudyWithTagsAndZonesById(studyCreatedEvent.getStudy().getId());
        Iterable<Account> accounts = accountRepository.findAll(AccountPredicates.findByTagsAndZones(study.getTags(), study.getZones()));
        accounts.forEach(account -> {
            if(account.isStudyCreatedByEmail()) {
                // 이메일 전송
                sendStudyCreatedEmail(account, study, "새로운 스터디가 생겼습니다.",
                        "스터디올래, '" + study.getTitle() + "' 스터디가 생겼습니다.");
            }

            if(account.isStudyCreatedByWeb()) {
                // 웹 알림
                createStudyNotification(account, study, NotificationType.STUDY_CREATED);
            }
        });
    }

    @EventListener
    public void handlerStudyUpdateEvent(StudyUpdateEvent studyUpdateEvent) {
        Study study = studyRepository.findStudyWithManagersAndMembersById(studyUpdateEvent.getStudy().getId());
        Set<Account> accounts = new HashSet<>();
        accounts.addAll(study.getManagers());
        accounts.addAll(study.getMembers());
        accounts.forEach(account -> {
            if(account.isStudyUpdatedByEmail()) {
                sendStudyCreatedEmail(account, study, studyUpdateEvent.getMessage(),
                        "스터디올래, '" + study.getTitle() + "스터디에 새 소식이 생겼습니다.");
            }

            if(account.isStudyUpdatedByWeb()) {
                createStudyNotification(account, study, NotificationType.STUDY_UPDATED);
            }

        });
    }

    private void sendStudyCreatedEmail(Account account, Study study, String contextMessage, String emailSubject) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link", "/study/" + study.getStudyPath(study.getPath()));
        context.setVariable("linkName", study.getTitle());
        context.setVariable("message", contextMessage);
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .subject(emailSubject)
                .to(account.getEmail())
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }

    private void createStudyNotification(Account account, Study study, NotificationType notificationType) {
        Notification notification = new Notification();
        notification.setTitle(study.getTitle());
        notification.setLink("/study/" + study.getStudyPath(study.getPath()));
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(study.getShortDescription());
        notification.setAccount(account);
        notification.setNotificationType(notificationType);
        notificationRepository.save(notification);
    }
}
