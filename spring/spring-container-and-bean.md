# (spring) 스프링 컨테이너와 스프링 빈
> 스프링 컨테이너, 스프링 빈 조회, BeanFactory 와 ApplicationContext

<br>

## 스프링 컨테이너
```java
//스프링 컨테이너 생성
ApplicationContext applicationContext =
new AnnotationConfigApplicationContext(xxxConfig.class);
```
- 스프링 컨테이너를 부를 때 `BeanFactory`, `ApplicationContext` 로 구분해서 이야기하는데 `BeanFactory` 를 직접 사용하는 경우가 거의 없어 `ApplicationContext` 를 스프링 컨테이너라고 한다. 
- Config 클래스를 파라미터로 전달하여 이를 기반으로 컨테이너는 빈 저장소에 빈을 등록한다. 
- 설정 정보를 참고하여 의존관계 주입을 한다.

<br>

### 스프링 컨테이너 생성 과정
#### 1. 스프링 컨테이너 생성
- `new AnnotationConfigApplicationContext(..)` 를 통해 생성하고, 생성할 때는 구성 정보를 지정해주어야 한다. 
#### 2. 스프링 빈 등록
- 스프링 컨테이너는 파라미터로 넘어온 아래와 같은 설정 정보를 통해 스프링 빈 저장소에 빈을 등록한다. 
- 빈 이름은 메서드 이름을 사용하고, 직접 부여할 수도 있다. ( `@Bean(name="memberRepository2")` )
- 빈 이름을 동일하게 하면 다른 빈이 무시되거나 기존 빈을 덮어버리거나 설정에 따라 오류가 발생할 수 있기 때문에 **항상 다른 이름을 부여**해야 한다.
```java
@Bean
public MemberRepository memberRepository() {
	return new MemberRepository();
}
```
#### 3&4. 스프링 빈 의존관계 설정 ( 준비 → 완료 )
- 스프링 빈을 생성하고 의존 관계를 주입하는 단계가 나누어져 있는데 자바로 스프링 빈을 등록하면 생성자를 호출하면서 의존관계 주입도 한 번에 처리된다. 
- 스프링 컨테이너는 설정 정보를 참고해서 의존관계를 주입한다. ( DI )

<br>

## 스프링 빈 조회
### 동일한 타입이 둘 이상인 경우
- 타입으로 조회 시 같은 타입의 스프링 빈이 둘 이상이면 오류가 발생하기 때문에 이 때는 빈 이름을 지정한다.
```java
@Test
@DisplayName("빈 이름을 지정하기") 
void findBeanByName() {
	MemberRepository memberRepository = ac.getBean("memberRepository1", MemberRepository.class);
	assertThat(memberRepository).isInstanceOf(MemberRepository.class);     
}
```

### 상속 관계
- 부모 타입으로 조회하면 자식 타입도 함께 조회하기 때문에 최상위 타입인 `Object` 타입으로 조회하면 모든 스프링 빈을 조회할 수 있다. 
- 부모 타입으로 조회했을 때 자식이 둘 이상이면 중복 오류가 발생하기 때문에 빈 이름을 지정한다.
```java
@Test
@DisplayName("빈 이름 지정하기")
void findBeanByParentTypeBeanName() {
	DiscountPolicy rateDiscountPolicy = ac.getBean("rateDiscountPolicy",DiscountPolicy.class);
	assertThat(rateDiscountPolicy).isInstanceOf(RateDiscountPolicy.class);
}
```

<br>

## BeanFactory 와 ApplicationContext

### BeanFactory
- 스프링 컨테이너의 최상위 인터페이스로 스프링 빈을 관리하고 조회하는 역할을 담당한다.
- `getBean()` 뿐 아니라 빈을 관리하고 검색하는 대부분의 기능을 제공한다.
### ApplicationContext
- 빈을 관리하고 검색하는 등의 `BeanFactory` 기능을 모두 상속받아 제공한다. 
- 애플리케이션을 개발할 때는 빈을 관리하고 조회하는 기능은 물론, 수 많은 부가 기능이 필요하다. 
#### ApplicationContext 가 제공하는 부가 기능
```java
public interface ApplicationContext extends EnvironmentCapable, ListableBeanFactory, HierarchicalBeanFactory, MessageSource, ApplicationEventPublisher, ResourcePatternResolver { ..}
```
- `MessageSource` : 국제화 기능을 제공한다. 예를 들어 한국에서 들어오면 한국어로, 영어권에서 들어오면 영어로 출력한다. 
- `EnvironmentCapable` : 환경 변수 로컬, 개발, 운영 등을 구분해서 처리한다.
- `ApplicationEventPublisher` : 이벤트를 발행하고 구독하는 모델을 편리하게 지원한다. 
- `ResourcePatternResolver` : 파일, 클래스패스, 외부 등에서 리소스를 편리하게 조회한다. 

<br>

## 참고 
[인프런 - 스프링 핵심 원리 - 기본편](https://inf.run/kj9JQ)