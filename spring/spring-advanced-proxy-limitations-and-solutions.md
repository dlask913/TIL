# (spring) 프록시 기술과 한계 
> 타입 캐스팅, 의존관계 주입, CGLIB, 스프링의 해결책

<br>

## JDK 동적 프록시 한계: 타입 캐스팅
- JDK 동적 프록시는 인터페이스를 기반으로 프록시를 생성하고 CGLIB 는 구체 클래스를 기반으로 프록시를 생성한다. 
- 인터페이스가 없는 경우에는 CGLIB 를 사용해야 하지만 인터페이스가 있는 경우에는 둘 중 하나를 선택할 수 있다. 
```java
proxyTargetClass=false // JDK 동적 프록시 사용
proxyTargetClass=true // CGLIB 사용
```

### JDK 동적 프록시 한계 
- JDK 동적 프록시는 인터페이스로 캐스팅이 가능하지만 구체 클래스로 캐스팅이 불가능하다. → `ClassCastException` 발생
- 인터페이스를 기반으로 프록시를 생성하기 하여, 구체 클래스가 어떤 것인지 전혀 알지 못하기 때문이다. 
```java
@Test  
void jdkProxy() {  
    MemberServiceImpl target = new MemberServiceImpl();  
    ProxyFactory proxyFactory = new ProxyFactory(target);  
    proxyFactory.setProxyTargetClass(false); // JDK 동적 프록시
  
    // 프록시를 인터페이스로 캐스팅 성공  
    MemberService memberServiceProxy = (MemberService) proxyFactory.getProxy();  
    // JDK 동적 프록시를 구현 클래스로 캐스팅 시도 실패
    assertThrows(ClassCastException.class, () -> {  
        MemberServiceImpl castingMemberService = (MemberServiceImpl) memberServiceProxy;  
    });  
}
```

### CGLIB 동적 프록시
- 구체 클래스를 기반으로 하기 때문에 구체 클래스로 캐스팅이 가능하고 구체 클래스의 부모 타입인 인터페이스도 당연히 캐스팅이 가능하다. 
```java
@Test  
void cglibProxy() {  
    MemberServiceImpl target = new MemberServiceImpl();  
    ProxyFactory proxyFactory = new ProxyFactory(target);  
    proxyFactory.setProxyTargetClass(true); // CGLIB 프록시  
  
    // 프록시를 인터페이스로 캐스팅 성공  
    MemberService memberServiceProxy = (MemberService) proxyFactory.getProxy();  
    log.info("proxy class={}", memberServiceProxy.getClass());  
  
    // CGLIB 프록시를 구현 클래스로 캐스팅 시도 성공  
    MemberServiceImpl castingMemberService = (MemberServiceImpl) memberServiceProxy;
}
```

<br>

## JDK 동적 프록시 한계: 의존관계 주입
- JDK 동적 프록시를 사용하면 구체 클래스 타입에 의존관계 주입이 불가능하다. 
- JDK 프록시는 인터페이스 기반으로 만들어지고, `MemberServiceImpl` 타입이 뭔지 전혀 모르기 때문에 주입이 불가능해진다. → `MemberServiceImpl = JDK Proxy` 성립 X
- CGLIB 를 사용하면 구체 클래스와 부모 타입인 인터페이스 모두 의존관계 주입이 가능하다.
```java
@SpringBootTest(properties = {"spring.aop.proxy-target-class=false"})
@Import(ProxyDIAspect.class)  
public class ProxyDITest {  
  
    @Autowired  
    MemberService memberService;  
  
    @Autowired  
    MemberServiceImpl memberServiceImpl; 
    /* 실행 결과
    Bean named 'memberServiceImpl' is expected to be of type 'hello.aop.member.MemberServiceImpl' but was actually of type 'jdk.proxy3.$Proxy54' */
  
    @Test  
    void go() {...}  
}
```

### CGLIB 에 대한 고민
- DI 의 장점은 클라이언트 코드 변경 없이 구현 클래스를 변경할 수 있는 것인데 이렇게 하려면 인터페이스를 기반으로 의존 관계를 주입 받아야 한다. 
- CGLIB 가 좋아보이지만 구현 클래스에 의존 관계를 주입하면 향후 구현 클래스를 변경할 때 의존 관계 주입을 받는 클라이언트 코드도 함께 변경해야 한다. 

<br>

## CGLIB 문제점
#### 1. 대상 클래스에 기본 생성자 필수 
- CGLIB 는 구체 클래스를 상속 받는데, 자바에서 상속을 받으면 자식 클래스의 생성자를 호출할 때 자식 클래스 생성자에서 부모 클래스 생성자도 호출해야 한다. ( 생략 시 super() 자동 )
- CGLIB 프록시는 대상 클래스를 상속 받고 생성자에서 대상 클래스의 기본 생성자를 호출하기 때문에 대상 클래스에 기본 생성자를 만들어야 한다. ( 생성자가 없으면 기본 생성자 자동 생성 )

#### 2. 생성자 2번 호출 문제 
- 실제 target 객체를 생성할 때와 프록시 객체를 생성할 때 부모 클래스의 생성자 호출, 총 2번이 호출된다. 
![image](https://github.com/user-attachments/assets/6771f0dc-7d13-48b7-80ba-46813a6549a5)

#### 3. final 키워드 클래스, 메서드 사용 불가
- `final` 키워드가 클래스에 있으면 상속이 불가능하고 메서드에 있으면 오버라이딩이 불가능하다. 
- CGLIB 는 상속을 기반으로 하기 때문에 두 경우 프록시가 생성되지 않거나 정상 동작하지 않는다. 
- 프레임워크 개발이 아니라 일반적인 웹 애플리케이션을 개발할 때는 `final` 키워드를 잘 사용하지 않기 때문에 특별이 문제가 되지는 않는다. 

<br>

## 스프링의 해결책
#### 스프링 3.2, CGLIB 를 스프링 내부에 함께 패키징
- CGLIB 를 사용하려면 CGLIB 라이브러리가 별도로 필요했는데 스프링 내부에 함께 패키징해서 별도 라이브러리 추가 없이 CGLIB 를 사용할 수 있게 되었다. 

#### CGLIB 기본 생성자 필수 문제 해결
 - 스프링 4.0 부터 `objensis` 라는 라이브러리를 사용해서 기본 생성자 없이 객체 생성이 가능해졌다.

#### 생성자 2번 호출 문제 
- `objensis` 라이브러리 덕분에 실제 타겟 클래스 생성 시 딱 1번만 호출된다. 

#### 스프링 부트 2.0 - CGLIB 기본 사용
- 스프링 부트 2.0 부터 CGLIB 를 기본으로 사용하도록 해서 구체 클래스 타입으로 의존 관계를 주입하는 문제가 해결되었다. 
- 별도 설정 없이 AOP 를 적용하면 기본적으로 `proxyTargetClass=true` 로 사용한다. 
- 추가 설정 없이는, 인터페이스가 있어도 항상 CGLIB 를 사용해서 구체 클래스 기반 프록시를 생성한다.

<br>

## 참고 
[인프런 - 스프링 핵심 원리 고급편](https://inf.run/FWeFN) 