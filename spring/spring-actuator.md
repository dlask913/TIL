## (spring) Actuator
> 다양한 Actuator Endpoint ( health, info, loggers, httpexchanges ) 및 보안

<br>

### Actuator 라이브러리 추가
```groovy
implementation 'org.springframework.boot:spring-boot-starter-actuator'
```

<br>

## 엔드포인트 설정
1. 엔드포인트 활성화
: shutdown 을 제외한 엔드포인트는 대부분 기본으로 활성화 ( on )
- shutdown 엔드포인트 활성화
```yml
management:  
  endpoint:  
    shutdown:  
      enabled: true
```

2. 엔드포인트 노출
: HTTP 와 JMX 를 선택할 수 있는데 JMX 를 잘 사용하지 않으므로 HTTP 노출 고려
- application.yml 
```yml
management:  
  endpoints:  
    web:  
      exposure:  
        include: "*"  # 모든 엔드포인트를 웹에 노출
        exclude: "env" # env 는 제외
``` 

3. localhost:8080/actuator 접속하여 노출된 기능 확인

<br> 

## Actuator 엔드포인트 목록 ( [공식문서](https://docs.spring.io/spring-boot/docs/current/actuator-api/htmlsingle/) )
- beans : 스프링 컨테이너에 등록된 스프링 빈 확인
- conditions : condition 을 통해서 빈을 등록할 때 평가 조건과 일치하거나 그렇지 않은 이유를 표시
- configprops : @ConfigurationProperties 확인
- env : Environment 정보 확인
- health : 애플리케이션 health 정보 확인
- httpexchanges : HTTP 호출 응답 정보 확인. ( HttpExchangeRepository 를 구현한 빈을 별도 등록 필요 )
- info : 애플리케이션 정보 확인
- loggers : 애플리케이션 로거 설정 확인 및 변경 가능
- metrics : 애플리케이션의 메트릭 정보 확인
- mappings : @RequestMapping 정보 확인
- threaddump : 스레드 덤프를 실행해서 출력
- shutdown : 애플리케이션 종료 

<br> 

## Health 
: 애플리케이션이 요청에 응답을 할 수 있는 지 뿐만 아니라 애플리케이션이 사용하는 데이터베이스가 응답하는지, 디스크 사용량에는 문제가 없는지도 확인할 수 있다. 
- 더 자세히 확인하기
```yml
management:  
  endpoint:  
    health:  
      show-details: always
      # show-components: always 로 설정할 경우 상태 정보만 간략하게 노출된다.
```
- 실행 결과 ( /actuator/health )
![image](https://github.com/dlask913/TIL/assets/79985588/043da571-12fe-4708-85c7-f8f63d20ec09)

<br> 

## Info
: 애플리케이션의 기본 정보를 노출.
- java : 자바 런타임 정보
- os : OS 정보
- env : Environment 에서 info. 로 시작하는 정보
- build : 빌드 정보 ( META-INF/build-info.properties 파일 필요 )
- git : git 정보 ( git.properties 파일 필요 )
> env, java, os 는 기본적으로 비활성화 되어있다. 

 ### java, os, env 활성화하기
```yml
management:  
  info:  
    java:  
      enabled: true  
    os:  
      enabled: true
    env:
      enabled: true
```

### 빌드 정보 노출
1. build.gradle 에 추가
```groovy
springBoot {  
    buildInfo()  
}
```
2. build/resources/main/META-INF/build-info.properties  확인

### git 정보 노출
- build.gradle 에 plugins 추가
> git 으로 관리되고 있는 프로젝트가 아니면 오류 발생.
```groovy
plugins {
	...
	id 'com.gorylenko.gradle-git-properties' version '2.4.1'
}
```
- 더 자세한 정보 확인하기
```yml
management:  
  info:  
    git:  
      mode: full
```

<br> 

## Loggers
: 로깅과 관련된 정보를 확인하고 실시간으로 변경할 수도 있다. 
> 로그를 별도로 설정하지 않으면 기본으로 INFO 를 사용.

- 좀 더 자세히 조회하기
: /actuator/loggeres/{로거이름}

### 실시간 로그 레벨 변경
: 운영 서버는 보통 INFO 로그 레벨을 사용하는데 서비스 운영 중 문제가 있어서 급하게 DEBUG 나 TRACE 로그를 확인하고 싶을 때 유용하다. 
- TRACE 로 로그 레벨 변경하기 : **POST /actuator/loggeres/{로거이름}**
```JSON
{
    "configuredLevel": "TRACE"
}
```
- 조회 : **GET /actuator/loggeres/{로거이름}**

<br> 

## Httpexchanges 
: HTTP 요청과 응답의 과거 기록을 확인할 수 있다. ```HttpExchangeRepository``` 인터페이스의 구현체를 빈으로 등록해야한다. 
> 기능에 제한이 많기 때문에 개발 단계에서만 사용하고 운영에서는 핀포인트, Zipkin 등 추천
- InMemoryHttpExchangeRepository 추가 ( 스프링 부트 제공 )
```java
@SpringBootApplication  
public class ActuatorApplication {  
  
    public static void main(String[] args) {  
        SpringApplication.run(ActuatorApplication.class, args);  
    }  
  
    @Bean  // 추가
    public InMemoryHttpExchangeRepository httpExchangeRepository() {  
        return new InMemoryHttpExchangeRepository();  
    }
}
```

<br> 

## Actuator 와 보안
: Actuator 가 제공하는 기능들은 애플리케이션 내부 정보를 너무 많이 노출하기 때문에 내부망을 사용하는 것이 안전하다. 예를 들어 외부망을 통해 8080 에만 접근할 수 있고 다른 포트는 **내부망에서만 접근**할 수 있다면 액츄에이터를 다른 포트에 설정하면 된다. 
- Actuator 포트 설정
```yml
management:  
  server:  
    port: 9292
```
- Actuator URL 경로에 인증 설정
: 포트를 분리하는 것이 어렵고 어쩔 수 없이 외부 인터넷 망을 통해 접근해야 한다면 /actuator 경로에 서블릿 필터, 스프링 인터셉터 또는 스프링 시큐리티를 통해 추가 개발 필요

<br>

## 참고 
[인프런 - 스프링 부트 핵심 원리와 활용](https://inf.run/7VBBx) 