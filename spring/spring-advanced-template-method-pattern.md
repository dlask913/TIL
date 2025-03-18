# (spring) 템플릿 메서드 패턴
> 템플릿 메서드 패턴으로 로그 추적기 코드 복잡도 감소 시키기

<br>

### 템플릿 메서드 패턴으로 로그 추적기 코드 복잡도 감소 시키기
> - 로그 추적 로직을 추가할 때마다 try-catch 문으로 인해 코드 복잡성이 증가하였다. 
> - 템플릿 메서드 패턴을 통해 변하는 것(핵심 기능)과 변하지 않는 것(부가 기능)을 분리한다.

<br>

### 핵심 기능과 부가 기능
- 핵심 기능 : 해당 객체가 제공하는 고유의 기능을 말한다. 
```java
orderServicee.orderItem();
```
- 부가 기능 : 핵심 기능을 보조하기 위해 제공되는 기능으로 로그 추적 로직, 트랜잭션 기능이 있다. 단독으로 사용되지 않고 핵심 기능과 함께 사용된다. 
```java
TraceStatus status = null;  
try {  
    status = trace.begin("OrderService.orderItem()");  
    // 핵심 기능 호출
    trace.end(status);  
} catch (Exception e) {  
    trace.exception(status, e);  
    throw e;  
}
```

<br>

## 템플릿 메서드 패턴
> 핵심 기능과 부가 기능을 분리하여 문제를 해결하기 위해 사용하는 디자인 패턴

- 변하지 않는 코드를 부모 클래스에, 변하는 부분을 자식 클래스에 두고 상속과 오버라이딩을 사용한다. → 상속과 오버라이딩을 통한 다형성으로 문제 해결
- GoF 디자인 패턴 목적 : 작업에서 알고리즘의 골격을 정의하고 일부 단계를 하위 클래스로 연기한다. 템플릿 메서드를 사용하면 하위 클래스가 알고리즘의 구조를 변경하지 않고도 알고리즘의 특정 단계를 재정의할 수 있다. 

<br>

### 템플릿 메서드 패턴 실습
#### 1. 부모 클래스 생성하여 템플릿 코드 작성하기
```java
@Slf4j  
public abstract class AbstractTemplate {  
    public void execute() {  
        long startTime = System.currentTimeMillis();  
        // 비즈니스 로직 실행  
        call(); // 상속  
        // 비즈니스 로직 종료  
        long endTime = System.currentTimeMillis();  
        long resultTime = endTime - startTime;  
        log.info("resultTime={}", resultTime);  
    }  
    protected abstract void call();  
}
```

#### 2. 자식 클래스 생성하여 핵심 기능 작성하기
```java
@Slf4j  
public class SubClassLogic1 extends AbstractTemplate {  
    @Override  
    protected void call() {  
        log.info("비즈니스 로직1 실행");  
    }  
}

@Slf4j  
public class SubClassLogic2 extends AbstractTemplate {  
    @Override  
    protected void call() {  
        log.info("비즈니스 로직2 실행");  
    }  
}
```

#### 3. 결론
- 비즈니스 로직을 실행하기 위해 자식 클래스를 계속 만들어야 하는 단점이 있다.
- 익명 내부 클래스를 사용하면 객체 인스턴스를 생성하면서 동시에 상속 받은 자식 클래스를 정의하여 생성할 수 있다. 
- 익명 내부 클래스 : 클래스 이름을 직접 지정하지 않고 클래스 내부에 선언되는 클래스

<br>

### 익명 내부 클래스 사용하여 로그 추적기에 적용하기
#### 1. 부모 클래스 생성하여 템플릿 코드 작성하기
```java
public abstract class AbstractTemplate<T> {  
  
    private final LogTrace trace;  
  
    public AbstractTemplate(LogTrace trace) {  
        this.trace = trace;  
    }  
  
    public T execute(String message) {  
        TraceStatus status = null;  
        try {  
            status = trace.begin(message);  
            T result = call(); // 비즈니스 로직 호출  
            trace.end(status);  
            return result;  
        } catch (Exception e) {  
            trace.exception(status, e);  
            throw e;  
        }  
    }  
  
    protected abstract T call();  
}
```

#### 2. 익명 클래스 사용하여 로그 추적기에 적용하기
- 객체를 생성하며 AbstractTemplate 을 상속받은 자식 클래스를 정의하였기 때문에 별도의 자식 클래스를 만들 필요가 없다. 
```java
ublic class OrderService {  
    ..
    public void orderItem(String itemId) {  
        AbstractTemplate<Void> template = new AbstractTemplate<>(trace) {  
            @Override  
            protected Void call() {  // 반환 타입이 없을 때, Void 로 선언
                orderRepository.save(itemId); // 비즈니스 로직 호출
                return null;
            }  
        };  
        template.execute("OrderService.orderItem()");  
    }  
}
```

#### 3. 결론
- 로그를 출력하는 템플릿 코드는 부모 클래스에, 핵심 기능은 자식 클래스를 만들어 분리하여 핵심 기능에 좀 더 집중할 수 있게 되었다.
- 템플릿 코드를 수정해야 한다면 한 번만 수정해도 되기 때문에 단일 체계의 원칙을 지킨다. (SRP)

<br>

### 문제점
> 상속을 사용하기 때문에 상속에서 오는 단점들을 그대로 가져간다.

- 자식 클래스와 부모클래스가 컴파일 시점에 강하게 결합된다. ( 의존관계 )
- 자식 클래스에서 부모 클래스의 기능을 사용하지 않음에도 부모클래스를 상속받고 있으며, 코드에 명확히 적혀있다. ( 자식 클래스 extends 부모 클래스 )
- 부모 클래스를 잘못 수정하면 자식 클래스에 영향을 미친다.

<br>

## 참고 
[인프런 - 스프링 핵심 원리 고급편](https://inf.run/FWeFN) 