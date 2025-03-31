# (spring) JDK 동적 프록시와 CGLIB
> 프록시 수가 많다는 문제점 해결하기, 리플렉션, JDK 동적 프록시와 CGLIB

<br>

### 프록시 수가 많다는 문제점 해결하기
> - 인터페이스 및 구체 프록시를 로그 추적기에 적용했을 때, 대상 클래스 수만큼 프록시를 생성해야 한다는 문제점이 있다.
> - 프록시를 하나만 만들어서 적용할 수 있는 동적 프록시 기술을 사용해서 해결한다.

<br>

## 리플렉션
- 클래스나 메서드의 메타정보를 동적으로 획득하고 실행할 수도 있다. 
- 메타정보를 활용하여 동적으로 호출할 메서드를 변경할 수 있다.

### 리플렉션 예제 및 주의사항 
- Method 메타정보를 통해서 호출할 메서드 정보를 동적으로 제공한다. ( `callA`, `callB` )
- 메서드 정보를 잘못 기입하더라도 리플렉션은 런타임에 동작하기 때문에 컴파일 시점에 오류를 잡을 수 없다. 
```java
@Test  
void reflection() throws Exception {  
    //클래스 정보  
    Class classHello = Class.forName("hello.proxy.jdkdynamic.ReflectionTest$Hello");  
  
    Hello target = new Hello(); // 실제 실행할 인스턴스
    Method methodCallA = classHello.getMethod("callA");  
    dynamicCall(methodCallA, target);  
  
    Method methodCallB = classHello.getMethod("callB");  
    dynamicCall(methodCallB, target);  
}  
  
private void dynamicCall(Method method, Object target) throws Exception {  
    log.info("start");  
    Object result = method.invoke(target); // 획득한 메서드 메타정보로 실제 인스턴스 메서드 호출
    log.info("result={}", result);  
}
```

<br>

## JDK 동적 프록시
- 직접 프록시 클래스를 만들지 않아도 프록시 객체가 동적으로 런타임에 만들어진다. 
- JDK 동적 프록시는 인터페이스를 기반으로 프록시를 동적으로 만들어준다. ( 인터페이스 필수 )

### JDK 동적 프록시 적용
#### 1. LogTrace 를 적용할 수 있는 InvocationHandler 생성
```java
public class LogTraceBasicHandler implements InvocationHandler {  
  
    private final Object target;  
    private final LogTrace logTrace;  
  
    public LogTraceBasicHandler(Object target, LogTrace logTrace) {  
        this.target = target;  
        this.logTrace = logTrace;  
    }  
  
    @Override  
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {  
  
        TraceStatus status = null;  
        try {  
            String message = method.getDeclaringClass().getSimpleName() + "." + method.getName() + "()";
            status = logTrace.begin(message);  
            Object result = method.invoke(target, args); // 로직 호출
            logTrace.end(status);  
            return result;  
        } catch (Exception e) {  
            logTrace.exception(status, e);  
            throw e;  
        }  
    }  
}
```

#### 2. 수동으로 동적 프록시 빈 등록 ( Service 예시 )
- JDK 동적 프록시를 사용해 각 Controller, Service, Repository 에 맞는 동적 프록시를 생성한다. 
```java
@Configuration  
public class DynamicProxyBasicConfig {  
    
    @Bean  
    public OrderService orderService(LogTrace logTrace) {  
        OrderService orderService = new OrderServiceImpl(orderRepository(logTrace));  
        OrderService proxy = (OrderService) Proxy.newProxyInstance(OrderService.class.getClassLoader(),  
                new Class[]{OrderService.class},  
                new LogTraceBasicHandler(orderService, logTrace));  
        return proxy;
    }
    ..
	@Bean  
	public LogTrace logTrace() { // 로그 추적기 
	    return new ThreadLocalLogTrace();  
	}
}
```

#### 3. 메서드 이름 필터 기능 추가
- 필터 기능을 적용하여 특정 메서드 이름이 매칭 되는 경우에만 로직을 실행할 수 있다. 
```java
@RequiredArgsConstructor  
public class LogTraceFilterHandler implements InvocationHandler {  
  
    private final Object target;  
    private final LogTrace logTrace;  
    private final String[] patterns; // 메서드 이름이 이 패턴일 때만 로그 남도록
  
    @Override  
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {  
  
        String methodName = method.getName(); // 메서드 이름  
        if (!PatternMatchUtils.simpleMatch(patterns, methodName)) { // 해당 메서드 이름이 패턴에 맞지 않다면 ( request*, save, .. )            
	        return method.invoke(target, args); // 메서드 호출  
        }
        .. // LogTraceBasicHandler 로직과 동일
	}
}

@Configuration  
public class DynamicProxyFilterConfig {  
  
    private static final String[] PATTERNS = {"request*", "order*", "save*"}; // 로그 남길 패턴 정의  
  
    @Bean  
    public OrderServiceV1 orderServiceV1(LogTrace logTrace) {  
        OrderServiceV1 orderServiceV1 = new OrderServiceV1Impl(orderRepositoryV1(logTrace));  
        OrderServiceV1 proxy = (OrderServiceV1) Proxy.newProxyInstance(OrderServiceV1.class.getClassLoader(),  
                new Class[]{OrderServiceV1.class},  
                new LogTraceFilterHandler(orderServiceV1, logTrace, PATTERNS));  
        return proxy;  
    }
    ..
```

#### 동작 순서

> Client - request() → OrderControllerV1 $proxy1 
>       - handler.invoke() → logTraceBasicHandler 
>       - mothod.invoke(request()) → orderControllerV1Impl 
>       - orderItem(..) → OrderServiceV1 $proxy2 
>       - handler.invoke() → logTraceBasicHandler 
>       - method.invoke(orderItem(..)) → orderServiceV1Impl

<br>

## CGLIB
- 바이트코드를 조작해서 동적으로 클래스를 생성하는 기술을 제공하는 라이브러리이다.
- 인터페이스가 없어도 구체 클래스만 가지고 동적 프록시를 만들어낼 수 있다. 
- 원래는 외부 라이브러리인데 스프링 프레임워크가 내부 소스 코드에 포함했기 때문에 별도의 외부 라이브러리를 추가하지 않아도 사용할 수 있다. 
- `InvocationHandler` 대신에 `MethodInterceptor` 를 구현하여 동적 프록시를 실행한다.
```java
@Slf4j  
@RequiredArgsConstructor  
public class TimeMethodInterceptor implements MethodInterceptor {  
  
    private final Object target;  
  
    @Override // (CGLIB가 적용된 객체, 호출된 메서드, 전달된 인수, 메서드 호출에 사용할 proxy)    
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {  
        log.info("TimeProxy 실행");  
        long startTime = System.currentTimeMillis();  
  
        Object result = methodProxy.invoke(target, args); // 로직 실행  
  
        long endTime = System.currentTimeMillis();  
        long resultTime = endTime - startTime;  
        log.info("TimeProxy 종료 resultTime={}", resultTime);  
        return result;  
    }  
}
```

<br>

## 문제점
- 인터페이스가 있는 경우와 그렇지 않은 경우 `InvocationHandler` 와 `MethodInterceptor` 를 중복으로 사용해야 하는 지 고민이 필요하다.

<br>

## 참고 
[인프런 - 스프링 핵심 원리 고급편](https://inf.run/FWeFN) 