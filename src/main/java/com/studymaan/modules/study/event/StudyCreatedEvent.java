package com.studymaan.modules.study.event;

import com.studymaan.modules.study.Study;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StudyCreatedEvent {

    private final Study study;
}
