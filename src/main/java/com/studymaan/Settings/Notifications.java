package com.studymaan.Settings;

import com.studymaan.domain.Account;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
// @NoArgsConstructor
public class Notifications {

    private boolean studyCreatedByEmail;

    private boolean studyCreatedByWeb;

    private boolean studyEnrollmentResultByEmail;

    private boolean studyEnrollmentResultByWeb;

    private boolean studyUpdatedByEmail;

    private boolean studyUpdatedByWeb;
}
