# (spring) 프록시 팩토리와 PointCut, Advice, Advisor
> JDK 동적 프록시와 CGLIB 중복 생성해야하는 경우, 프록시 팩토리, PointCut, Advice, Advisor

<br>

### JDK 동적 프록시와 CGLIB 중복 생성해야하는 경우
> - 인터페이스가 있는 경우와 그렇지 않은 경우 InvocationHandler 와 MethodInterceptor 를 중복으로 사용해야 하는 지 고민이 필요하다.
> - 동적 프록시를 통합해서 편리하게 만들어주는 프록시 팩토리를 사용하여 해결한다.

<br>

## 프록시 팩토리
- 대상에 인터페이스가 있으면 JDK 동적 프록시를 사용하고 구체 클래스만 있다면 CGLIB 를 사용한다.
- `proxyTargetClass=true` 옵션을 통해 인터페이스 여부와 상관없이 CGLIB 를 사용할 수 있다. 
- 프록시 팩토리를 사용하면 `Advice` 를 호출하는 전용 `InvocationHandler`, `org.aopalliance.intercept.MethodInterceptor` 를 내부에서 사용하기 때문에 `Advice` 만 만들면 된다. ( 부가 기능 구현 )
```java
@FunctionalInterface  
public interface MethodInterceptor extends Interceptor {  
	/* 메서드를 호출하는 방법, 현재 프록시 객체 인스턴스, args, 메서드 정보 등이 포함 */
    @Nullable  
    Object invoke(@Nonnull MethodInvocation invocation) throws Throwable;  
}
```
- 특정 조건에 맞을 때만 프록시 로직을 적용하는 필터링 기능을 적용하기 위해서는 `PointCut` 을 사용한다.
- 사용 흐름
![image](https://github.com/user-attachments/assets/47244509-7836-4b71-859a-d2269e881111)

<br>

### 프록시 팩토리 예제 ( 인터페이스 )

#### 1. MethodInterceptor 구현하여 TimeAdvice 생성하기
- `proceed()` : target 클래스의 정보가 이미 포함되어있기 때문에 target 정보를 필요로 하지 않는다.
```java
@Slf4j  
public class TimeAdvice implements MethodInterceptor {  
    @Override  
    public Object invoke(MethodInvocation invocation) throws Throwable {  
        log.info("TimeProxy 실행");  
        long startTime = System.currentTimeMillis();  
  
        Object result = invocation.proceed(); // target 클래스 호출
  
        long endTime = System.currentTimeMillis();  
        long resultTime = endTime - startTime;  
        log.info("TimeProxy 종료 resultTime={}", resultTime);  
        return result;  
    }  
}
```

#### 2. 인터페이스 및 로직 구현
```java
public interface ServiceInterface {  
    void save();  
    void find();  
}

@Slf4j  
public class ServiceImpl implements ServiceInterface {  
    @Override  
    public void save() {  
        log.info("save 호출");  
    }  
    @Override  
    public void find() {  
        log.info("find 호출");  
    }  
}
```

#### 3. 프록시 팩토리 적용
```java
@Test  
@DisplayName("인터페이스가 있으면 JDK 동적 프록시 사용")  
void interfaceProxy() {  
    ServiceInterface target = new ServiceImpl();  
    // 1. 프록시 팩토리 생성 시, 프록시의 호출 대상 함께 넘겨주기
    ProxyFactory proxyFactory = new ProxyFactory(target); 
    // 2. 부가 기능 로직 설정 ( Advice )
    proxyFactory.addAdvice(new TimeAdvice()); 
    // 3. 프록시 객체를 생성하고 결과 반환
    ServiceInterface proxy = (ServiceInterface) proxyFactory.getProxy();  
    log.info("targetClass={}", target.getClass()); // ServiceImpl
    log.info("proxyClass={}", proxy.getClass());  
  
    proxy.save();  // 4. 실행
  
    assertThat(AopUtils.isAopProxy(proxy)).isTrue(); // 프록시 팩토리를 통해서 프록시 생성되면 참
    assertThat(AopUtils.isJdkDynamicProxy(proxy)).isTrue(); // JDK 동적 프록시인 경우 참
    assertThat(AopUtils.isCglibProxy(proxy)).isFalse(); 
}
```

#### setProxyTargetClass 옵션
- ProxyTargetClass 옵션을 사용하면 인터페이스가 있어도 CGLIB 를 사용하고 클래스 기반 프록시를 사용할 수 있다. 
- 스프링 부트는 AOP 를 적용할 때 기본적으로 `proxyTargetClass=true` 로 설정해서 사용한다.
```java
ServiceInterface target = new ServiceImpl();  
ProxyFactory proxyFactory = new ProxyFactory(target);  
proxyFactory.setProxyTargetClass(true);  // CGLIB, 클래스 기반 프록시 사용하도록 설정
proxyFactory.addAdvice(new TimeAdvice());  
ServiceInterface proxy = (ServiceInterface) proxyFactory.getProxy();
```

<br>

## PointCut, Advice, Advisor
- 포인트컷 (PointCut) : 어디에 부가 기능을 적용할 지 안할 지 판단하는 필터링 로직
- 어드바이스 (Advice) : 프록시가 호출하는 부가 기능 ( 프록시 로직 )
- 어드바이저 (Advisor) : 하나의 포인트컷과 하나의 어드바이스를 가지고 있는 것
- 프록시 팩토리와 어드바이저 관계
![image](https://github.com/user-attachments/assets/31143776-4196-499a-b650-249d38090cc8)

<br>

### Advisor 사용 방법 
- `DefaultPointcutAdvisor` : `Advisor` 인터페이스의 가장 일반적인 구현체로, 생성자를 통해 하나의 포인트컷과 하나의 어드바이스를 넣어준다.
- 프록시 팩토리를 사용할 때 `Advisor` 는 필수이다.
```java
ServiceInterface target = new ServiceImpl();  
ProxyFactory proxyFactory = new ProxyFactory(target);  
DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(Pointcut.TRUE, new TimeAdvice()); // (항상 참을 반환하는 PointCut, 위에서 구현한 Advice)
proxyFactory.addAdvisor(advisor); // 프록시 팩토리에 Advisor 적용
ServiceInterface proxy = (ServiceInterface) proxyFactory.getProxy();  
  
proxy.save();  
proxy.find();
```

<br>

### 스프링이 제공하는 포인트컷
- 포인트컷을 직접 만들 수도 있지만 일반적으로 스프링이 만들어둔 구현체를 사용한다.
- `NameMatchMethodPointCut` : 메서드 이름을 기반으로 매칭한다. ( 내부에서 `PatternMatchUtils` 사용 )
- `JdkRegexpMethodPointcut` : JDK 정규 표현식을 기반으로 포인트컷을 매칭한다.
- `TruePointCut` : 항상 참을 반환한다.
- `AnnotationMatchingPointcut` : 애노테이션으로 매칭한다.
- `AspectJExpressionPointcut` : AspectJ 표현식으로 매칭한다. ( ★ )
#### NameMatchMethodPointCut 사용 예시
```java
@Test  
@DisplayName("스프링이 제공하는 포인트컷")  
void advisorTest() {  
    ServiceInterface target = new ServiceImpl();  
    ProxyFactory proxyFactory = new ProxyFactory(target);  
    NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();  
    pointcut.setMappedNames("save"); // *save* 허용
    DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(pointcut, new TimeAdvice());  
    proxyFactory.addAdvisor(advisor);  
    ServiceInterface proxy = (ServiceInterface) proxyFactory.getProxy();  
  
    proxy.save();
    proxy.find();
}
/** 실행 결과
TimeAdvice - TimeProxy 실행
ServiceImpl - save 호출
TimeAdvice - TimeProxy 종료 resultTime=0
ServiceImpl - find 호출 // TimeProxy 호출 X
**/
```

<br>

### 여러 Advisor 함께 적용하기
- 프록시를 여러개 생성하여 각 프록시에 Advisor 를 적용하는 게 아니라 **하나의 프록시에 여러 Advisor**를 적용한다. ( ProxyFactory → Advisor 는 1:N )
- 프록시 팩토리에 원하는 만큼 addAdvisor() 를 통해 `Advisor` 를 등록하면 된다.
- 등록하는 순서대로 `Advisor` 가 호출된다.
#### 여러 Advisor 등록 예시
> proxy → advisor2 → advisor1 → target

```java
@Test  
@DisplayName("하나의 프록시, 여러 어드바이저")  
void multiAdvisorTest() {  
    DefaultPointcutAdvisor advisor1 = new DefaultPointcutAdvisor(Pointcut.TRUE, new Advice1());  
    DefaultPointcutAdvisor advisor2 = new DefaultPointcutAdvisor(Pointcut.TRUE, new Advice2());  
  
    // 프록시 한 개 생성  
    ServiceInterface target = new ServiceImpl();  
    ProxyFactory proxyFactory1 = new ProxyFactory(target);  
  
    proxyFactory1.addAdvisor(advisor2); // advisor2 등록
    proxyFactory1.addAdvisor(advisor1); // advisor1 등록
    ServiceInterface proxy = (ServiceInterface) proxyFactory1.getProxy();  
  
    proxy.save(); // 실행  
}  
  
@Slf4j  
static class Advice1 implements MethodInterceptor {  
    @Override  
    public Object invoke(MethodInvocation invocation) throws Throwable {  
        log.info("advice1 호출");  
        return invocation.proceed();  
    }  
}  
@Slf4j  
static class Advice2 implements MethodInterceptor {  
    @Override  
    public Object invoke(MethodInvocation invocation) throws Throwable {  
        log.info("advice2 호출");  
        return invocation.proceed();  
    }  
}
```

<br>

### LogTrace 에 프록시 팩토리 적용하기 ( 인터페이스 )
#### 1. 부가 기능 로직을 구현한 LogTraceAdvice 생성
```java
@RequiredArgsConstructor  
public class LogTraceAdvice implements MethodInterceptor {  
  
    private final LogTrace logTrace;  
  
    @Override  
    public Object invoke(MethodInvocation invocation) throws Throwable {  
        TraceStatus status = null;  
        try {  
            Method method = invocation.getMethod();  
            String message = method.getDeclaringClass().getSimpleName() + "." + method.getName() + "()";  
            status = logTrace.begin(message);  
            Object result = invocation.proceed(); // 로직 호출  
            logTrace.end(status);  
            return result;  
        } catch (Exception e) {  
            logTrace.exception(status, e);  
            throw e;  
        }  
    }  
}
```

#### 2. 수동으로 프록시 빈 등록 ( Service 예시 )
- 프록시 팩토리에 각각의 target 과 advisor 를 등록해서 프록시를 생성하고 생성된 프록시를 스프링 빈으로 등록한다.
```java
@Slf4j  
@Configuration  
public class ProxyFactoryConfig {  
    @Bean  
    public OrderService orderService(LogTrace logTrace) {  
        OrderService orderService = new OrderServiceImpl(orderRepository(logTrace));  
        ProxyFactory factory = new ProxyFactory(orderService); // 프록시 팩토리 생성
        factory.addAdvisor(getAdvisor(logTrace)); // Advisor 적용
        OrderService proxy = (OrderService) factory.getProxy(); // 프록시 반환
        return proxy;  
    }
    ..
    private Advisor getAdvisor(LogTrace logTrace) {  
	    // pointcut, name 으로 시작하는 메서드에 true 반환
	    NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();  
	    pointcut.setMappedNames("request*", "order*", "save*");  
	    // advice  
	    LogTraceAdvice advice = new LogTraceAdvice(logTrace);  
	    return new DefaultPointcutAdvisor(pointcut, advice);  
	}
	@Bean  
	public LogTrace logTrace() {  
	    return new ThreadLocalLogTrace();  
	}
}
```

#### 3. 실행 결과
```console
[1ace96d5] OrderController.request()
[1ace96d5] |-->OrderService.orderItem()
[1ace96d5] |   |-->OrderRepository.save()
[1ace96d5] |   |<--OrderRepository.save() time=1010ms
[1ace96d5] |<--OrderService.orderItem() time=1011ms
[1ace96d5] OrderController.request() time=1012ms
```

<br>

## 문제점
- Advisor, Advice, PointCut 덕분에 어떤 부가 기능을 어디에 적용할 지 명확하게 이해할 수 있고 원본 코드를 전혀 손대지 않고 프록시를 통해 부가 기능을 적용할 수 있었다.
- ProxyFactoryConfig 에서와 같이 무수히 많은 동적 프록시 생성 코드를 만들어야 한다는 문제가 있다.
- 컴포넌트 스캔을 사용하면 스프링에서 실제 객체를 이미 컨테이너에 빈으로 등록을 다 해버린 상태이기 때문에 프록시 적용이 불가능하다.

<br>

## 참고 
[인프런 - 스프링 핵심 원리 고급편](https://inf.run/FWeFN) 