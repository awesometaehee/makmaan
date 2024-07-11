package com.studymaan.modules.main;

import com.studymaan.modules.account.AccountRepository;
import com.studymaan.modules.account.CurrentAccount;
import com.studymaan.modules.account.Account;
import com.studymaan.modules.event.EnrollmentRepository;
import com.studymaan.modules.study.Study;
import com.studymaan.modules.study.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final StudyRepository studyRepository;
    private final AccountRepository accountRepository;
    private final EnrollmentRepository enrollmentRepository;

    @GetMapping("/")
    public String home(@CurrentAccount Account account, Model model) {
        if(account != null) {
            Account accountLoaded = accountRepository.findAccountWithTagsAndZonesById(account.getId());
            model.addAttribute(accountLoaded);
            // 스터디 참석할 모임 목록
            model.addAttribute("enrollmentList", enrollmentRepository.findByAccountAndAcceptedOrderByEnrolledAtDesc(accountLoaded, true));
            // 스터디 목록
            model.addAttribute("studyList", studyRepository.findByAccount(accountLoaded.getTags(), accountLoaded.getZones()));
            // 관리중인 스터디 목록
            model.addAttribute("studyManagerOf", studyRepository.findFirst5ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(account, false));
            // 참여중인 스터디 목록
            model.addAttribute("studyMemberOf", studyRepository.findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(account, false));
            return "index-after-login";
        }

        model.addAttribute("studyList", studyRepository.findFirst9ByPublishedAndClosedOrderByPublishedDateTimeDesc(true, false));
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // Spring JPA Pageable -> size, page, sort
    @GetMapping("/search/study")
    public String searchStudy(@PageableDefault(size = 9, sort = "publishedDateTime", direction = Sort.Direction.DESC) Pageable pageable
            , String keyword, Model model) {
        Page<Study> searchPage = studyRepository.findByKeyword(keyword, pageable);
        int currentPage = searchPage.getNumber() + 1; // 현재 페이지
        int startPage = Math.max(currentPage - 4, 1); // 현재 페이지의 주변에 표시할 시작 페이지 번호
        int endPage = Math.min(currentPage + 4, searchPage.getTotalPages()); // 현재 페이지의 주변에 표시할 마지막 페이지 번호
        int firstPage = 0; // 처음
        int lastPage = searchPage.getTotalPages() - 1; // 끝
        model.addAttribute("searchPage", searchPage);
        model.addAttribute("current", currentPage);
        model.addAttribute("start", startPage);
        model.addAttribute("end", endPage);
        model.addAttribute("first", firstPage);
        model.addAttribute("last", lastPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortProperty", pageable.getSort().toString().contains("publishedDateTime") ? "publishedDateTime" : "memberCount");
        return "/search";
    }
}
