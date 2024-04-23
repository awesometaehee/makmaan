package com.studymaan.account;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// @Retention : 커스텀 어노테이션 생성 시 매모리 유지 범위, 대상을 설정, RUNTIME : 어플리케이션이 동작하는 동안 항상 영향을 끼침
@Retention(RetentionPolicy.RUNTIME)
// @Target : 커스텀 어노테이션이 적용될 대상을 지정, PARAMETER : 일반적인 파라미터에 지정
@Target(ElementType.PARAMETER)
// @AuthenticationPrincipal : 세션정보 UserDetails에 접근 가능
@AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : account")
public @interface CurrentAccount {
}
