# (spring) 스프링이 제공하는 빈 후처리기
> 스프링이 제공하는 빈 후처리기, LogTrace 에 스프링이 제공하는 자동 프록시 생성기 적용하기

<br>

## 스프링이 제공하는 빈 후처리기

### 라이브러리 추가
```gradle
implementation 'org.springframework.boot:spring-boot-starter-aop'
```
- 스프링 부트가 AOP 관련 클래스를 자동으로 스프링 빈에 등록한다.
- 위 자동 설정으로 `AnnotationAwareAspectJAutoProxyCreator` 라는 빈 후처리기가 스프링 빈에 자동으로 등록된다. 
- 위 빈 후처리기는 스프링 빈으로 등록된 `Advisor` 들을 자동으로 찾아서 프록시가 필요한 곳에 자동으로 프록시를 적용해준다.
- `Advisor` 안에는 `Pointcut` 과 `Advice` 가 모두 포함되어있기 때문에 `Advisor` 만 알고 있으면 그 안에 있는 `Pointcut` 으로 어떤 스프링 빈에 프록시를 적용해야 할지 알 수 있다.
- `Advisor` 뿐 아니라 `@Aspect` 도 자동으로 인식해서 프록시를 만들고 AOP 를 적용해준다.

<br>

### 동작 과정

![image](https://github.com/user-attachments/assets/4f303b55-6f3c-4e40-9216-d2821d92c0b8)

1. 생성 후 전달 : 스프링 빈 대상이 되는 객체를 생성하여 빈 후처리기에 전달 ( `@Bean`, 컴포넌트 스캔 모두 포함 ) 
2. 모든 Advisor 빈 조회 : 자동 프록시 생성기 - 빈 후처리기는 스프링 컨테이너에서 모든 Advisor 를 조회
3. 프록시 적용 대상 체크 : Advisor 에 포함되어 있는 포인트컷을 사용해서 객체 클래스 정보 및 모든 메서드를 포인트컷에 매칭하여 조건이 하나라도 만족하면 프록시 적용 대상임을 판단
4. 프록시 생성 : 프록시 적용 대상이면 프록시를 생성하고 반환해서 프록시를 스프링 빈으로 등록
5. 빈 등록 : 반환된 객체는 스프링 빈으로 등록

<br>

### LogTrace 에 자동 프록시 생성기 적용하기

#### Advisor 빈 등록하기
- 빈 후처리기는 등록하지 않아도 자동 프록시 생성기가 자동으로 등록해준다.
```java
@Configuration  
public class AutoProxyConfig {  
    @Bean  
    public Advisor advisor(LogTrace logTrace) {  
        // pointcut  
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

#### AssertJExpressionPointcut 사용하기
- AOP 에 특화된 AssertJ 포인트컷 표현식을 적용하여 복잡한 포인트컷을 만들 수 있다.
```java
    @Bean  
    public Advisor advisor(LogTrace logTrace) {  
        //pointcut  
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();  
        
        // package.name 패키지와 그 하위 패키지의 모든 메서드를 대상으로 한다.
        // pointcut.setExpression("execution(* package.name..*(..))");  

        // && 와 ! 연산자를 사용하여 noLog() 메서드는 대상에서 제외할 수 있다.
        pointcut.setExpression("execution(* package.name..*(..)) && !execution(* hello.proxy.app..noLog(..))");
        
        //advice  
        LogTraceAdvice advice = new LogTraceAdvice(logTrace);  
        return new DefaultPointcutAdvisor(pointcut, advice);  
    }
```

<br>

## 하나의 프록시, 여러 Advisor 적용
- 프록시 팩토리가 생성하는 프록시는 내부에 여러 advisor 들을 포함할 수 있기 때문에 프록시 자동 생성기는 프록시를 하나만 생성한다. 
#### 프록시 자동 생성기 상황별 정리
- advisor1 의 포인트컷만 만족 → 프록시 1개 생성, 프록시에 advisor1 만 포함
- advisor1, advisor2 의 포인트컷 모두 만족 → 프록시 1개 생성, 프록시에 advisor1 과 advisor2 모두 포함
- advisor1, advisor2 의 포인트컷 모두 만족하지 않음 → 프록시가 생성되지 않음

<br>

## 정리
- 포인트컷 조건에 맞는 것이 하나라도 있으면 프록시를 생성한다.
- 프록시가 호출되었을 때 부가 기능인 어드바이스를 적용할지 말지는 포인트컷을 보고 판단한다.
- 프록시를 모든 곳에 생성하는 것은 비용 낭비이기 때문에 꼭 필요한 곳에 최소한의 프록시를 적용하도록 한다.

<br>

## 참고 
[인프런 - 스프링 핵심 원리 고급편](https://inf.run/FWeFN) 