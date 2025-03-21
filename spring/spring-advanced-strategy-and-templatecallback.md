# (spring) 전략 패턴과 템플릿 콜백 패턴
> 템플릿 메서드 패턴를 적용한 로그 추적기의 상속 문제 해결, 전략 패턴, 템플릿 콜백 패턴

<br>

### 템플릿 메서드 패턴를 적용한 로그 추적기의 상속 문제 해결
> - 템플릿 메서드 패턴을 사용하게 되면 부모 클래스와 자식 클래스가 강하게 결합되는 상속의 문제점을 갖게 된다.
> - 상속이 아닌 위임을 통해 문제를 해결한다.

<br>

## 전략 패턴 ( Strategy )
- 변하지 않는 부분을 템플릿 코드를 `Context` 에, 변하는 부분을 `Strategy` 인터페이스에 구현하여 문제를 해결한다 → 위임 
- `Strategy` 인터페이스를 생성하고 구현체를 개별적으로 생성하여 필요한 구현체를 그 때마다 주입받아 사용할 수 있다.
- GOF 디자인 패턴 목적 : 알고리즘 제품군을 정의하고 각각을 상호 교환하게 만든다. 전략을 사용하면 알고리즘을 사용하는 클라이언트와 독립적으로 알고리즘을 변경할 수 있다. 

<br>

### 전략 패턴 실습
#### 1. Strategy 인터페이스와 구현체 생성하여 변하는 코드 작성하기
```java
public interface Strategy { // 변하는 알고리즘 역할
    void call();  
}

@Slf4j  
public class StrategyLogic1 implements Strategy { // 개별적으로 구현체 생성
    @Override  
    public void call() {  
        log.info("비즈니스 로직1 실행");  
    }  
}

@Slf4j  
public class StrategyLogic2 implements Strategy { // 개별적으로 구현체 생성
    @Override  
    public void call() {  
        log.info("비즈니스 로직2 실행");  
    }  
}
```

#### 2. Context 생성하여 변하지 않는 로직을 갖는 템플릿 코드 작성하기
- `Context` 는 `Strategy` 인터페이스에만 의존하기 때문에 `Strategy` 의 구현체를 변경하거나 새로 만들어도 `Context` 에 영향을 주지 않는다.
```java
@Slf4j  
public class Context {  
  
    private Strategy strategy;  
  
    public Context(Strategy strategy) { // 인터페이스에 의존
        this.strategy = strategy; 
    }  
  
    public void execute() {  
        long startTime = System.currentTimeMillis();  
        // 비즈니스 로직 실행  
        strategy.call(); // 위임  
        // 비즈니스 로직 종료  
        long endTime = System.currentTimeMillis();  
        long resultTime = endTime - startTime;  
        log.info("resultTime={}", resultTime);  
    }  
}
```

#### 3. 전략 패턴 사용하기
```java
StrategyLogic1 strategyLogic1 = new StrategyLogic1();  
ContextV context = new ContextV1(strategyLogic1); // 원하는 구현체 주입
context.execute(); 
```

#### 동작 순서 
1. Context 에 원하는 Strategy 구현체를 주입한다. 
2. 클라이언트가 context 를 실행한다.
3. context 는 context 로직 ( `execute()` )을 시작한다.
4. context 로직 중간에 `strategy.call()` 을 호출하여 주입 받은 strategy 로직을 실행한다.
5. context 는 나머지 로직을 실행한다.

<br>

### 익명 내부 클래스 실습
- 익명 내부 클래스를 사용할 수도 있고 인터페이스에 메서드가 1개만 있는 경우 람다로 사용할 수도 있다. 
```java
// 익명 내부 클래스 사용
ContextV1 context1 = new ContextV1(new Strategy() {  
    @Override  
    public void call() {  
        log.info("비즈니스 로직1 실행");  
    }  
});  
context1.execute();

// 람다식 사용
ContextV1 context1 = new ContextV1(() -> log.info("비즈니스 로직1 실행"));  
context1.execute();
```

<br>

### 문제점
> 선조립, 후실행 방식은 조립한 이후 전략을 변경하는 게 번거롭다.

- Context 내부 필드에 Strategy 를 주입해서 사용한다는 것은 선조립, 후실행을 의미한다.
- Context 를 싱글톤으로 사용할 때 전략을 변경하려면 동시성 이슈 등을 고려해야 한다.
- 전략을 `execute(..)` 가 호출될 때마다 항상 파라미터로 전달하여 해결할 수 있는데, 이는 실행할 때마다 전략을 계속 지정해주어야 한다는 단점이 될 수 있다. → 전략 패턴 파라미터 방식, 템플릿 콜백 패턴과 유사

<br>

## 템플릿 콜백 패턴
- 코드를 실행할 때 변하지 않는 템플릿 안에서 원하는 부분만 살짝 다른 코드를 실행하고 싶을 때 적합하다.
- 다른 코드의 인수로서 넘겨주는 실행 가능한 코드를 콜백(callback) 이라고 한다.
- 전략 패턴에서 Context 가 템플릿 역할을 하고 Strategy 부분이 콜백으로 넘어온다고 생각하면 된다. 
- GOF 디자인 패턴에 정의된 게 아니라 스프링 내부에서 자주 사용하는 방식이다. ( `JdbcTemplate`, `RestTemplate`, `TransactionTemplate`, `RedisTemplate` )

<br>

### 템플릿 콜백 패턴 실습
#### 1. 콜백 로직을 전달할 `Callback` 인터페이스 생성하기
```java
public interface Callback {  
    void call();  
}
```

#### 2. `TimeLogTemplate` 생성하여 템플릿 코드 작성하기
```java
@Slf4j  
public class TimeLogTemplate {  
    public void execute(Callback callback) {  
        long startTime = System.currentTimeMillis();  
        // 비즈니스 로직 실행  
        callback.call(); // 위임  
        // 비즈니스 로직 종료  
        long endTime = System.currentTimeMillis();  
        long resultTime = endTime - startTime;  
        log.info("resultTime={}", resultTime);  
    }  
}
```

#### 3. 익명 내부 클래스로 템플릿 콜백 패턴 사용하기
```java
TimeLogTemplate template = new TimeLogTemplate();  
template.execute(new Callback() {  
    @Override  
    public void call() {  
        log.info("비즈니스 로직1 실행");  
    }  
});

// 람다식
TimeLogTemplate template = new TimeLogTemplate();  
template.execute(() -> log.info("비즈니스 로직1 실행"));
```

#### 동작 순서
1. 클라이언트가 Temlate 을 실행하며 callback 전달
2. execute() 시작
3. callback.call() → callback
4. execute() 종료

<br>

### 로그 추적기에 템플릿 콜백 패턴 적용하기
#### 1. 콜백을 전달할 TraceCallback 인터페이스 생성
```java
public interface TraceCallback<T> {  
    T call();  
}
```

#### 2. TraceTemplate 생성하여 템플릿 코드 작성
```java
public class TraceTemplate {  
  
    private final LogTrace trace;  
  
    public TraceTemplate(LogTrace trace) { // 인터페이스에 의존
        this.trace = trace;  
    }  
  
    public <T> T execute(String message, TraceCallback<T> callback) {  
        TraceStatus status = null;  
        try {  
            status = trace.begin(message);  
            T result = callback.call(); // 로직 호출  
            trace.end(status);  
            return result;  
        } catch (Exception e) {  
            trace.exception(status, e);  
            throw e;  
        }  
    }  
}
```

#### 3. Template 에 LogTrace 를 주입 받아 로그 추적기 적용
```java
@Service  
public class OrderService {  
  
    private final OrderRepository orderRepository;  
    private final TraceTemplate template;  
  
    public OrderService(OrderRepository orderRepository, LogTrace trace) {  
        this.orderRepository = orderRepository;  
        this.template = new TraceTemplate(trace); // LogTrace 주입
    }  
  
    public void orderItem(String itemId) {  
        // 템플릿 실행하면서 콜백을 전달
        template.execute("OrderService.orderItem()", () -> {
            orderRepository.save(itemId);  
            return null;  
        });  
    }  
}
```

<br>

### 문제점
- 변하는 코드와 변하지 않는 코드를 분리하고 콜백으로 람다를 사용해서 코드 사용을 최소화하였지만 결국 로그 추적기 적용을 위해 원본 코드를 수정해야 한다.

<br>

## 참고 
[인프런 - 스프링 핵심 원리 고급편](https://inf.run/FWeFN) 