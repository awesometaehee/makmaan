package com.studymaan.modules.account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studymaan.modules.account.form.*;
import com.studymaan.modules.tag.TagForm;
import com.studymaan.modules.tag.TagRepository;
import com.studymaan.modules.zone.Zone;
import com.studymaan.modules.zone.ZoneRepository;
import com.studymaan.modules.tag.Tag;
import com.studymaan.modules.account.validator.NicknameFormValidator;
import com.studymaan.modules.account.validator.PasswordFormValidator;
import com.studymaan.modules.tag.TagService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.studymaan.modules.account.SettingsController.ROOT;
import static com.studymaan.modules.account.SettingsController.SETTINGS;

@Controller
@RequestMapping(ROOT + SETTINGS)
@RequiredArgsConstructor
public class SettingsController {

    private final AccountService accountService;
    private final ModelMapper modelMapper;
    private final NicknameFormValidator nicknameFormValidator;
    private final TagService tagService;
    private final TagRepository tagRepository;
    private final ObjectMapper objectMapper;
    private final ZoneRepository zoneRepository;

    public static final String ROOT = "/";
    public static final String SETTINGS = "settings";
    public static final String PROFILE = "/profile";
    public static final String PASSWORD = "/password";
    public static final String NOTIFICATIONS = "/notifications";
    public static final String ACCOUNT = "/account";
    public static final String TAGS = "/tags";
    public static final String ZONES = "/zones";

    @InitBinder("passwordForm")
    public void passwordFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    @InitBinder("nicknameForm")
    public void nicknameFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(nicknameFormValidator);
    }

    @GetMapping( PROFILE)
    public String updateProfileForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, Profile.class));

        return SETTINGS + PROFILE;
    }

    @PostMapping(PROFILE)
    public String updateProfile(@CurrentAccount Account account, @Valid Profile profile, Errors errors,
                                RedirectAttributes attributes, Model model) {
        if(errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS + PROFILE;
        }

        accountService.updateProfile(account, profile);
        attributes.addFlashAttribute("message", "프로필을 수정했습니다");
        return "redirect:/" + SETTINGS + PROFILE;
    }

    @GetMapping(PASSWORD)
    public String updatePasswordForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());

        return SETTINGS + PASSWORD;
    }

    @PostMapping(PASSWORD)
    public String updatePassword(@CurrentAccount Account account, @Valid PasswordForm passwordForm, Errors errors, Model model, RedirectAttributes attributes) {
        if(errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS + PASSWORD;
        }

        accountService.updatePassword(account, passwordForm.getNewPassword());
        attributes.addFlashAttribute("message", "패스워드가 변경되었습니다");
        return "redirect:/" + SETTINGS + PASSWORD;
    }

    @GetMapping(NOTIFICATIONS)
    public String updateNotificationsForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, Notifications.class));
        return SETTINGS + NOTIFICATIONS;
    }

    @PostMapping(NOTIFICATIONS)
    public String updateNotifications(@CurrentAccount Account account, @Valid Notifications notifications, Errors errors, Model model, RedirectAttributes attributes) {
        if(errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS + NOTIFICATIONS;
        }

        accountService.updateNotifications(account, notifications);
        attributes.addFlashAttribute("message", "알림 설정을 변경했습니다.");
        return "redirect:/" + SETTINGS + NOTIFICATIONS;
    }

    @GetMapping(ACCOUNT)
    public String updateAccountForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, NicknameForm.class));
        return SETTINGS + ACCOUNT;
    }

    @PostMapping(ACCOUNT)
    public String updateAccount(@CurrentAccount Account account, @Valid NicknameForm nicknameForm, Model model, Errors errors, RedirectAttributes attributes
                                , HttpServletRequest request, HttpServletResponse response) {
        if(errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS + ACCOUNT;
        }

        accountService.updateAccount(account, nicknameForm, request, response);
        attributes.addFlashAttribute("message", "닉네임을 변경했습니다.");
        return "redirect:/" + SETTINGS + ACCOUNT;
    }

    @GetMapping(TAGS)
    public String updateTags(@CurrentAccount Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);
        Set<Tag> tags = accountService.getTags(account);
        // tag title을 문자열로 수집(map)해서 콜렉터의 리스트로 변환
        model.addAttribute("tags", tags.stream().map(Tag::getTitle).collect(Collectors.toList()));
        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));
        return SETTINGS + TAGS;
    }

    @PostMapping(TAGS + "/add")
    @ResponseBody
    public ResponseEntity addTags(@CurrentAccount Account account, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle(); // client에서 받아온 tag title
        Tag tag = tagService.findOrCreateNew(title);
        accountService.addTag(account, tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping(TAGS + "/remove")
    @ResponseBody
    public ResponseEntity removeTags(@CurrentAccount Account account, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle(); // client에서 받아온 tag title
        Tag tag = tagService.findOrCreateNew(title);
        accountService.removeTag(account, tag);
        return ResponseEntity.ok().build();
    }

    @GetMapping(ZONES)
    public String updateZones(@CurrentAccount Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);
        Set<Zone> zones = accountService.getZones(account);
        model.addAttribute("zones", zones.stream().map(Zone::toString).collect(Collectors.toList()));
        List<String> allZones = zoneRepository.findAll().stream().map(Zone::toString).toList();
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allZones));
        return SETTINGS + ZONES;
    }

    @PostMapping(ZONES + "/add")
    @ResponseBody
    public ResponseEntity addZones(@CurrentAccount Account account, @RequestBody ZoneForm zoneForm) {
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if(zone == null) {
            return ResponseEntity.badRequest().build();
        }
        accountService.addZone(account, zone);
        return ResponseEntity.ok().build();
    }

    @PostMapping(ZONES + "/remove")
    public ResponseEntity removeZones(@CurrentAccount Account account, @RequestBody ZoneForm zoneForm) {
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if(zone == null) {
            return ResponseEntity.badRequest().build();
        }
        accountService.removeZone(account, zone);
        return ResponseEntity.ok().build();
    }
}
