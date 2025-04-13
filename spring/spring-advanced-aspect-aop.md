# (spring) @Aspect 프록시
> LogTrace 에 @Aspect 프록시 적용하기, 자동 프록시 생성기와 @Aspect 동작 방식

<br>

## @Aspect 프록시 
- Advisor 를 직접 만들어 빈으로 등록하면 자동 프록시 생성기가 빈으로 등록된 Advisor 를 찾아 프록시를 자동으로 적용해주는데, 더욱 편리한 방법으로 `@Aspect` 가 있다. 
- 관점 지향 프로그래밍(AOP) 를 가능하게 하는 AspectJ 프로젝트에서 제공하는 애노테이션이다.
- 스프링은 이것을 차용해서 `@Aspect` 로 매우 편리하게 포인트컷과 어드바이스로 구성되어 있는 Advisor 생성 기능을 지원한다. 

<br>

### LogTrace 에 @Aspect 프록시 적용하기
#### 1. LogTraceAspect 생성하기
- 애노테이션 기반 프록시 적용을 위해 `@Aspect` 가 필요하다.
- `@Around` 의 값에는 포인트컷 AssertJ 표현식을 넣고 메서드는 어드바이스가 된다. 
- `joinPoint` 는 `MethodInvocation invocation` 과 유사한 기능으로 내부에 실제 호출 대상, 젇날 인자, 어떤 객체와 메서드가 호출되었는지 정보가 포함된다.
```java
@Aspect  
@RequiredArgsConstructor @Slf4j  
public class LogTraceAspect {  
    private final LogTrace logTrace;  
  
    @Around("execution(* hello.proxy.app..*(..))") // 포인트컷 표현식  
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable { // Advice   
TraceStatus status = null;  
        try {  
            String message = joinPoint.getSignature().toShortString();  
            status = logTrace.begin(message);  
            Object result = joinPoint.proceed(); // 로직 호출  
            logTrace.end(status);  
            return result;  
        } catch (Exception e) {  
            logTrace.exception(status, e);  
            throw e;  
        }  
    }  
}
```

#### 2. LogTraceAspect 빈 등록하기
- `@Aspect `가 있어도 스프링 빈으로 등록해줘야 하며 `LogTraceAspect` 에 `@Component` 를 붙여 컴포넌트 스캔을 사용하여 빈으로 등록할 수도 있다. 
```java
@Configuration  
public class AopConfig {  
    @Bean  
    public LogTraceAspect logTraceAspect(LogTrace logTrace) {  
        return new LogTraceAspect(logTrace);  
    }  
    @Bean  
    public LogTrace logTrace() {  
        return new ThreadLocalLogTrace();  
    }  
}
```

<br>

## 자동 프록시 생성기와 @Aspect
- 자동 프록시 생성기는 Advisor 를 자동으로 찾아와 필요한 곳에 프록시를 생성하고 적용하는데, 여기에 추가로 `@Aspect` 를 찾아 Advisor 로 만들어주는 역할도 한다. 
> 1. @Aspect 를 보고 Advisor 로 변환해서 저장한다. 
> 2. Advisor 를 기반으로 프록시를 생성한다.

<br>

### 동작 방식
#### @Aspect 를 어드바이저로 변환해서 저장하는 과정
1. 스프링 애플리케이션 로딩 시점에 자동 프록시 생성기를 호출한다.
2. 자동 프록시 생성기는 스프링 컨테이너에서 `@Aspect` 가 붙은 빈을 모두 조회한다.
3. `@Aspect` 어드바이저 빌더를 통해 `@Aspect` 정보를 기반으로 Advisor 를 생성한다.
4. 생성한 Advisor를 `@Aspect` 어드바이저 빌더 내부에 저장한다. 

#### Advisor 를 기반으로 프록시 생성
![image](https://github.com/user-attachments/assets/c5b00978-faef-49c8-a71c-4866a9ee08f8)
1. 생성 후 전달 : 스프링 빈 대상이 되는 객체를 생성하여 빈 후처리기에 전달 ( `@Bean`, 컴포넌트 스캔 모두 포함 ) 
2. 모든 Advisor 빈 조회 : 스프링 컨테이너에서 Advisor 빈을 모두 조회한다.
3. <b>@Aspect Advisor 조회 : `@Aspect` 어드바이저 빌더 내부에 저장된 Advisor 를 모두 조회한다. </b>
4. 시 적용 대상 체크 : 앞서 조회한 Advisor 에 포함되어 있는 포인트컷을 사용해서 객체 클래스 정보 및 모든 메서드를 포인트컷에 매칭하여 조건이 하나라도 만족하면 프록시 적용 대상임을 판단
5. 프록시 생성 : 프록시 적용 대상이면 프록시를 생성하고 반환해서 프록시를 스프링 빈으로 등록
6. 등록 : 반환된 객체는 스프링 빈으로 등록

#### @Aspect 어드바이저 빌더
- `BeanFactoryAspectJAdvisorsBuilder` 클래스로, `@Aspect` 의 정보를 기반으로 포인트컷, 어드바이스, 어드바이저를 생성하고 보관하는 것을 담당한다.
- `@Aspect` 의 정보를 기반으로 Advisor 를 만들고 `@Aspect` 어드바이저 빌더 내부 저장소에 캐시한다.

<br>

## 참고 
[인프런 - 스프링 핵심 원리 고급편](https://inf.run/FWeFN) 