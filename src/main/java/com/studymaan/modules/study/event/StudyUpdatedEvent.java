package com.studymaan.modules.study.event;

import com.studymaan.modules.study.Study;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;

@Getter
@RequiredArgsConstructor
public class StudyUpdatedEvent {

    private final Study study;

    private final String message;
}
