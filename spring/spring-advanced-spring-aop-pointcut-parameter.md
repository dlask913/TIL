# (spring) 스프링 AOP 포인트컷 파라미터 전달
> this, target, args, @target, @within, @annotation, @args 파라미터 전달 예제 및 this 와 target 차이점

<br>

## 파라미터 전달
- this, target, args, @target, @within, @annotation, @args 표현식을 사용하여 어드바이스에 매개변수를 전달할 수 있다. 
- 적용 대상
```java
@ClassAop  
@Component  
public class MemberServiceImpl implements MemberService {  
    @Override  
    @MethodAop("test value")  
    public String hello(String param) {  
        return "ok";  
    }  
  
    public String internal(String param) {  
        return "ok";  
    }  
}
```

<br>

### args 예제
- 포인트컷의 이름과 매개변수의 이름을 맞추어야 한다. ( ex> arg )
- 타입이 메서드에 지정한 타입으로 제한된다.
```java
@Slf4j  
@Aspect  
static class ParameterAspect {  
  
    @Pointcut("execution(* hello.aop.member..*.*(..))")  
    private void allMember() {  
    }  
  
    @Around("allMember()") // joinpoint 에서 꺼내오기
    public Object logArgs1(ProceedingJoinPoint joinPoint) throws Throwable {  
        Object arg1 = joinPoint.getArgs()[0]; 
        log.info("[logArgs1]{}, arg={}", joinPoint.getSignature(), arg1);  
        return joinPoint.proceed();  
    }
    @Around("allMember() && args(arg,..)") // 매개변수로 받기
	public Object logArgs2(ProceedingJoinPoint joinPoint, Object arg) throws Throwable {  
	    log.info("[logArgs2]{}, arg={}", joinPoint.getSignature(), arg);  
	    return joinPoint.proceed();  
	}  
	  
	@Before("allMember() && args(arg,..)") // joinpoint 없이 사용 (가장 축약)
	public void logArgs3(String arg) { // String 으로 타입 제한
	    log.info("[logArgs3] arg={}", arg);  
	}
}
```

<br>

### this 와 target 예제 
- this 는 프록시 객체를 전달 받고 target 은 실제 대상 객체를 전달 받는다.
```java
@Before("allMember() && this(obj)")  
public void thisArgs(JoinPoint joinPoint, MemberService obj) {  
    log.info("[this]{}, obj={}", joinPoint.getSignature(), obj.getClass());  
}

@Before("allMember() && target(obj)")  
public void targetArgs(JoinPoint joinPoint, MemberService obj) {  
    log.info("[target]{}, obj={}", joinPoint.getSignature(), obj.getClass());  
}

/** 실행 결과
[target]String hello.aop.member.MemberServiceImpl.hello(String), obj=class hello.aop.member.MemberServiceImpl
[this]String hello.aop.member.MemberServiceImpl.hello(String), obj=class hello.aop.member.MemberServiceImpl$$EnhancerBySpringCGLIB$$f63f6edf
**/
```

<br>

### @target 과 @within 예제
- 타입의 애노테이션을 전달 받는다. 
```java
@Before("allMember() && @target(annotation)")  
public void atTarget(JoinPoint joinPoint, ClassAop annotation) {  
    log.info("[@target]{}, obj={}", joinPoint.getSignature(), annotation);  
}  
  
@Before("allMember() && @within(annotation)")  
public void atWithin(JoinPoint joinPoint, ClassAop annotation) {  
    log.info("[@within]{}, obj={}", joinPoint.getSignature(), annotation);  
}

/** 실행 결과
[@target]String hello.aop.member.MemberServiceImpl.hello(String), obj=@hello.aop.member.annotation.ClassAop()
[@within]String hello.aop.member.MemberServiceImpl.hello(String), obj=@hello.aop.member.annotation.ClassAop()
**/
```

<br>

### @annotation 예제
- 메서드의 애노테이션을 전달 받는다. 
- `annotation.value()` 를 통해 해당 애노테이션의 값을 전달 받을 수 있다. 
```java
@Before("allMember() && @annotation(annotation)")  
public void atAnnotation(JoinPoint joinPoint, MethodAop annotation) {  
    log.info("[@annotation]{}, annotationValue={}", joinPoint.getSignature(), annotation.value());
}

/** 실행 결과
[@annotation]String hello.aop.member.MemberServiceImpl.hello(String), annotationValue=test value
**/
```

<br>

### this 와 target 차이점
- this 는 프록시 객체를 target 은 실제 구현체(target 객체)를 대상으로 포인트컷을 매칭한다.
#### JDK 동적 프록시가 적용된 경우
- 인터페이스( MemberService )를 지정하면 this 와 target 모두 부모 타입을 허용하기 때문에 AOP 가 적용된다.
- 구현체( MemberServiceImpl )을 지정하면 JDK 동적 프록시로 만들어진 proxy 객체는 실제 구현체와는 다른 새로운 클래스이기 때문에 this 의 AOP 적용 대상이 아니다.
- target 객체가 MemberServiceImpl 타입이므로 target 의 AOP 적용 대상이다.
#### CGLIB 프록시 적용된 경우
- 인터페이스( MemberService )를 지정하면 this 와 target 모두 부모 타입을 허용하기 때문에 AOP 가 적용된다.
- CGLIB 로 만들어진 proxy 객체는 구현체 ( MemberServiceImpl ) 를 상속 받아 만들었기 때문에 부모 타입을 허용하는 this 의 AOP 적용 대상이 된다. 
- target 객체가 MemberServiceImpl 타입이므로 target 의 AOP 적용 대상이다.

<br>

## 참고 
[인프런 - 스프링 핵심 원리 고급편](https://inf.run/FWeFN) 