package com.studymaan.modules.account;

import com.querydsl.core.types.Predicate;
import com.studymaan.modules.account.QAccount;
import com.studymaan.modules.tag.Tag;
import com.studymaan.modules.zone.Zone;

import java.util.Set;

public class AccountPredicates {

    // tag, zone이 포함된 account 조회
    public static Predicate findByTagsAndZones(Set<Tag> tags, Set<Zone> zones) {
        QAccount account = QAccount.account;
        return account.zones.any().in(zones).and(account.tags.any().in(tags));
    }
}
