package com.studymaan.config;

import com.studymaan.account.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.SecurityBuilder;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import javax.sql.DataSource;
import java.nio.file.PathMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AccountService accountService;
    private final DataSource dataSource;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // static 한 리소스들은 security 필터를 적용하지 마라
        return web -> web
                .ignoring()
                .requestMatchers("/node_modules/**")
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 특정 요청들은 Security 체크하지 않도록 설정
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        .requestMatchers("/", "/login", "/sign-up", "/check-email-token"
                        , "/email-login", "/check-email-login", "/login-link", "/profile/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/profile/*").permitAll()
                        .anyRequest().authenticated());

        // 로그인 페이지 설정
        http.formLogin(form -> form.loginPage("/login").permitAll());
        // 로그아웃 시 페이지 설정
        http.logout(logout -> logout.logoutSuccessUrl("/"));
        // 로그인 쿠키 및 세션 유지 설정
        http.rememberMe(remember -> remember.userDetailsService(accountService).tokenRepository(tokenRepository()));

        return http.build();
    }

    @Bean
    public PersistentTokenRepository tokenRepository() {
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        return jdbcTokenRepository;
    }

}
