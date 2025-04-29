# (spring) 프록시와 내부 호출
> 프록시와 내부 호출 문제점 및 해결 ( 자기 자신 주입, 지연 조회, 구조 변경 )

<br>

## 문제점
- AOP 를 적용하기 위해서는 항상 프록시를 통해 대상 객체를 호출해야 프록시에서 먼저 어드 바이스를 호출하고 이후에 대상 객체를 호출한다. 
- 만약 프록시를 거치지 않고 대상 객체를 호출하면 AOP 가 적용되지 않아 어드바이스도 호출되지 않는다. 

### 내부 호출 예제 
#### 1. Service 로직 작성
- `external()` 을 호출하면 내부에서 `internal()` 이라는 자기 자신의 메서드를 호출한다. 
- 자바에서는 메서드를 호출할 때 대상을 지정하지 않으면 `this` 가 붙게 된다.
```java
@Slf4j  
@Component  
public class CallService {  
  
    public void external() {  
        log.info("call external");  
        internal(); // 내부 메서드 호출(this.internal())  
    }  
  
    public void internal() {  
        log.info("call internal");  
    }  
}
```

#### 2. AOP 적용을 위한 Aspect 생성
```java
@Slf4j  
@Aspect  
public class CallLogAspect {  
    @Before("execution(* hello.aop.internalcall..*.*(..))")  // 패키지 필터링
    public void doLog(JoinPoint joinPoint) {  
        log.info("aop={}", joinPoint.getSignature());  
    }  
}
```

#### 3. 메서드 호출 테스트 코드 작성 및 실행 결과
- 서비스 로직을 호출하는 테스트 코드를 만들어, `external()` 을 실행한다.
- `external()` 을 실행할 때는 프록시를 호출하는데 내부에서 `internal()` 을 호출할 때 `CallLogAspect` 어드바이스가 호출되지 않는 것을 확인할 수 있다. 
```java
@Slf4j  
@Import(CallLogAspect.class)  
@SpringBootTest  
class CallServiceTest {  
  
    @Autowired CallService callService;  
  
    @Test  
    void external() {  
        callService.external();  
    }  
}

/** 실행 결과
aop=void hello.aop.internalcall.CallServiceV0.external()
call external
call internal
**/
```

### 동작 방식
![image](https://github.com/user-attachments/assets/265f9187-2786-4ace-8b0e-6dac6daaf557)

- 외부에서 `internal()` 을 호출하는 경우 프록시를 거치기 때문에 어드바이스가 적용되지만 내부 호출 ( `this.internal()` ) 인 경우 프록시를 거치지 않기 때문에 어드바이스를 적용할 수 없다. 
- 참고로, 실제 코드에 AOP 를 직접 적용하는 AspectJ 를 사용하면 프록시가 아니라 해당 코드에 직접 AOP 코드가 붙어 있기 때문에 내부 호출과 무관하게 AOP 를 적용할 수 있다. ( 설적이 복잡하고 JVM 옵션 필요 )

<br>

## 해결
### 대안1. 자기 자신 주입
> 스프링 부트 2.6 부터는 순환 참조를 기본적으로 금지하도록 정책이 변경되었기 때문에 2.6부터는 오류가 발생한다. ( spring.main.allow-circular-references=true 옵션으로 해결 가능 )

- 자기 자신을 의존관계 주입받아 프록시 인스턴스를 통해 호출하여 해결한다. 
- 생성자 주입 시 본인을 생성하면서 주입해야 하기 때문에 순환 사이클이 만들어져 오류가 발생한다. 
- 수정자 주입을 사용하면 스프링이 생성된 이후에 주입할 수 있기 때문에 오류가 발생하지 않는다. 
```java
@Slf4j  
@Component  
public class CallService {  
  
    private CallService callService;  
  
    @Autowired  
    public void setCallService(CallService callService) { // 수정자 주입
        this.callService = callService;  
    }  
  
    public void external() {  
        log.info("call external");  
        callService.internal(); // 외부 메서드 호출  
    }  
  
    public void internal() {  
        log.info("call internal");  
    }  
}
```

<br>

### 대안2. 지연 조회
- 스프링 빈을 지연해서 조회할 수 있는데, `ObjectProvider(Provider)`, `ApplicationContext` 를 사용할 수 있다. 
- `ObjectProvider` 는 객체를 스프링 컨테이너에서 조회하는 것을 스프링 빈 생성 시점이 아니라 실제 객체를 사용하는 시점으로 지연할 수 있다. ( 순환 사이클 발생 X )
```java
@Slf4j  
@Component  
public class CallService {  
  
    private final ObjectProvider<CallService> callServiceProvider;  
  
    public CallService(ObjectProvider<CallService> callServiceProvider) {  
        this.callServiceProvider = callServiceProvider;  
    }  
  
    public void external() {  
        log.info("call external");  
        CallService callService = callServiceProvider.getObject();  
        callService.internal(); // 외부 메서드 호출  
    }  
  
    public void internal() {  
        log.info("call internal");  
    }  
}
```

<br>

### 대안3. 구조 변경
- 가장 나은 대안으로, 내부 호출이 발생하지 않도록 구조를 변경한다. ( 가장 권장 )
- `InternalService` 라는 별도의 클래스로 분리하여 클라이너트가 `external()`, `internal()` 을 모두 호출하도록 구조를 변경한다.
```java
@Slf4j  
@Component  
@RequiredArgsConstructor  
public class CallServiceV3 {  
    private final InternalService internalService;  
    public void external() {  
        log.info("call external");  
        internalService.internal(); // 외부 메서드 호출  
    }  
}

@Slf4j  
@Component  
public class InternalService {  
    public void internal() {  
        log.info("call internal");  
    }  
}
```

<br>

### 참고사항
- AOP 는 주로 트랜잭션 적용이나 주요 컴포넌트의 로그 출력 기능에 사용된다. 
- AOP 는 `public` 메서드에만 적용하고 `private` 처럼 작은 단위에는 적용하지 않는다. 
- AOP 적용을 위해 `private` 메서드를 외부 클래스로 변경하고 `public` 으로 변경하는 일은 거의 없으나 위 예제처럼 `public` 메서드에서 `public` 메서드를 내부 호출하는 경우에 문제가 발생하기 때문에 주의해야 한다.


<br>

## 참고 
[인프런 - 스프링 핵심 원리 고급편](https://inf.run/FWeFN) 