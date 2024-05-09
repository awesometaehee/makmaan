package com.studymaan.modules.zone;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ZoneService {

    private final ZoneRepository zoneRepository;

    @PostConstruct // 빈이 초기화된 이후 코드 실행
    public void initZoneData() throws IOException {
        if(zoneRepository.count() == 0) {
            Resource resource = new ClassPathResource("zones_kr.csv");
            // csv 파일을 객체로 불러오기
            List<Zone> zoneList = Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8).stream()
                    .map(line -> {
                        String[] split = line.split(",");
                        return Zone.builder().city(split[0]).localNameOfCity(split[1]).province(split[2]).build();
                    }).toList();
            zoneRepository.saveAll(zoneList);
        }
    }
}
