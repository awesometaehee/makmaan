package com.studymaan.modules.study;

import com.studymaan.modules.account.CurrentAccount;
import com.studymaan.modules.account.Account;
import com.studymaan.modules.study.validator.StudyValidator;
import com.studymaan.modules.study.form.StudyForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.internal.bytebuddy.utility.RandomString;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

@Controller
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;
    private final ModelMapper modelMapper;
    private final StudyValidator studyValidator;
    private final StudyRepository studyRepository;

    @InitBinder("studyForm")
    public void studyFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(studyValidator);
    }

    @GetMapping("/new-study")
    public String newStudyForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new StudyForm());
        return "study/form";
    }

    @PostMapping("/new-study")
    public String newStudySubmit(@CurrentAccount Account account, @Valid StudyForm studyForm, Errors errors, Model model) {
        if(errors.hasErrors()) {
            model.addAttribute(account);
            return "study/form";
        }
        Study newStudy = studyService.createNewStudy(account, modelMapper.map(studyForm, Study.class));
        return "redirect:/study/" + URLEncoder.encode(newStudy.getPath(), StandardCharsets.UTF_8);
    }

    @GetMapping("/study/{path}")
    public String viewStudy(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudy(path);
        model.addAttribute(account);
        model.addAttribute(study);
        return "study/view";
    }

    @GetMapping("/study/{path}/members")
    public String viewStudyMembers(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudy(path);
        model.addAttribute(account);
        model.addAttribute(study);
        return "study/members";
    }

    @GetMapping("/study/{path}/join")
    public String joinStudy(@CurrentAccount Account account, @PathVariable String path) {
        Study study = studyRepository.findStudyWithMembersByPath(path);
        studyService.addMember(study, account);
        return "redirect:/study/" + study.getStudyPath(path) + "/members";
    }

    @GetMapping("/study/{path}/leave")
    public String leaveStudy(@CurrentAccount Account account, @PathVariable String path) {
        Study study = studyRepository.findStudyWithMembersByPath(path);
        studyService.removeMember(study, account);
        return "redirect:/study/" + study.getStudyPath(path) + "/members";
    }

    @GetMapping("/study/data")
    public String putData(@CurrentAccount Account account) {
        studyService.putData(account);
        return "redirect:/";
    }
}
