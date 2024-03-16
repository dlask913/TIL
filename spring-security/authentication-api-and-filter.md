# Spring Security 인증 API 및 필터
> spring security version 5.2.x 기준

<br>

## 스프링 시큐리티 의존성 추가 시 일어나는 일들
- 서버가 기동되면 스프링 시큐리티의 초기화 작업 및 보안 설정이 이루어진다
- 별도 설정이나 구현을 하지 않아도 기본적인 웹 보안 기능이 연동되어 작동
1. 모든 요청은 인증되어야 자원에 접근 가능하다
2. 인증 방식은 form login 방식과 httpBasic 로그인 방식을 제공한다
3. 기본 로그인 페이지 제공
4. 기본 계정 한 개 제공 ( user / 랜덤 문자열 )
  
<br>

## 인증 API - Http Basic 인증
```java
http.httpBasic();
```
- 간단한 설정과 Stateless ( Session Cookie - JESSIONID 사용 X )
- Client 는 ID,Password 값을 Base64 로 인코딩한 후 Authorization Header 에 추가하여 Server에 리소스 요청
- 외부에 쉽게 노출되기 떄문에 SSL 이나 TLS 필수

<br>

## 인증 API - Form Login 인증
  
```java
http.formLogin()
    .loginPage("/login.html") // 사용자 정의 로그인 페이지
    .defaultSuccessUrl("/home") // 로그인 성공 후 이동 페이지
    .failureUrl("/login.html?error=true") // 로그인 실패 후 이동 페이지
    .usernameParameter("username") // 아이디 파라미터명 설정
    .passwordParameter("password") // 패스워드 파라미터명 설정
    .loginProcessingUrl("/login") // 로그인 Form Action Url
    .successHandler(loginSuccessHandler()) // 로그인 성공 후 핸들러
    .failureHandler(loginFailureHandler()) // 로그인 실패 후 핸들러
```

<br>

## 인증 API - UsernamePasswordAuthenticationFilter 
- 사용자가 로그인하게 되면 인증 처리가 이루어지는데 이 인증 처리를 담당하고 인증 처리에 관련된 요청을 처리하는 필터
- AuthenticationManger 에서 인증성공하면 Authentication을 SecurityContext에 저장하고, 인증이 실패하면 AuthenticationProver 에서 AuthenticationException 예외가 발생하여 필터가 failure
- SecurityContext : 인증 객체를 저장하는 저장소 또는 보관소 ( 전역적으로 인증 객체를 참조할 수 있도록 설계된 객체 )

<br>

## 인증 API - Logout : 로그아웃 기능 작동
```java
http.logout()   // 로그아웃 처리
    .logoutUrl("/logout")   // 로그아웃 처리 URL
    .logoutSuccessUrl("/login")     // 로그아웃 성공 후 이동페이지
    .deleteCookies("JSESSIONID", "remember-me") // 로그아웃 후 쿠키 삭제
    .addLogoutHandler(logoutHandler())  // 로그아웃 핸들러
    .logoutSuccessHandler(logoutSuccessHandler()) // 로그아웃 성공 후 핸들러
```
- request(/logout) 시 Server 는 세션 무효화, 인증토큰 삭제, 쿠키정보 삭제, 로그인 페이지로 리다이렉트
  
<br>

## 인증 API - Remember Me 인증 (RememberMeAuthenticationFilter)
1. 세션이 만료되고 웹 브라우저가 종료된 후에도 어플리케이션이 사용자를 기억하는 기능
2. Remember-Me 쿠키에 대한 Http 요청을 확인한 후 토큰 기반 인증을 사용해 유효성을 검사하고 토큰이 검증되면 사용자는 로그인된다.
3. 사용자 라이프 사이클
  - 인증 성공(Remeber-Me 쿠키 설정)
  - 인증 실패(쿠키가 존재하면 쿠키 무효화)
  - 로그아웃(쿠키가 존재하면 쿠키 무효화)
```java
http.rememberMe() // rememberMe 기능 작동
    .rememberMeParameter("remember") // 기본 파라미터명은 remember-me
    .tokenValiditySeconds(3600) // Default 는 14일
    .alwaysRemember(true) // 리멤버 미 기능이 활성화되지 않아도 항상 실행
    .userDetailsService(userDetailsService)
```

<br>

## 인증 API - AnonymousAuthenticationFilter
- 익명사용자 인증 처리 필터
- 익명사용자와 인증사용자를 구분해서 처리하기 위한 용도로 사용
- 화면에서 인증 여부를 구현할 때 isAnonymous() 와 isAuthenticated() 로 구분해서 사용
- 인증객체를 세션에 저장하지 X

<br>

## 인증 API - 동시 세션 제어
```java
http.sessionManagement() // 세션 관리 기능 작동
    .maximumSessions(1) // 최대 허용 가능 세션 수, -1 : 무제한 로그인 세션 허용
    .maxSessionsPreventsLogin(true) // 동시 로그인 차단, false: 기존 세션 만료 (default)
    .invalidSessionUrl("/invalid") // 세션이 유효하지 않을 때 이동할 페이지
    .expiredUrl("/expired") // 세션이 만료된 경우 이동할 페이지
```

<br>

## 인증 API - 세션 고정 보호
```java
http.sessionManagement() // 세션 관리 기능이 작동
    .sessionFixation().changeSessionId() // 기본값 ( none,migrateSession,newSession )
```

<br>

## 인증 API - 세션 정책
```java
http.sessionManagement()
    .sessionCreationPolicy(SessionCreationPolicy.If_Required)
```
- SessionCreationPolicy.Always : 스프링 시큐리티가 항상 세션 생성
- SessionCreationPolicy.If_Required : 스프링 시큐리티가 필요 시 생성 (기본값)
- SessionCreationPolicy.Never : 스프링 시큐리티가 생성하지 않지만 이미 존재하면 사용
- SessionCreationPolicy.Stateless : 스프링 시큐리티가 생성하지 않고 존재해도 사용하지 않음

<br>

## 인증 API - SessionManagementFilter
1. 세션 관리 : 인증 시 사용자의 세션 정보를 등록, 조회, 삭제 등의 세션 이력 관리
2. 동시적 세션 제어 : 동일 계정으로 접속이 허용되는 최대 세션 수를 제한
3. 세션 고정 보호 : 인증 할 때마다 세션쿠키를 새로 발급하여 공격자의 쿠키 조작을 방지
4. 세션 생성 정책 : Always,If_required,Never,Stateless

<br>

## 인증 API - ConcurrentSessionFilter
- SessionmanagementFilter 와 연계해서 동시적 세션 제어 처리
- 매 요청마다 현재 사용자의 세션 만료 여부 체크
- 세션이 만료되었을 경우 즉시 만료 처리
- session.isExpired() == true → 로그아웃 처리, 즉시 오류 페이지 응답 ( expired )

<br>

## 참고
[스프링 시큐리티](https://inf.run/GJpP)