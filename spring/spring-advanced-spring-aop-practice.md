# (spring) 스프링 AOP 구현
> @Aspect 로 AOP 구현하기, 포인트컷 분리, 어드바이스 추가, 포인트컷 참조, 어드바이스 순서, 어드바이스 종류

<br>

## 스프링 AOP 구현

### 라이브러리 추가
```gradle
implementation 'org.springframework.boot:spring-boot-starter-aop'
```
- `@Aspect` 를 사용하려면 `@EnableAspectJAutoProxy` 를 스프링 설정에 추가해야 하지만 스프링 부트를 사용하면 자동으로 추가된다.

<br>

### @Aspect 로 AOP 구현하기
#### 1. 비즈니스 로직 작성
```java
@Slf4j @Repository  
public class OrderRepository {  
    public String save(String itemId) {  
        log.info("[orderRepository] 실행");  
        //저장 로직  
        if (itemId.equals("ex")) {  
            throw new IllegalStateException("예외 발생!");  
        }  
        return "ok";  
    }  
}

@Slf4j @Service  
public class OrderService {  
    private final OrderRepository orderRepository;  
  
    public OrderService(OrderRepository orderRepository) {  
        this.orderRepository = orderRepository;  
    }  
  
    public void orderItem(String itemId) {  
        log.info("[orderService] 실행");  
        orderRepository.save(itemId);  
    }  
}
```

#### 2. @Aspect 를 이용한 AOP 구현
- `@Around` 의 값은 포인트컷이 되며 메서드는 어드바이스가 된다. 
- `OrderService` 와 `OrderRepository` 의 모든 메서드는 AOP 적용 대상이 된다.
- 스프링 AOP 는 AspectJ 를 직접 사용하는 것이 아니라 AspectJ 의 문법을 차용하고 프록시 방식의 AOP 를 제공한다.
- `org.aspectj` 패키지 관련 기능은 `aspectjweaver.jar` 라이브러리가 제공하는 기능으로, `spring-boot-starter-aop` 를 포함하여 사용할 수 있다. 
```java
@Slf4j @Aspect  
public class AspectV1 {  
    // hello.aop.order 패키지와 하위 패키지를 지정하는 AspectJ 포인트컷 표현식
    @Around("execution(* hello.aop.order..*(..))")  
    public Object doLog(ProceedingJoinPoint joinPoint) throws Throwable {  
        log.info("[log] {}", joinPoint.getSignature()); //join point 시그니처  
        return joinPoint.proceed();  
    }  
}
```

#### 3. 스프링 빈으로 등록하여 실행 결과 확인
- `@Aspect` 는 컴포넌트 스캔이 되는 것은 아니기 때문에 스프링 빈으로 등록해야 한다. 
- `@Import` 는 주로 설정 파일을 추가할 때 사용하지만 간단하게 테스트할 때 사용하기 좋다. 
```java
@Slf4j  
@SpringBootTest  
@Import(AspectV1.class) // 스프링 빈 등록
public class AopTest {  
    @Autowired  
    OrderService orderService;  
    @Autowired  
    OrderRepository orderRepository;  
  
    @Test  
    void aopInfo() {  
        log.info("isAopProxy, orderService={}",  AopUtils.isAopProxy(orderService));  
        log.info("isAopProxy, orderRepository={}", AopUtils.isAopProxy(orderRepository));  
    }
}
/** 실행 결과
isAopProxy, orderService=true
isAopProxy, orderRepository=true
**/
```

> ※ 스프링 빈으로 등록하는 방법
> 1. @Bean 을 사용해서 직접 등록
> 2. @Component 컴포넌트 스캔을 사용해서 자동 등록
> 3. @Import 주로 설정 파일을 추가할 때 사용 ( @Configuration )

<br>

### 포인트컷 분리
> @Pointcut 을 사용해서 별도로 분리하기

- `@Pointcut` 에 포인트컷 표현식을 사용하고, 메서드 이름과 파라미터를 합쳐서 포인트컷 시그니처( signature )라고 한다.
- 메서드의 반환 타입은 `void` 여야 하고 코드 내용은 비워둔다. 
- 내부에서만 사용하면 private 을 사용해도 되지만 다른 Aspect 에서 참고하려면 public 을 사용한다. 
```java
@Slf4j @Aspect  
public class AspectV2 {  
    @Pointcut("execution(* hello.aop.order..*(..))")  
    private void allOrder(){} // 포인트컷 시그니처 (★)
  
    @Around("allOrder()")
    public Object doLog(ProceedingJoinPoint joinPoint) throws Throwable {  
        log.info("[log] {}", joinPoint.getSignature()); // join point 시그니처  
        return joinPoint.proceed();  
    }  
}
```

<br>

### doTransaction(..) 어드바이스 추가 
> && 연산 사용하여 별도 적용해보기

- 포인트컷은 `&&`(AND), `||`(OR), `!`(NOT) 3가지 조합이 가능하다.
- 결과적으로, `doTransaction()` 어드바이스는 `OrderService` 에만 적용되고 `OrderService` 와 `OrderRepository` 모두에 적용된다.
- 동작 순서 : 클라이언트 → [ doLog() → doTransaction() ] → orderService.orderItem() → [ doLog() ] → orderRepository.save()
```java
@Slf4j @Aspect  
public class AspectV3 {  
    @Pointcut("execution(* hello.aop.order..*(..))")  
    private void allOrder(){}
  
    @Pointcut("execution(* *..*Service.*(..))") // 클래스 이름 패턴이 *Service
    private void allService(){}  
  
    @Around("allOrder()")  
    public Object doLog(ProceedingJoinPoint joinPoint) throws Throwable {  
        log.info("[log] {}", joinPoint.getSignature()); // join point 시그니처  
        return joinPoint.proceed();  
    }  
  
    // hello.aop.order 패키지와 하위 패키지 AND 클래스 이름 패턴이 *Service    
    @Around("allOrder() && allService()")  
    public Object doTransaction(ProceedingJoinPoint joinPoint) throws Throwable {  
  
        try {  
            log.info("[트랜잭션 시작] {}", joinPoint.getSignature());  
            Object result = joinPoint.proceed();  
            log.info("[트랜잭션 커밋] {}", joinPoint.getSignature());  
            return result;  
        } catch (Exception e) {  
            log.info("[트랜잭션 롤백] {}", joinPoint.getSignature());  
            throw e;  
        } finally {  
            log.info("[리소스 릴리즈] {}", joinPoint.getSignature());  
        }  
    }  
}
```

<br>

### 포인트컷 참조
> 어드바이스 순서 변경을 위해 포인트컷 외부로 빼기 ( public )

#### 1. Pointcut 외부로 빼기
- 포인트컷을 공용으로 사용하기 위해 별도의 외부 클래스에 모아둘 수 있다. 
- 외부에서 호출하기 위해 접근 제어자를 public 으로 열어두어야 한다. 
```java
public class Pointcuts {  
    @Pointcut("execution(* hello.aop.order..*(..))") // 패키지 필터링
    public void allOrder(){}

	@Pointcut("execution(* *..*Service.*(..))") // 클래스 이름 필터링
    public void allService(){}  
  
    @Pointcut("allOrder() && allService()") // allOrder && allService  
    public void orderAndService() {}  
}
```

#### 2. Pointcut 참조
- 패키지명을 포함한 클래스 이름과 포인트컷 시그니처를 모두 지정하여 사용할 수 있다. 
- 포인트컷을 여러 어드바이스에서 함께 사용할 때 효과적이다.
```java
@Slf4j @Aspect  
public class AspectV4Pointcut {  
    @Around("hello.aop.order.aop.Pointcuts.allOrder()")  
    public Object doLog(ProceedingJoinPoint joinPoint) throws Throwable {  
        log.info("[log] {}", joinPoint.getSignature()); //join point 시그니처  
        return joinPoint.proceed();  
    }
}
```

<br>

### 어드바이스 순서
> 순서 지정을 위해 @Aspect 적용 단위로 org.springframework.core.annotation.@Order 적용하기

- 어드바이스는 기본적으로 순서를 보장하지 않기 때문에 `@Order` 를 적용해야 한다. 
- `@Order` 는 어드바이스 단위가 아니라 클래스 단위로 적용할 수 있기 때문에 하나의 Aspect 에 여러 어드바이스가 있으면 순서를 보장 받을 수 없다. → **Aspect 별도 클래스로 분리**
- 각 Aspect 에 실행 순서를 지정한다.

```java
/** doTransaction() → doLog() **/
@Slf4j  
public class AspectV5Order {  
    @Aspect  
    @Order(2)  
    public static class LogAspect { // 클래스로 분리
        @Around("hello.aop.order.aop.Pointcuts.allOrder()")  
        public Object doLog(ProceedingJoinPoint joinPoint) throws Throwable {  
            log.info("[log] {}", joinPoint.getSignature());
            return joinPoint.proceed();  
        }  
    }  
  
    @Aspect  
    @Order(1)  
    public static class TxAspect { // 클래스로 분리
        @Around("hello.aop.order.aop.Pointcuts.orderAndService()")  
        public Object doTransaction(ProceedingJoinPoint joinPoint) throws Throwable {  
  
            try {  
                log.info("[트랜잭션 시작] {}", joinPoint.getSignature());  
                Object result = joinPoint.proceed();  
                log.info("[트랜잭션 커밋] {}", joinPoint.getSignature());  
                return result;  
            } catch (Exception e) {  
                log.info("[트랜잭션 롤백] {}", joinPoint.getSignature());  
                throw e;  
            } finally {  
                log.info("[리소스 릴리즈] {}", joinPoint.getSignature());  
            }  
        }  
    }  
}
```

<br>

### 어드바이스 종류
- `@Around` : 메서드 호출 전후에 수행, 가장 강력한 어드바이스로 조인 포인트 실행 여부 선택, 반환 값 변환, 예외 변환 등이 가능하다. 
- `@Before` : 조인 포인트 실행 이전에 실행
- `@AfterReturning` : 조인 포인트가 정상 완료 후 실행
- `@AfterThrowing` : 메서드가 예외를 던지는 경우 실행
- `@After` : 조인 포인트가 정상 또는 예외에 관계없이 실행 ( finally )
- 모든 어드바이스는 `org.aspectj.lang.JoinPoint` 를 첫번째 파라미터에 사용할 수 있고 생략이 가능하다. 단, `@Around` 는 `ProceedingJoinPoint` 를 사용해야 한다. 

#### @Around 외 다른 어드바이스가 존재하는 이유
- `@Around`는 항상 `joinPoint` 를 호출해야 하는데 실수로 호출하지 않으면 **타겟이 호출되지 않는 치명적인 버그가 발생**한다. 
- `@Before`, `@After` 같은 어드바이스는 `joinPoint.proceed()` 를 호출하는 고민을 하지 않아도 되고 **의도가 명확하게** 드러난다.

#### 실행 순서
- @Around, @Before, @After, @AfterReturning, @AfterThrowing 
- 어드바이스가 적용되는 순서는 위와 같지만 호출 순서와 리턴 순서는 반대이다. 
- `@Aspect` 안에 동일한 종류의 어드바이스가 2개 있으면 순서가 보장되지 않기 때문에 이 경우 `@Aspect` 를 분리하고 `@Order` 를 적용한다. 

#### JoinPoint 인터페이스 주요 기능
- `getArgs()` : 메서드 인수 반환
- `getThis()` : 프록시 객체 반환
- `getTarget()` : 대상 객체 반환
- `getSignature()` : 조언되는 메서드 설명 반환
- `toString()` : 조안되는 방법에 대한 유용한 설명 인쇄

#### ProceedingJoinPoint 인터페이스 주요 기능
- `proceed()` : 다음 어드바이스나 타겟 호출

<br>

## 참고 
[인프런 - 스프링 핵심 원리 고급편](https://inf.run/FWeFN) 