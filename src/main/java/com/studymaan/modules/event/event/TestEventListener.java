package com.studymaan.modules.event.event;

import com.studymaan.modules.study.Study;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Async
@Component
@Transactional
@RequiredArgsConstructor
public class TestEventListener {

    @EventListener
    public void test(TestUpdateEvent testUpdateEvent) {
        log.info("이벤트리스너 테스트 {}", testUpdateEvent.getMessage());
        System.out.println(testUpdateEvent.getMessage());
    }

}
