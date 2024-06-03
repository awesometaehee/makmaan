package com.studymaan.modules.event.event;

import com.studymaan.modules.event.Enrollment;

public class EnrollmentAcceptedEvent extends EnrollmentEvent {
    public EnrollmentAcceptedEvent(Enrollment enrollment) {
        super(enrollment, "모임 참가 신청을 수락헀습니다. 모임에 출석하세요.");
    }
}
