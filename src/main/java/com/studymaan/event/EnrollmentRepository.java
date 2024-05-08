package com.studymaan.event;

import com.studymaan.domain.Account;
import com.studymaan.domain.Enrollment;
import com.studymaan.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByEventAndAccount(Event event, Account account);

    Enrollment findByEventAndAccount(Event event, Account account);
}
