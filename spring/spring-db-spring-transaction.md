# (spring) 스프링 트랜잭션 이해
> 트랜잭션 추상화, 스프링 트랜잭션 ( 사용 방식, 적용 확인, 규칙, 옵션 )

<br>

## 트랜잭션 추상화
- JDBC 기술과 JPA 기술은 트랜잭션을 사용하는 코드 자체가 다른데, 이렇게 각 데이터 접근 기술들이 트랜잭션을 처리하는 방식을 동일한 방식으로 사용할 수 있도록 도와준다. 
- 스프링은 `PlatformTransactionManger` 라는 인터페이스를 통해 트랜잭션을 추상화한다. 
- 트랜잭션을 추상화해서 제공할 뿐 아니라 실무에서 주로 사용하는 데이터 접근 기술에 대한 트랜잭션 매니저의 구현체도 제공한다. 
- 스프링 부트는 어떤 데이터 접근 기술을 사용하는지 자동으로 인식해서 적절한 트랜잭션 매니저를 선택해 스프링 빈으로 등록해주기 때문에 트랜잭션 매니저를 선택하고 등록하는 과정도 생략할 수 있다.
![image](https://github.com/user-attachments/assets/ab2cfe9e-66e2-4c9f-acb5-2c8dc3e6e899)

<br>

## 스프링 트랜잭션 

### 스프링 트랜잭션 사용 방식
#### 1. 선언적 트랜잭션 관리
- @Transactional 을 선언해서 적용한다. 
- 간편하고 실용적이다. 
- 기본적으로 **프록시 방식의 AOP 가 적용**되며, 특정 클래스나 메서드에 하나라도 있으면 트랜잭션 AOP 는 프록시를 만들어서 스프링 컨테이너에 등록한다. 
#### 2. 프로그래밍 방식 트랜잭션 관리 
- 트랜잭션 매니저 또는 트랜잭션 템프릿 등을 사용해서 트랜잭션 관련 코드를 직접 작성한다. 
- 애플리케이션 코드가 트랜잭션 코드와 강하게 결합된다. 

<br>

### 선언적 트랜잭션과 AOP 
> Client → **트랜잭션 프록시 ( 트랜잭션 시작 - 처리 - 종료 )** → 서비스 ( 비즈니스 로직 ) → 리포지토리 ( 데이터 접근 로직 )

- 트랜잭션을 처리하기 위한 프록시를 적용하면 트랜잭션을 처리하는 객체와 비즈니스 로직을 처리하는 서비스 객체를 명확하게 분리할 수 있다. 

#### 프록시 도입 후 전체 과정

![image](https://github.com/user-attachments/assets/3346846e-ddac-422e-8aca-3f923de7beff)

1. 프록시 호출
2. 스프링 컨테이너를 통해 트랜잭션 매니저 획득
3. transactionManger.getTransaction()
4. 데이터 소스 → 커넥션 생성
5. 트랜잭션은 커넥션에 con.setAutocommit(false) 를 지정하면서 시작
6. 커넥션 보관
7. 같은 트랜잭션을 유지하기 위해 같은 데이터베이스 커넥션 사용 ( 이를 위해 스프링 내부에서는 트랜잭션 동기화 매니저가 사용 된다 )
8. 실제 서비스 호출
9. 트랜잭션 동기화 획득 : JdbcTemplate 을 포함한 대부분의 데이터 접근 기술들은 트랜잭션을 유지하기 위해 내부에서 트랜잭션 동기화 매니저를 통해 리소스(커넥션) 을 동기화 한다. 

<br>

### 트랜잭션 적용 확인
- @Transactional 을 통해 트랜잭션을 적용하면 실제 트랜잭션이 적용되고 있는지 아닌지를 확인하기 어렵다. 
- `AopUtils.isAopProxy()` : AOP 적용 여부를 확인할 수 있다. 
```java
@Autowired BasicService basicService;  

@Test  
void proxyCheck() {  
    log.info("aop class={}", basicService.getClass());  
    assertThat(AopUtils.isAopProxy(basicService)).isTrue(); // true
}
/** 실행 로그 
..TxBasicTest$BasicService$$EnhancerBySpringCGLIB$$63eb0ea8 **/
```
- `transaction.interceptor=TRACE` : application.properties 에 설정을 추가하여 트랜잭션 프록시가 호출하는 트랜잭션의 시작과 종료를 명확하게 확인할 수 있다. 
```properties
logging.level.org.springframework.transaction.interceptor=TRACE
```
- `TransactionSynchronizationManager.isActualTransactionActive()` : 현재 스레드에 트랜잭션이 적용되어 있는지 확인할 수 있다. 
```java
@Transactional  
public void tx() {  
    boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
    log.info("tx active={}", txActive);  
}
/** 실행 결과
tx active=true **/
```

<br>

### @Transactional 규칙
#### 1. 우선순위 규칙
- 항상 더 구체적이고 자세한 것이 높은 우선 순위를 가진다. 
- 클래스의 메서드 > 클래스의 타입 > 인터페이스의 메서드 > 인터페이스의 타입 순서
- 인터페이스에 `@Transactional` 을 사용하면 AOP 적용 방식에 따라 적용 되지 않는 경우도 있기 때문에 스프링 공식 메뉴얼에서 권장하지 않는다.
#### 2. 클래스에 적용하면 메서드는 자동 적용

#### ※ public 메서드만 트랜잭션 적용
- 스프링의 트랜잭션 AOP 기능은 `public` 메서드에만 트랜잭션을 적용하도록 기본 설정이 되어 있다. 
- 클래스 레벨에 트랜잭션을 적용하면 모든 메서드에 트랜잭션이 걸려, 의도하지 않는 곳까지 트랜잭션이 과도하게 적용될 수 있기 때문이다. 
- 주로 비즈니스 로직의 시작점에 걸기 때문에 대부분 외부에 열어준 곳을 시작점으로 사용하기 때문이다. 
- `public` 이 아닌 곳에 `@Transactional` 이 붙어 있으면 예외가 발생하지는 않고 트랜잭션 적용만 무시된다.

<br>

### 트랜잭션 AOP 주의 사항 - 초기화 시점
- 초기화 코드 ( `@PostConstructor` ) 와 `@Transactional` 을 같이 사용하면 초기화 코드가 먼저 호출되고 그 다음에 트랜잭션 AOP 가 적용되기 때문에 트랜잭션이 적용되지 않는다. 
- 가장 확실한 대안으로 `ApplicationReadyEvent` 이벤트를 사용하면 트랜잭션 AOP 를 포함한 스프링이 컨테이너가 완정히 생성되고 난 다음에 이벤트가 붙은 메서드를 호출해준다.
```java
// @PostConstructor
@EventListener(value = ApplicationReadyEvent.class) 
@Transactional
public void init() {..}
```

<br>

### 트랜잭션 옵션
#### value, transactionManager
- 사용할 트랜잭션 매니저 지정
- 이 값을 생략하면 기본으로 등록된 트랜잭션 매니저를 사용하기 때문에 대부분 생략한다. 
- 사용하는 트랜잭션 매니저가 둘 이상이라면 이름을 지정해서 구분한다.
#### rollbackFor
- 예외 발생 시 체크 예외인 Exception 과 그 하위 예외들은 커밋하는 것이 기본 정책인데, 이 옵션을 통해 기본 정책에 추가로 어떤 예외가 발생할 때 롤백할 지 지정할 수 있다. 
```java
@Transactional(rollbackFor = Exception.class)
```
#### noRollbackFor
- 기본 정책에 추가로 어떤 예외가 발생했을 때 롤백하면 안되는 지 지정할 수 있다.
#### propagation
- 트랜잭션 전파에 대한 옵션
#### isolation
- 트랜잭션 격리 수준을 지정할 수 있다. 
- 기본 값은 `DEFAULT` 로, 대부분 데이터베이스에서 설정한 기준을 따르고 개발자가 직접 지정하는 경우는 드물다. 
  - `DEFAULT` : 데이터베이스에서 설정한 격리 수준을 따른다. 
  - `READ_UNCOMMITTED` : 커밋되지 않은 읽기
  - `READ_COMMITTED` : 커밋된 읽기
  - `REPEATABLE_READ` : 반복 가능한 읽기
  - `SERIALIZABLE` : 직렬화 가능
#### timeout
- 트랜잭션 수행 시간에 대한 타임아웃을 초 단위로 지정한다. 
- 기본 값은 트랜잭션 시스템의 타임아웃을 사용하는데 운영 환경에 따라 동작하지 않는 경우도 있기 때문에 확인하고 사용한다. 
#### label
- 트랜잭션 애노테이션에 있는 값을 직접 읽어서 어떤 동작을 하고 싶을 때 사용할 수 있다. 
- 일반적으로 사용하지 않는다. 
#### readOnly 
- 트랜잭션은 기본적으로 읽기 쓰기가 모두 가능한 트랜잭션이 생성된다. ( `readOnly = false` )
- `true` 로 사용하면 읽기 전용 트랜잭션으로 생성되어 등록, 수정, 삭제가 안되고 읽기 기능만 작동한다. ( 드라이버나 DB 에 따라 정상 동작하지 않는 경우도 있다 )
- 크게 프레임워크, JDBC 드라이버, 데이터베이스 3곳에서 적용된다.

<br>

## 참고 
[인프런 - 스프링 DB 2편 - 데이터 접근 활용 기술](https://inf.run/NMpER) 