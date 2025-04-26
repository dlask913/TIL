# (spring) 스프링 AOP 포인트컷
> 포인트컷 지시자, execution, within, args, target, @target, @within, @annotation, bean

<br>

## 포인트컷 지시자
- 포인트컷 표현식은 AspectJ pointcut execution, 즉 애스펙트J 가 제공하는 포인트컷 표현식을 줄여서 말하는 것이다. 
- 포인트컷 표현식은 execution 같은 포인트컷 지시자 (Pointcut Designator) 로 시작한다. = PCD

### 포인트컷 지시자 종류 
- `execution` : 메서드 실행 조인 포인트를 매칭한다. 스프링 AOP 에서 가장 많이 사용하고 기능도 복잡하다. 
- `within` : 특정 타입 내 조인 포인트를 매칭한다. 
- `args` : 인자가 주어진 타입의 인스턴스인 조인 포인트이다.
- `this` : 스프링 빈 객체 (스프링 AOP 프록시) 를 대상으로 하는 조인 포인트이다. 
- `target` : Target 객체 (스프링 AOP 프록시가 가르키는 실제 대상)를 대상으로 하는 조인 포인트이다. 
- `@target` : 실행 객체의 클래스에 주어진 타입의 애노테이션이 있는 조인 포인트이다. 
- `@within` : 주어진 애노테이션이 있는 타입 내 조인 포인트이다. 
- `@annotation` : 메서드가 주어진 애노테이션을 가지고 있는 조인 포인트를 매칭한다. 
- `@args` : 전달된 실제 인수의 런타임 타입이 주어진 타입의 애노테이션을 갖는 조인 포인트이다. 
- `bean` : 스프링 전용 포인트컷 지시자로, 빈의 이름으로 포인트컷을 지정한다. 

<br>

### AOP 예제 생성

#### 1. ClassAop 와 MethodAop 생성
- `@Target(ElementType.TYPE)` : Class 레벨에서 AOP 를 적용하기 위한 커스텀 애노테이션으로, 클래스 내 모든 public 메서드에 대해 AOP 적용이 가능하도록 포인트컷을 지정할 수 있다. 
- `@Target(ElementType.METHOD)` : Method 레벨에서 AOP 를 적용하기 위한 커스텀 애노테이션으로, 특정 메서드에먼 AOP 로직을 적용하고 싶을 때 사용한다. 
```java
@Target(ElementType.TYPE)  
@Retention(RetentionPolicy.RUNTIME)  
public @interface ClassAop {  
}

@Target(ElementType.METHOD)  
@Retention(RetentionPolicy.RUNTIME)  
public @interface MethodAop {  
    String value(); // 값을 지정하여 로직에 활용 가능
}
```

#### 2. AOP 적용
- 위에서 생성한 `@ClassAop` 와 `@MethodAop` 를 비즈니스 로직에 적용한다.
```java
public interface MemberService {  
    String hello(String param);  
}

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

### Execution 
#### execution 문법
```
execution(접근제어자? 반환타입 선언타입?메서드이름(파라미터) 예외?)
```
- 메서드 실행 조인 포인트를 매칭하고, `?` 는 생략 가능하다.
- `*` 같은 패턴을 지정할 수 있다. 

#### 정확하게 모든 내용이 매칭되는 표현식 지정
- `AspectJExpressionPointcut` 에 포인트컷 표현식을 지정하면 포인트컷 표현식을 처리해준다.
```java
@Slf4j  
public class ExecutionTest {
    AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();  
    Method helloMethod;  
  
    @BeforeEach  
    public void init() throws NoSuchMethodException {  
        helloMethod = MemberServiceImpl.class.getMethod("hello", String.class);
    }  
  
    @Test  
    void exactMatch() { 
        pointcut.setExpression("execution(public String hello.aop.member.MemberServiceImpl.hello(String))"); // 정확하게 매칭
        assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
    }
}
```

#### 생략한 포인트컷 지정
- 파라미터에서 `..` 은 파라미터의 타입과 파라미터 수가 상관없다는 것을 의미한다.
- 패키지에서 `.` 은 정확하게 해당 위치의 패키지, `..` 은 해당 위치의 패키지와 그 하위 패키지도 포함한다.
```java
@Test  
void allMatch() {  
    // 가장 많이 생략한 포인트컷
    pointcut.setExpression("execution(* *(..))"); 
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
}

@Test  
void nameMatchStar() {  
    // 메서드 이름 매칭 관련 포인트컷
    pointcut.setExpression("execution(* *el*(..))");
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
}

@Test  
void packageExactMatch() { 
    // 패키치 이름 매칭 관련 포인트컷
    // heelo.aop.member.*(1).*(2) - (1): 타입, (2): 메서드 이름
    pointcut.setExpression("execution(* hello.aop.member.*.*(..))");  
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
}
```

<br>

### within
- 특정 타입 내의 조인 포인트들로 매칭을 제한한다. 
- 해당 타입이 매칭되면 그 안의 메서드(조인 포인트)들이 자동으로 매칭되고, `execution` 에서 타입 부분만 사용한다고 보면 된다. 
- `execution` 으로 대부분 해결이 되기 때문에 잘 사용하지는 않는다. 
- `execution` 과의 차이점은 부모 타입을 지정할 수 없다는 것이다. 
- 정확하게 타입이 맞아야 하며 인터페이스를 지정할 수 없다. ( `execution` 은 가능 )
```java
@Test  
void withinExact() {  
    pointcut.setExpression("within(hello.aop.member.MemberServiceImpl)");  
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
}  
  
@Test  
void withinStar() {  
    pointcut.setExpression("within(hello.aop.member.*Service*)");  
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
}  
  
@Test  
void withinSubPackage() {  
    pointcut.setExpression("within(hello.aop..*)");  
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isTrue();
}  
  
@Test  
@DisplayName("타켓의 타입에만 직접 적용, 인터페이스를 선정하면 안된다.")  
void withinSuperTypeFalse() {  
    pointcut.setExpression("within(hello.aop.member.MemberService)");  
    assertThat(pointcut.matches(helloMethod, MemberServiceImpl.class)).isFalse();
}
```

<br>

### args
- 기본 문법은 `execution` 의 `args` 부분과 같다. 
- `execution` 은 파라미터 타입이 정확하게 매칭되어야 하는데 `args` 는 부모 타입을 허용한다. 
- `execution` 은 클래스에 선언된 정보를 기반으로 판단하고 ( 정적 ) `args` 는 실제 넘어온 파라미 객체 인스턴스를 보고 판단한다. ( 동적 )
- 단독으로 사용되기 보다는 파라미터 바인딩에서 주로 사용된다. 
```java
@Test  
void args() {  
    //hello(String)과 매칭. String 은 Object, java.io.Serializable 의 하위 타입
    assertThat(pointcut("args(String)")  
            .matches(helloMethod, MemberServiceImpl.class)).isTrue();
    assertThat(pointcut("args(Object)")  
            .matches(helloMethod, MemberServiceImpl.class)).isTrue();  
    assertThat(pointcut("args()")  
            .matches(helloMethod, MemberServiceImpl.class)).isFalse();  
    assertThat(pointcut("args(..)")  
            .matches(helloMethod, MemberServiceImpl.class)).isTrue();  
    assertThat(pointcut("args(*)")  
            .matches(helloMethod, MemberServiceImpl.class)).isTrue();  
    assertThat(pointcut("args(String,..)")  
            .matches(helloMethod, MemberServiceImpl.class)).isTrue();

	assertThat(pointcut("execution(* *(java.io.Serializable))") // 매칭 실패  
        .matches(helloMethod, MemberServiceImpl.class)).isFalse();  
	assertThat(pointcut("execution(* *(Object))") // 매칭 실패  
        .matches(helloMethod, MemberServiceImpl.class)).isFalse();
}
```

<br>

### @target 과 @within
- `@target` 과 `@within` 은 타입에 있는 애노테이션으로 AOP 적용 여부를 판단한다. ( ex> `@ClassAop` )
- `@target` 은 부모 클래스의 메서드까지 어드바이스를 다 적용하고 `@within` 은 자기 자신의 클래스에 정의된 메서드에만 어드바이스를 적용한다. 

#### 1. 부모 클래스와 자식 클래스 예제 생성
- 별도 bean 으로 등록하는 과정 필요
```java
static class Parent {  
    public void parentMethod(){} //부모에만 있는 메서드  
}  
  
@ClassAop  
static class Child extends Parent {  
    public void childMethod(){}  
}
```

#### 2. @target 과 @within 적용하기
- `@target` : 인스턴스 기준으로 모든 메서드의 조인 포인트 선정 ( 부모 타입의 메서드도 적용 )
- `@within` : 선택된 클래스 내부에 있는 메서드만 조인 포인트 선정 ( 부모 타입 메서드 적용 X )
- 별도 bean 으로 등록하는 과정 필요 
```java
@Slf4j  
@Aspect  
static class AtTargetAtWithinAspect {  
    @Around("execution(* hello.aop..*(..)) && @target(hello.aop.member.annotation.ClassAop)")
    public Object atTarget(ProceedingJoinPoint joinPoint) throws Throwable {  
        log.info("[@target] {}", joinPoint.getSignature());  
        return joinPoint.proceed();  
    }  
  
    @Around("execution(* hello.aop..*(..)) && @within(hello.aop.member.annotation.ClassAop)")
    public Object atWithin(ProceedingJoinPoint joinPoint) throws Throwable {  
        log.info("[@within] {}", joinPoint.getSignature());  
        return joinPoint.proceed();  
    }  
}
```

#### 3. 실행결과 
- `parentMethod()` 는 부모 클래스에만 정의되어 있기 때문에 `@within` 에서 AOP 적용 대상이 되지 않는 것을 확인할 수 있다. 
```java
public class AtTargetAtWithinTest {  
    @Autowired  
    Child child;  
  
    @Test  
    void success() {  
        child.childMethod(); // 부모, 자식 모두 있는 메서드  
        child.parentMethod(); // 부모 클래스만 있는 메서드  
    }
}
/** 실행 결과
[@target] void hello.aop.pointcut.AtTargetAtWithinTest$Child.childMethod()
[@within] void hello.aop.pointcut.AtTargetAtWithinTest$Child.childMethod()
[@target] void hello.aop.pointcut.AtTargetAtWithinTest$Parent.parentMethod()
**/
```

<br>

### @annotation
- @annotation 은 메서드가 주어진 애노테이션을 가지고 있는 조인포인트를 매칭한다. ( ex> `@MethodAop` )
```java
@Slf4j  
@Aspect  
static class AtAnnotationAspect {  
    @Around("@annotation(hello.aop.member.annotation.MethodAop)")  
    public Object doAtAnnotation(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("[@annotation] {}", joinPoint.getSignature());  
        return joinPoint.proceed();  
    }
}
```

<br>

### bean
- 스프링 빈의 이름으로 AOP 적용 여부를 지정하고 스프링에서만 사용할 수 있는 특별한 지시자이다.
- `*` 과 같은 패턴을 사용할 수 있다. 
```java
@Slf4j  
@Import(BeanTest.BeanAspect.class)  
@SpringBootTest
public class BeanTest {  
    @Autowired  
    OrderService orderService;  
    @Test  
    void success() {  
        orderService.orderItem("itemA");  
    }  
  
    @Aspect  
    static class BeanAspect {  
        @Around("bean(orderService) || bean(*Repository)")  
        public Object doLog(ProceedingJoinPoint joinPoint) throws Throwable {  
            log.info("[bean] {}", joinPoint.getSignature());  
            return joinPoint.proceed();  
        }  
    }  
}
```

<br>

### args, @args, @target 주의 사항
- 위 포인트컷 지시자는 **단독으로 사용하면 안된다**.
- `args`, `@args`, `@target` 은 실제 객체 인스턴스가 생성되고 실행될 때 어드바이스 적용 여부를 확인할 수 있는데 이 때 프록시가 없다면 판단 자체가 불가능하다.
- 스프링 컨테이너가 프록시를 생성하는 시점은 스프링 컨테이너가 만들어지는 애플리케이션 로딩 시점에 적용할 수 있는데 위와 같은 포인트컷 지시자가 있으면 모든 스프링 빈에 AOP 를 적용하려고 시도한다. 
- 문제는 모든 스프링 빈에 AOP 프록시를 적용하려고 하면 스프링이 내부에서 사용하는 빈 중 `final` 로 지정된 빈들도 있기 때문에 오류가 발생한다. 
- 따라서 이러한 표현식은 최대한 프록시 적용 대상을 축소하는 표현식과 함께 사용해야 한다. 

<br>

## 참고 
[인프런 - 스프링 핵심 원리 고급편](https://inf.run/FWeFN) 