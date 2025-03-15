# (spring) 로그 추적기
> 로그 추적기 요구사항, 개발, 문제점

<br>

### 요구 사항
> 어떤 부분에서 병목이 발생하는지, 어떤 부분에서 예외가 발생하는 지 로그를 통해 확인한다.

- 모든 PUBLIC 메서드의 호출과 응답 정보를 로그로 출력한다.
- 애플리케이션의 흐름을 변경하지 않는다.
- 메서드 호출에 걸린 시간을 기록한다.
- 정상 흐름과 예외 흐름을 구분하여 예외 발생 시 정보를 남긴다.
- 메서드 호출의 깊이를 표현한다. 
- HTTP 요청 단위 특정 ID 를 남겨서 요청을 명확히 구분한다.

<br>

## 로그 추적기 개발

#### 1. TraceId 구현
- 하나의 HTTP 요청에 대한 서비스 흐름을 구분할 고유 ID 를 정의한다.
- 트랜잭션의 깊이를 나타낼 level 을 정의한다. 
```java
@Getter  
@AllArgsConstructor
public class TraceId {  
  
    private String id;  
    private int level;  
  
    public TraceId() {  
        this.id = createId();  
        this.level = 0;  
    }
  
    private String createId() {  // 고유 ID 생성
        return UUID.randomUUID().toString().substring(0, 8);  
    }
    public TraceId createNextId() { // TraceId 의 다음 단계
        return new TraceId(id, level + 1);  
    }
    public TraceId createPreviousId() {  // TraceId 의 이전 단계
        return new TraceId(id, level - 1);  
    }
    public boolean isFirstLevel() {  
        return level == 0;  
    }  
}
```

#### 2. TraceStatus 구현
- 로그의 상태 정보를 저장하기 위한 용도로, 로그 시작할 때와 종료할 때 사용한다.
- TraceId 와 시작 시간, 시작 시 사용할 메시지를 정의한다.
```java
@Getter  
@AllArgsConstructor  
public class TraceStatus {  
    private TraceId traceId;  
    private Long startTimeMs; // 시작 시간  
    private String message; // 시작 시 사용한 메시지  
}
```

#### 3. 추적 로깅 구현
- `begin(String message)` : 로그 시작
- `beginSync(TraceId beforeTraceId, String message)` : traceId 와 level 을 동기화시키기 위해 이전 traceId 를 받아와서 HTTP 요청 구분  
- `end(TraceStatus status)` : 비지니스 로직 정상 종료 시 호출
- `exception(TraceStatus status, Exception e)` : 비즈니스 로직 예외 발생 시 호출
- `complete(TraceStatus status, Exception e)` : 실행 시간과 함께 종료/예외 로그 출력
- `addSpace(String prefix, int level)` : 레벨과 상태에 맞는 깊이 추가

```java
@Slf4j  
@Component  
public class HelloTrace {  

    private static final String START_PREFIX = "-->";  
    private static final String COMPLETE_PREFIX = "<--";  
    private static final String EX_PREFIX = "<X-";  
  
    public TraceStatus begin(String message) {  
        TraceId traceId = new TraceId();  
        Long startTimeMs = System.currentTimeMillis();  
        log.info("[{}] {}{}", traceId.getId(), addSpace(START_PREFIX, traceId.getLevel()), message);  
        return new TraceStatus(traceId, startTimeMs, message);  
    }  
  
    public TraceStatus beginSync(TraceId beforeTraceId, String message) {    
        TraceId nextId = beforeTraceId.createNextId();  
        Long startTimeMs = System.currentTimeMillis();  
        log.info("[{}] {}{}", nextId.getId(), addSpace(START_PREFIX, nextId.getLevel()), message);  
        return new TraceStatus(nextId, startTimeMs, message);  
	}
  
    public void end(TraceStatus status) {  
        complete(status, null);  
    }  
  
    public void exception(TraceStatus status, Exception e) {  
        complete(status, e);  
    }  
  
    private void complete(TraceStatus status, Exception e) {  
        Long stopTimeMs = System.currentTimeMillis();  
        long resultTimeMs = stopTimeMs - status.getStartTimeMs();  
        TraceId traceId = status.getTraceId();  
        if (e == null) { // 정상 종료
            log.info("[{}] {}{} time={}ms", traceId.getId(), addSpace(COMPLETE_PREFIX, traceId.getLevel()), status.getMessage(), resultTimeMs);  
        } else { // 예외 발생
            log.info("[{}] {}{} time={}ms ex={}", traceId.getId(), addSpace(EX_PREFIX, traceId.getLevel()), status.getMessage(), resultTimeMs, e.toString());  
        }  
    }  
  
    private static String addSpace(String prefix, int level) {  
        StringBuilder sb = new StringBuilder();  
        for (int i = 0; i < level; i++) {  
            sb.append((i == level - 1) ? "|" + prefix : "|   ");  
        }  
        return sb.toString();
    }  
}
```

#### 4. 로그 추적기 적용
- 단계별로 로그 추적 로직을 적용한다. 
- 예외 발생 시 예외를 던지지 않으면 상위 호출자가 예외를 인지하지 못해 전체 흐름이 예상과 달라질 수 있기 때문에 주의해야 한다. 

```java
public class OrderController {
	..
	@GetMapping("/request")  
	public String request(String itemId) {  
	    TraceStatus status = null;  
	    try {
	        // 1. 로그 추적 시작 ( level = 0 )
	        status = trace.begin("OrderController.request()"); 
	        orderService.orderItem(status.getTraceId(), itemId);  
	        // 4-1. level 0 의 traceId 종료
			trace.end(status);  
	        return "ok";  
	    } catch (Exception e) {  
	        // 4-2. level 0 의 traceId 예외 발생
			trace.exception(status, e);  
	        throw e; // ★
	    }  
	}
}

public class OrderService {
	..
	public void orderItem(TraceId traceId, String itemId) {  
	    TraceStatus status = null;  
	    try {  
	        // 2. traceId 전달하여 동기화 ( level = 1 )
	        status = trace.beginSync(traceId, "OrderService.orderItem()"); 
	        orderRepository.save(status.getTraceId(), itemId);  
	        // 3-1. level 1 의 traceId 정상 종료
	        trace.end(status);  
	    } catch (Exception e) {  
	        // 3-2. level 1 의 traceId 예외 발생
	        trace.exception(status, e);
	        throw e; // ★
	    }  
	}
}
```

5. 실행 결과 
```console
[09715f46] OrderController.request()  
[09715f46] |-->OrderService.orderItem()  
[09715f46] |<--OrderService.orderItem() time=1001ms  
[09715f46] OrderController.request() time=1003ms
```

### 문제점
- HTTP 요청을 구분하고 깊이를 표현하기 위해 TraceId 동기화가 필요하다.
- TraceId 의 동기화를 위해 관련 메서드 및 인터페이스의 모든 파라미터를 수정해야 한다.
- 로그를 처음 시작할 때와 처음이 아닐 때 호출해야하는 메서드가 다르다. 

<br>

## 참고 
[인프런 - 스프링 핵심 원리 고급편](https://inf.run/FWeFN) 