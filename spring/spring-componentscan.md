# (spring) 컴포넌트 스캔
> 컴포넌트 스캔과 의존관계 자동 주입, 탐색 위치와 기본 스캔 대상, 필터, 중복 등록과 충돌

<br>

## 컴포넌트 스캔과 의존관계 자동 주입
- 스프링은 개발자가 수동으로 빈을 등록하지 않아도 자동으로 스프링 빈을 등록하는 컴포넌트 스캔이라는 기능을 제공한다. 
- 의존관계도 자동으로 주입하는 `@Autowired` 기능도 제공한다.

### 적용 예제
- 컴포넌트 스캔은 `@Component` 가 붙은 클래스 뿐 아니라 `@Configuration` 이 붙은 설정 정보도 자동으로 등록한다.
```java
@Component
public class MemoryMemberRepository implements MemberRepository {}

@ComponentScan
public class AutoAppConfig { }
```

- `@Autowired` 를 사용하면 생성자에서 의존관계를 주입받을 수 있다. 
```java
@Component
public class OrderServiceImpl implements OrderService {
	private final MemberRepository memberRepository;
    @Autowired
	public OrderServiceImpl(MemberRepository memberRepository) {
		this.memberRepository = memberRepository; 
	} 
}
```

### 동작 방식
#### @ComponentScan
- `@Component` 가 붙은 모든 클래스를 스프링 빈으로 등록한다. 
- 이때 스프링 빈의 기본 이름은 클래스명을 사용하되 맨 앞글자만 소문자를 사용한다. ( MemberRepository → memberRepository )
- 스프링 빈의 이름을 직접 부여할 수도 있다. ( `@Component("memberRepository2")` )
#### @Autowired
- 생성자에 `@Autowired` 를 지정하면 스프링 컨테이너가 자동으로 해당 스프링 빈을 찾아서 주입한다. 
- 생성자에 파라미터가 많아도 다 자동으로 주입해준다.

<br>

## 탐색 위치와 기본 스캔 대상
#### 탐색 대상 시작 위치 지정
- `basePackages` : 모든 자바 클래스를 다 컴포넌트 스캔하면 시간이 오래 걸리기 때문에 탐색할 패키지의 시작 위치를 지정할 수 있다.
- `basePackageClasses` : 지정한 클래스의 패키지를 탐색 시작 위치로 지정한다.
- 만약 지정하지 않으면 `@ComponentScan` 이 붙은 설정 정보 클래스의 패키지가 시작 위치가 된다.
- 패키지 위치를 지정하지 않고 설정 정보 클래스의 위치를 프로젝트 최상단에 두는 것을 권장한다. ( 스프링 부트 기본 제공 )
```java
@ComponentScan(
        basePackages = "hello.core", 
)
```

#### 컴포넌트 스캔 기본 대상
- 컴포넌트 스캔은 `@Component` 뿐 아니라 `@Conroller`, `@Service`, `@Repository`, `@Configuration` 과 같은 내용도 추가로 대상에 포함한다.
- 기본적으로 `useDefaultFilters` 옵션이 켜져있는데 이 옵션을 끄면 기본 스캔 대상들이 제외된다.

<br>

## 필터
- includeFilters : 컴포넌트 스캔 대상을 추가로 지정한다.
- excludeFilters : 컴포넌트 스캔에서 제외할 대상을 지정한다. 
- 스프링 부트는 컴포넌트 스캔을 기본으로 제공하는데 옵션을 변경하며 사용하기보다는 스프링의 기본 설정에 최대한 맞추어 사용하는 것을 권장한다.
```java
// @MyIncludeComponent 가 붙은 class 를 스캔 대상에 추가
// @MyExcludeComponent 가 붙은 class 를 스캔 대상에서 제외
@ComponentScan(
  includeFilters = @Filter(type = FilterType.ANNOTATION, classes = MyIncludeComponent.class),
  excludeFilters = @Filter(type = FilterType.ANNOTATION, classes = MyExcludeComponent.class)
)
```

#### FilterType 옵션
- ANNOTATION : 기본값, 애노테이션을 인식해서 동작한다. 
- ASSIGNABLE_TYPE : 지정한 타입과 자식 타입을 인식해서 동작한다. 
- ASPECTJ : AspectJ 패턴 사용
- REGEX : 정규 표현식 사용
- CUSTOM : TypeFilter 라는 인터페이스를 구현해서 처리

<br>

## 중복 등록과 충돌
#### 1. 자동 빈 등록 vs 자동 빈 등록
- 컴포넌트 스캔에 의해 자동으로 빈이 등록되는데 그 이름이 같은 경우 스프링은 오류를 발생시킨다. → `ConflictingBeanDefinitionException`

#### 2. 수동 빈 등록 vs 자동 빈 등록
- 수동 빈 등록이 우선권을 가지고 수동 빈이 자동 빈을 오버라이딩 한다.
- 보통은 개발자의 의도적으로 이런 결과를 유도하기보다는 설정들이 꼬여 이런 결과가 만들어지는 경우가 대부분이다. 
- 스프링 부트는 수동 빈 등록과 자동 빈 등록이 충돌나면 오류가 발생하도록 기본 값이 설정되어 있다.
- 기본 값은 allow-bean-definition-overriding 을 false 로 정의하여 변경할 수는 있으나 권장하지 않는다.
```console
// 에러 로그
Consider renaming one of the beans or enabling overriding by setting spring.main.allow-bean-definition-overriding=true
```

<br>

## 참고 
[인프런 - 스프링 핵심 원리 - 기본편](https://inf.run/kj9JQ)