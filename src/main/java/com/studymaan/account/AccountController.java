package com.studymaan.account;

import com.studymaan.domain.Account;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;

    @InitBinder("signUpForm") // Controller의 모든 요청 전에 initBinder를 선언한 메소드가 실행 - signUpForm
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping("/sign-up")
    public String signUpForm(Model model) {
        model.addAttribute("signUpForm", new SignUpForm());
        // model.addAttribute(new SignUpForm()); 클래스 네임과 attribute 네임이 같다면 이와 같이 생략 가능
        return "account/sign-up";
    }

    @PostMapping("/sign-up")
    public String signUpSubmit(@Validated SignUpForm signUpForm, Errors errors, HttpServletRequest request, HttpServletResponse response) {
        if(errors.hasErrors()) {
            return "account/sign-up";
        }

        Account account = accountService.processNewAccount(signUpForm);
        accountService.login(account, request, response);
        // TODO 회원가입 처리
        return "redirect:/";
    }

    @GetMapping("/check-email-token")
    public String checkEmailToken(String token, String email, Model model, HttpServletRequest request, HttpServletResponse response) {
        Account account = accountRepository.findByEmail(email);
        String view = "account/checked-email";
        if(account == null) {
            model.addAttribute("error", "wrong email");
            return view;
        }

        if(!account.isValidToken(token)) {
            model.addAttribute("error", "wrong token");
            return view;
        }

        accountService.completeSignUp(account, request, response);
        model.addAttribute("numberOfUser", accountRepository.count());
        model.addAttribute("nickname", account.getNickname());
        return view;
    }

    @GetMapping("/check-email")
    public String checkEmail(@CurrentUser Account account, Model model) {
        model.addAttribute("email", account.getEmail());
        return "account/check-email";
    }

    @GetMapping("/resend-confirm-email")
    public String resendConfirmEmail(@CurrentUser Account account, Model model) {
        if(!account.canSendConfirmEmail()) {
            model.addAttribute("error", "인증 메일은 1시간에 한번만 전송할 수 있습니다");
            model.addAttribute("email", account.getEmail());
            return "account/check-email";
        }

        accountService.sendSignUpConfirmEmail(account);

        return "redirect:/";
    }

    @GetMapping("/profile/{nickname}")
    public String viewProfile(@PathVariable String nickname, Model model, @CurrentUser Account account) throws IllegalAccessException {
        Account byNickname = accountRepository.findByNickname(nickname);
        if(nickname == null) {
            throw new IllegalAccessException(nickname + "에 해당하는 닉네임이 없습니다.");
        }

        model.addAttribute(byNickname);
        model.addAttribute("isOwner", byNickname.equals(account));

        return "account/profile";
    }
}
