package com.studymaan.account;

import com.studymaan.domain.Account;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountService accountService;
    private final AccountRepository accountRepository;


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

        account.completeSignUp();
        accountService.login(account, request, response);
        model.addAttribute("numberOfUser", accountRepository.count());
        model.addAttribute("nickname", account.getNickname());
        return view;
    }
}
