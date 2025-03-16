# (spring) ThreadLocal 로 동시성 문제 해결하기
> LogTrace 분리 및 스프링 빈 등록, ThreadLocal 을 사용하여 동시성 문제 해결하기

<br>

### ThreadLocal을 통해 로그 추적기 문제점 해결하기 ( [로그 추적기](https://github.com/dlask913/TIL/blob/main/spring/spring-advanced-trace-logger.md) )
> - HTTP 요청을 구분하고 깊이를 표현하기 위해 TraceId 동기화가 필요하다.
> - TraceId 의 동기화를 위해 관련 메서드 및 인터페이스의 모든 파라미터를 수정해야 한다.
> - 로그를 처음 시작할 때와 처음이 아닐 때 호출해야하는 메서드가 다르다.

<br>

## LogTrace 분리 및 스프링 빈 등록
> 메서드 호출마다 TraceId 를 넘길 필요없이 LogTrace 인터페이스로 분리하여 구현체 필드 (traceIdHolder) 에 TraceId 값을 저장한다.

#### 1. LogTrace 인터페이스 분리
- 기존 로그 추적기의 기능을 정의하여 LogTrace 인터페이스를 생성한다.
- TraceId 를 구현체 필드에 저장할거기 때문에 기존 `beginSync(..)` 는 제외한다.
```java
public interface LogTrace {  
    TraceStatus begin(String message); // 시작
    void end(TraceStatus status); // 정상 종료
    void exception(TraceStatus status, Exception e); // 예외 발생
}
```

#### 2. LogTrace 구현체로, FieldLogTrace 구현
- TraceId 를 저장할 traceIdHolder 필드를 정의한다.
- `syncTraceId()` : 인스턴스의 traceIdHolder 를 통해 값을 새로 생성하거나 기존에 저장되어있는 값을 업데이트한다.
- `releaseTraceId()` : 인스턴스의 traceIdHolder 를 통해 값을 초기화하거나 기존에 저장되어있는 값을 업데이트한다.

```java
@Slf4j  
public class FieldLogTrace implements LogTrace {  
  
    private static final String START_PREFIX = "-->";  
    private static final String COMPLETE_PREFIX = "<--";  
    private static final String EX_PREFIX = "<X-";  
  
    private TraceId traceIdHolder; // traceId 동기화  
  
    @Override  
    public TraceStatus begin(String message) {  
        syncTraceId();  
        TraceId traceId = traceIdHolder; 
        Long startTimeMs = System.currentTimeMillis();  
        log.info("[{}] {}{}", traceId.getId(), addSpace(START_PREFIX, traceId.getLevel()), message);  
        return new TraceStatus(traceId, startTimeMs, message);  
    }  

    // TraceId 동기화를 위해 traceIdHolder 를 사용하도록 변경  
    private void syncTraceId() {
        if (traceIdHolder == null) {  
            traceIdHolder = new TraceId();  
        } else {  
            traceIdHolder = traceIdHolder.createNextId();  
        }  
    }  
  
    @Override  
    public void end(TraceStatus status) {  
        complete(status, null);  
    }  
  
    @Override  
    public void exception(TraceStatus status, Exception e) {  
        complete(status, e);  
    }  
  
    private void complete(TraceStatus status, Exception e) {  
        Long stopTimeMs = System.currentTimeMillis();  
        long resultTimeMs = stopTimeMs - status.getStartTimeMs();  
        TraceId traceId = status.getTraceId();  
        if (e == null) {  
            log.info("[{}] {}{} time={}ms", traceId.getId(), addSpace(COMPLETE_PREFIX, traceId.getLevel()), status.getMessage(), resultTimeMs);  
        } else {  
            log.info("[{}] {}{} time={}ms ex={}", traceId.getId(), addSpace(EX_PREFIX, traceId.getLevel()), status.getMessage(), resultTimeMs, e.toString());  
        }  
        releaseTraceId();  
    }  
  
    // TraceId 동기화를 위해 traceIdHolder 를 사용하도록 변경  
    private void releaseTraceId() {
        if (traceIdHolder.isFirstLevel()) {  
            traceIdHolder = null; // destroy  
        } else {  
            traceIdHolder = traceIdHolder.createPreviousId();  
        }  
    }  
  
    private static String addSpace(String prefix, int level) {..}  
}
```

#### 3. LogTraceConifg 생성
- `LogTrace` 를 빈으로 등록하여 스프링 컨테이너가 관리하도록 한다.
- 이를 통해 LogTrace 구현체가 여러 곳에서 사용되더라도 같은 인스턴스를 재사용할 수 있어 불필요한 객체 생성을 방지할 수 있다. 
```java
@Configuration  
public class LogTraceConfig {  
    @Bean  
    public LogTrace logTrace() {  
        return new FieldLogTrace();  
    }  
}
```

### 문제점
- FiledLogTrace 는 싱글톤으로 등록된 스프링 빈으로, 객체의 인스턴스가 애플리케이션에 딱 하나 존재하여 여러 쓰레드가 동시에 접근하면 동시성 문제가 발생한다. 
- 여러 쓰레드가 동시에 같은 인스턴스의 필드 값을 변경하며 발생하는 문제를 동시성 문제라고 한다. 
- 지역 변수는 쓰레드마다가 각각 다른 메모리 영역이 할당되기 때문에 동시성 문제가 발생하지 않는다. 주로 같은 인스턴스의 필드, 또는 static 같은 공용 필드에 접근하여 값을 변경할 때  발생한다. 
- 실행 결과 : 쓰레드가 같지 않음에도 트랜잭션ID 가 동일하고 level 도 기대와 다른 결과로 출력됨을 알 수 있다. 
```console
[nio-8080-exec-2] FieldLogTrace : [2ae092ee] OrderController.request()  
[nio-8080-exec-2] FieldLogTrace : [2ae092ee] |-->OrderService.orderItem()  
[nio-8080-exec-2] FieldLogTrace : [2ae092ee] |   |-->OrderRepository.save()  
[nio-8080-exec-3] FieldLogTrace : [2ae092ee] |   |   |-->OrderController.request()  
[nio-8080-exec-3] FieldLogTrace : [2ae092ee] |   |   |   |-->OrderService.orderItem()  
[nio-8080-exec-3] FieldLogTrace : [2ae092ee] |   |   |   |   |-->OrderRepository.save()  
[nio-8080-exec-2] FieldLogTrace : [2ae092ee] |   |<--OrderRepository.save() time=1015ms  
[nio-8080-exec-2] FieldLogTrace : [2ae092ee] |<--OrderService.orderItem() time=1016ms  
[nio-8080-exec-2] FieldLogTrace : [2ae092ee] OrderController.request() time=1016ms  
[nio-8080-exec-3] FieldLogTrace : [2ae092ee] |   |   |   |   |<--OrderRepository.save() time=1011ms  
[nio-8080-exec-3] FieldLogTrace : [2ae092ee] |   |   |   |<--OrderService.orderItem() time=1011ms  
[nio-8080-exec-3] FieldLogTrace : [2ae092ee] |   |   |<--OrderController.request() time=1011ms
```

<br>

## ThreadLocal 을 사용하여 동시성 문제 해결하기
- 해당 쓰레드만 접근할 수 있는 특별한 저장소로, 쓰레드마다 별도의 내부 저장소를 제공한다. 
- 자바는 언어차원에서 쓰레드 로컬을 지원하기 위한 `java.lang.ThreadLocal` 클래스를 제공한다.
#### 1. LogTrace 구현체로, ThreadLocal 을 사용한 ThreadLocalLogTrace 구현
- 구현체 필드 타입을 ThreadLocal 로 변경하여 쓰레드별로 별도의 저장소를 갖게 한다.
- ThreadLocal 을 모두 사용 후에는 꼭 저장된 값을 제거한다.
```java
@Slf4j  
public class ThreadLocalLogTrace implements LogTrace {  
  
    private static final String START_PREFIX = "-->";  
    private static final String COMPLETE_PREFIX = "<--";  
    private static final String EX_PREFIX = "<X-";  
  
    // TraceId → ThreadLocal<저장할타입>
    private ThreadLocal<TraceId> traceIdHolder = new ThreadLocal<>();
  
    @Override  
    public TraceStatus begin(String message) {  
        syncTraceId();  
        TraceId traceId = traceIdHolder.get(); // ThreadLocal TraceId 조회
        Long startTimeMs = System.currentTimeMillis();  
        log.info("[{}] {}{}", traceId.getId(), addSpace(START_PREFIX, traceId.getLevel()), message);  
        return new TraceStatus(traceId, startTimeMs, message);  
    }  
  
    private void syncTraceId() {  
        TraceId traceId = traceIdHolder.get();  
        if (traceId == null) {  // ThreadLocal TraceId 새로 저장  
            traceIdHolder.set(new TraceId());
        } else {  // ThreadLocal TraceId 업데이트
            traceIdHolder.set(traceId.createNextId()); 
        }  
    }  
  
    @Override  
    public void end(TraceStatus status) {...}  
  
    @Override  
    public void exception(TraceStatus status, Exception e) {...}  
  
    private void complete(TraceStatus status, Exception e) {...}  
  
    private void releaseTraceId() {  
        TraceId traceId = traceIdHolder.get(); // ThreadLocal TraceId 조회  
        if (traceId.isFirstLevel()) {  
            traceIdHolder.remove(); // destroy, 사용 후 저장된 값 제거  
        } else {  // ThreadLocal TraceId 업데이트
            traceIdHolder.set(traceId.createPreviousId()); 
        }  
    }  
  
    private static String addSpace(String prefix, int level) {...}  
}
```

#### 2. LogTraceConifg 구현체 변경
- LogTrace 에 주입할 인스턴스를 ThreadLocalLogTrace 로 변경한다.
```java
@Configuration  
public class LogTraceConfig {  
  
    @Bean  
    public LogTrace logTrace() {  
        return new ThreadLocalLogTrace();  
    }  
}
```

#### 3. 애플리케이션 적용
- TraceId 를 파라미터로 넘기지 않고 LogTrace 내부에서 관리하도록 전달 방식 변경
- 기존 `beginSync()` 가 제거되고 `begin()` 만 사용하도록 변경
```java
public class OrderController {
	..
	private final LogTrace trace;
	
	@GetMapping("/request")  
	public String request(String itemId) {  
	    TraceStatus status = null;  
		try {  
		    status = trace.begin("OrderController.request()");  
		    orderService.orderItem(itemId);  
		    trace.end(status);  
		    return "ok";  
		} catch (Exception e) {  
		    trace.exception(status, e);  
		    throw e;
		}
	}
}

public class OrderService {
	..
	private final LogTrace trace;
	
	public void orderItem(TraceId traceId, String itemId) {  
	    TraceStatus status = null;  
		try {  
		    status = trace.begin("OrderService.orderItem()");  
		    orderRepository.save(itemId);  
		    trace.end(status);  
		} catch (Exception e) {  
		    trace.exception(status, e);  
		    throw e;  
		}
	}
}
```

### 주의사항
- 쓰레드 로컬 값을 사용 후 제거하지 않으면 WAS(톰캣) 처럼 쓰레드 풀을 사용하는 경우 심각한 문제가 발생할 수 있다.
- 예를 들어, 사용자A 의 HTTP 응답이 끝나면 WAS 는 사용이 끝난 `thread-A` 를 쓰레드 풀에 반환한다. 쓰레드를 생성하는 비용은 비싸기 때문에 쓰레드를 제거하지 않고 보통 쓰레드 풀을 통해 쓰레드를 재사용한다.
- 그래서 `thread-A` 는 쓰레드풀에 계속 살아있고 쓰레드 로컬의 `thread-A` 전용 보관소에 사용자A 데이터도 함께 살아있게 된다.
- 근데 사용자B 가 새로운 HTTP 요청을 했을 때 만약 `thread-A` 가 할당된다면 조회했을 때 사용자A 데이터를 반환하게 된다. → 사용자B 가 사용자A 의 데이터를 확인하게 되는 문제 발생

<br>

## 참고 
[인프런 - 스프링 핵심 원리 고급편](https://inf.run/FWeFN) 