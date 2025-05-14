# (spring) 싱글톤 컨테이너
> 싱글톤 패턴, 싱글톤 컨테이너, @Configuration 과 바이트코드 조작

<br>

## 싱글톤 패턴
- 웹 애플리케이션에 많은 요청이 동시에 들어오면 모든 요청에 대한 객체가 생성되고 소멸되기 때문에 메모리 낭비가 심해진다. 
- 싱글톤 패턴은 클래스의 인스턴스가 딱 1개만 생성되는 것을 보장하는 디자인 패턴이다.

### 싱글톤 패턴 적용 예제 
```java
public class SingletonService {
	// 1. static 영역에 객체를 딱 1개만 생성해둔다.
	private static final SingletonService instance = new SingletonService(); 
	
	// 2. 객체 인스턴스가 필요하면 이 static 메서드를 통해서만 조회하도록 허용한다.
	public static SingletonService getInstance() {
		return instance;
	}
	
	// 3. 생성자를 private으로 선언해서 외부에서 new 를 사용한 객체 생성을 못하게 막는다. (★)
	private SingletonService() { }
	
	public void logic() {
		System.out.println("싱글톤 객체 로직 호출");     
	}
}
```

- 실행 결과 : 호출할 때 마다 같은 객체를 반환하는 것을 알 수 있다. 
```java
SingletonService singletonService1 = SingletonService.getInstance();
SingletonService singletonService2 = SingletonService.getInstance();

System.out.println("singletonService1 = " + singletonService1); System.out.println("singletonService2 = " + singletonService2);
```

### 싱글톤 패턴 문제점
- 싱글톤 패턴을 구현하는 코드가 많이 들어간다. 
- 의존관계상 클라이언트카 구체 클래스에 의존( DIP 위반 )하기 때문에 OCP 원칙을 위반할 가능성이 높다.
- 테스트하기 어렵다.
- 내부 속성을 변경하거나 초기화하기 어렵다. 
- private 생성자로 자식 클래스를 만들기 어렵다.
- 결론적으로 유연성이 떨어지며 안티패턴으로 불리기도 한다. 

<br>

## 싱글톤 컨테이너
- 스프링 컨테이너는 싱글턴 패턴을 적용하지 않아도 객체 인스턴스를 싱글톤으로 관리한다. 
- 스프링 컨테이너는 싱글톤 컨테이너 역할을 하고, 이렇게 싱글톤 객체를 생성하고 관리하는 기능을 싱글톤 레지스트리라고 한다. 

### 싱글톤 컨테이너 사용 예제 
- `memberService` 를 조회할 때마다 같은 객체가 반환되는 것을 알 수 있다. 
- 덕분에 이미 만들어진 객체를 공유해서 효율적으로 재사용할 수 있다. 
- 스프링의 기본 빈 등록 방식은 싱글톤이지만 요청할 때마다 새로운 객체를 생성해서 반환하는 기능도 제공한다. 
```java
@Test
@DisplayName("스프링 컨테이너와 싱글톤")
void springContainer() {
	ApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
	MemberService memberService1 = ac.getBean("memberService", MemberService.class);
	MemberService memberService2 = ac.getBean("memberService", MemberService.class);
	//참조값이 같은 것을 확인
	assertThat(memberService1).isSameAs(memberService2);
}
```

### 싱글톤 방식의 주의점
- 싱글톤 방식은 여러 클라이언트가 하나의 같은 객체 인스턴스를 공유하기 때문에 싱글톤 객체는 무상태(stateless) 로 설계해야 한다. 
- 특정 클라이언트에 의존적인 필드가 있으면 안된다. 
- 특정 클라이언트가 값을 변경할 수 있는 필드가 있으면 안되고 가급적 읽기만 가능해야 한다. 
- 필드 대신에 자바에서 공유되지 않는 지역변수, 파라미터, ThreadLocal 등을 사용해야 한다.
- 아래 예시와 같이 order(..) 을 수행할 때마다 값을 저장하면 특정 클라이언트의 값이 마지막 클라이언트가 요청한 값으로 바뀌는 등 큰 문제가 발생할 수 있다. 
```java
public class StatefulService {
	private int price; // 상태를 유지하는 필드
	public void order(String name, int price) {
		this.price = price; // 여기가 문제!
	}
	..
}
```

<br>

## @Configuration 과 바이트코드 조작
- 아래와 같은 AppConfig 설정 정보가 있을 때 `MemoryMemberRepository` 가 `new` 로 2 개의 객체가 생성되며 싱글톤이 깨지는 것처럼 보이는데 실제로는 그렇지 않다.
```java
@Configuration
public class AppConfig {
    @Bean
	public MemberService memberService() {
		return new MemberServiceImpl(memberRepository());
	}
	@Bean
	public OrderService orderService() {
		return new OrderServiceImpl(memberRepository());
	}
	@Bean
	public MemberRepository memberRepository() {
		return new MemoryMemberRepository();
    }
}
```

### @Configuration 동작 방식
1. `memoryMemberRepository` 가 이미 스프링 컨테이너에 등록되어 있으면 스프링 컨테이너에서 찾아서 반환한다. 
2. 스프링 컨테이너에 없으면 기존 로직을 호출해서 `memoryMemberRepository` 를 생성하고 스프링 컨테이너에 등록하고 반환한다.
- 즉, `@Bean` 이 붙은 메서드마다 스프링 빈이 존재하면 반환하고 없으면 생성해서 스프링 빈으로 등록하고 반환하는 코드가 동적으로 만들어진다. ( 싱글톤 보장 )
-  `@Configuration` 없이 `@Bean` 만 사용하면 스프링 빈으로 등록은 되지만 싱글톤을 보장하지 않는다. → **스프링 설정 정보에 꼭 @Configuration 사용**

<br>

## 참고 
[인프런 - 스프링 핵심 원리 - 기본편](https://inf.run/kj9JQ)