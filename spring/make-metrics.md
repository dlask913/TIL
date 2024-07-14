## (spring) 모니터링 메트릭 활용하기
> 메트릭 정의하여 Counter, Timer, Gauge 메트릭 만들어 활용하기.

<br>

### 메트릭 정의
1. 주문수, 취소수
- 계속 증가하므로 카운터를 사용
2. 재고 수량 
- 증가하거나 감소하므로 게이지 사용

<br>

## 카운터 메트릭 등록하기
: 마이크로미터를 사용해서 주문수, 취소수를 대상으로 카운터 메트릭을 등록하자.

### Counter ( [공식문서](https://prometheus.io/docs/concepts/metric_types/) )
- 단조롭게 증가하는 단일 누적 측정 항목
- 값을 증가하거나 0으로 초기화 하는 것만 가능하다.
- 마이크로미터에서 값을 감소하는 기능도 지원하지만 목적에 맞지 않다.
- 프로메테우스에서는 일반적으로 카운터의 이름 마지막에 ```_total``` 을 붙여서 ```my_order_total```  과 같이 표현한다. <br>
 ex> HTTP 요청수

### Counter 메트릭 등록하기 
- Counter.builder("메트릭 이름") 을 통해 카운터를 생성한다.
- tag 는 프로메테우스에서 필터링할 수 있는 레이블로 사용된다.
- 주문과 취소는 메트릭 이름은 같고 tag 를 통해 구분한다.
- ``register(registry)`` : 만든 카운터를 MeterRegistry 에 등록하여 실제 동작하게 한다.
- ``increment()`` : 카운터의 값을 하나 증가시킨다.
#### OrderServiceV1
- MeterRegistry : 마이크로미터 기능을 제공하는 핵심 컴포넌트, 스프링을 통해 주입 받아 사용하고 이걸 통해서 카운터 및 게이지 등을 등록한다. 
```java
@Slf4j  
public class OrderServiceV1 implements OrderService {  
  
    private final MeterRegistry registry;  
    private AtomicInteger stock = new AtomicInteger(100);  
    public OrderServiceV1(MeterRegistry registry) {  
        this.registry = registry;  
    }  
  
    @Override  
    public void order() {  
        log.info("주문");  
        stock.decrementAndGet();  
  
        Counter.builder("my.order")  // 메트릭 등록
                .tag("class", this.getClass().getName())  
                .tag("method", "order")  
                .description("order")  
                .register(registry).increment();  
  
    }  
  
    @Override  
    public void cancel() {  
        log.info("취소");  
        stock.incrementAndGet();  
  
        Counter.builder("my.order")  // 메트릭 등록
                .tag("class", this.getClass().getName())  
                .tag("method", "cancel")  
                .description("order")  
                .register(registry).increment();  
    }  
  ..
}
```
#### OrderConfigV1
```java
 @Configuration  
public class OrderConfigV1 {  
    @Bean  
    OrderService orderService(MeterRegistry registry) {  
        return new OrderServiceV1(registry);  
    }  
}
```

### @Counted 로 Counter 등록하기
: @Counted 를 사용하면 비즈니스 로직 내에 메트릭을 등록하는 로직을 추가할 필요가 없다.
#### OrderServiceV2
```java
import io.micrometer.core.annotation.Counted;
..
@Slf4j  
public class OrderServiceV2 implements OrderService {  
    private AtomicInteger stock = new AtomicInteger(100);  
    
    // @Counted 를 사용하면 result, exception, method, class 같은 다양한 tag 를 자동으로 적용한다.  
    @Counted("my.order")  
    @Override  
    public void order() {  
        log.info("주문");  
        stock.decrementAndGet();  
    }  
  
    @Counted("my.order")  
    @Override  
    public void cancel() {  
        log.info("취소");  
        stock.incrementAndGet();  
    }  
    ..  
}
```
#### OrderConfigV2
```java
@Configuration  
public class OrderConfigV2 {  
    @Bean  
    public OrderService orderService() {  
        return new OrderServiceV2();  
    }  
    
    // CountedAspect 를 등록하면 @Counted 를 인지해서 Counter 를 사용하는 AOP 를 적용한다. 
    // CountedAspect 를 빈으로 등록하지 않으면 @Counted 관련 AOP 가 동작하지 않는다.  
    @Bean  
    public CountedAspect countedAspect(MeterRegistry registry) {  
        return new CountedAspect(registry);  
    }  
}
```


 ### 실행 결과
- 각 엔드포인트를 호출해보고, metrics 을 확인한다. ( 최소 한 번은 호출해야 메트릭이 나온다. ) <br>
: CountedAspect 를 적용하면 tag 중에 exceptions 이 추가된다. <br>
ex> http://localhost:8080/actuator/metrics/my.order?tag=method:cancel 

- 프로메테우스 포맷 메트릭 확인
> 프로메테우스는 . → _ 으로 변경 <br>
> 프로메테우스는 관례상 카운터 이름의 끝에 _total 을 붙인다. <br>

![image](https://github.com/user-attachments/assets/ec3b7192-d803-4eac-8ed1-2b58251ebf21)

### 그라파나 패널 추가하기
: 카운터는 계속 증가하기 때문에 특정 시간에 얼마나 증가했는지 확인하려면 increase(), rate() 같은 함수와 함께 사용하는 것이 좋다.
- 주문수 쿼리 추가
: increase(my_order_total{method="order"}[1m]) 
- 취소수 쿼리 추가
: increase(my_order_total{method="cancel"}[1m]) 

<br>

## Timer 메트릭 등록하기

### Timer
- Counter 와 유사한데 Timer 를 사용하면 실행 시간도 함께 측정할 수 있다. 

> **측정 가능 메트릭**
> -  seconds_count : 누적 실행 수 ( 카운터 ) 
 >- seconds_sum : 실행 시간의 합 ( sum )
 >- seconds_max : 최대 실행 시간 ( 가장 오래 걸린 실행 시간, 게이지 ) 으로, 내부에 타임 윈도우라는 개념이 있어서 1~3분마다 최대 실행 시간이 다시 계산된다. 

### Timer 메트릭 등록하기
#### OrderServiceV3
```java
@Slf4j  
public class OrderServiceV3 implements OrderService {  
    private final MeterRegistry registry;  
    private AtomicInteger stock = new AtomicInteger(100);  
    public OrderServiceV3(MeterRegistry registry) {  
        this.registry = registry;  
    }  
  
    @Override  
    public void order() {  
        Timer timer = Timer.builder("my.order")  // Timer 등록
                .tag("class", this.getClass().getName())  
                .tag("method", "order")  
                .description("order")  
                .register(registry);  
  
        timer.record(()-> {  
            log.info("주문");  
            stock.decrementAndGet();  
            sleep(500);  
        });  
    }  
  
    @Override  
    public void cancel() {  
        Timer timer = Timer.builder("my.order")  // Timer 등록
                .tag("class", this.getClass().getName())  
                .tag("method", "cancel")  
                .description("order")  
                .register(registry);  
  
        timer.record(() -> {  
            log.info("취소");  
            stock.incrementAndGet();  
            sleep(200);  
        });  
    }  
  
    private static void sleep(int l) {  
        try {  
            Thread.sleep(l + new Random().nextInt(200));  
        } catch (InterruptedException e) {  
            throw new RuntimeException(e);  
        }  
    }
    ..  
}
```
#### OrderConfigV3
```java
@Configuration  
public class OrderConfigV3 {  
    @Bean  
    OrderService orderService(MeterRegistry registry) {  
        return new OrderServiceV3(registry);  
    }  
}
```

### @Timed 로 Timer 등록하기
#### OrderServiceV4
```java
import io.micrometer.core.annotation.Timed;

// 타입이나 메서드 중에 적용할 수 있다. 
// 타입에 적용하면 해당 타입의 모든 public 메서드에 타이머가 적용된다. 
@Timed("my.order")  
@Slf4j  
public class OrderServiceV4 implements OrderService {  
    private AtomicInteger stock = new AtomicInteger(100);  
  
    @Override  
    public void order() {  
        log.info("주문");  
        stock.decrementAndGet();  
        sleep(500);  
    }  
  
    @Override  
    public void cancel() {  
        log.info("취소");  
        stock.incrementAndGet();  
        sleep(200);  
    }  

    private static void sleep(int l) { // 실행 시간 측정  
        try {  
            Thread.sleep(l + new Random().nextInt(200));  
        } catch (InterruptedException e) {  
            throw new RuntimeException(e);  
        }  
    }  
    ..
}
```
#### OrderConfigV4
```java
import io.micrometer.core.aop.TimedAspect;

@Configuration  
public class OrderConfigV4 {  
    @Bean  
    OrderService orderService() {  
        return new OrderServiceV4();  
    }  
    @Bean  
    public TimedAspect timedAspect(MeterRegistry registry) {  
        return new TimedAspect(registry);  
    }  
}
```

### 실행 결과
- actuator 메트릭 확인하기  ( http://localhost:8080/actuator/metrics/my.order )
: TimedAspect 를 적용하면 tag 중에 exceptions 이 추가된다.
```json
{
  "name": "my.order",
  "description": "order",
  "baseUnit": "seconds",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 3
    },
    {
      "statistic": "TOTAL_TIME",
      "value": 1.3025768
    },
    {
      "statistic": "MAX",
      "value": 0.5297637
    }
  ],
  "availableTags": [
    {
      "tag": "method",
      "values": [
        "cancel",
        "order"
      ]
    },
    {
      "tag": "class",
      "values": [
        "hello.order.v3.OrderServiceV3"
      ]
    }
  ]
}
```

-  프로메테우스 메트릭 보기
> seconds_sum / seconds_count 으로 평균 실행 시간을 구할 수도 있다.
![image](https://github.com/user-attachments/assets/1b7f304b-a914-4c96-b2a8-46103567185d)

### 그라파나 패널 추가하기
- 최대 실행시간
: my_order_seconds_max
- 평균 실행시간 구간별로 보기
: increase(my_order_seconds_sum[1m]) / increase(my_order_seconds_count[1m])

<br>

## Gauge 메트릭 등록하기
### 게이지 ( [공식문서](https://prometheus.io/docs/concepts/metric_types/) )
- 게이지는 임의로 오르내릴 수 있는 단일 숫자 값을 나타내는 메트릭
- 값의 현재 상태를 보는데 사용
- 값이 증가하거나 감소할 수 있다.
ex> 차량의 속도, CPU 사용량, 메모리 사용량
** 카운터와 게이지를 구분할 때는 값이 감소할 수 있는가를 고민해보면 된다.

### Gauge 메트릭 등록하기
- 게이지를 만들 때 전달한 함수는 외부에서 메트릭을 확인할 때 마다 호출되며, 이 함수의 반환 값이 게이지 값이 된다. 
- 프로메테우스가 /actuator/prometheus 를 통해 주기적으로 메트릭을 확인하기 때문에 주기적으로 찍히는 로그를 확인할 수 있다. 
#### OrderServiceV4
```java
@Timed("my.order")  
@Slf4j  
public class OrderServiceV4 implements OrderService {  
    
    private AtomicInteger stock = new AtomicInteger(100);  
  
    @Override  
    public AtomicInteger getStock() {  
        return stock;  
    }
    ..
}
```
#### StockConfigV1 
```java
@Configuration  
public class StockConfigV1 {  
  
    @Bean  
    public MyStockMetric myStockMetric(OrderService orderService, MeterRegistry registry){  
        return new MyStockMetric(orderService, registry);  
    }  
  
    @Slf4j  
    static class MyStockMetric {  
        private OrderService orderService;  
        private MeterRegistry registry;  
  
        public MyStockMetric(OrderService orderService, MeterRegistry registry) {  
            this.orderService = orderService;  
            this.registry = registry;  
        }  
  
        @PostConstruct  
        public void init() {  
            Gauge.builder("my.stock", orderService, service -> {  
                log.info("stock gauge call");  
                return service.getStock().get();  // 게이지 값
            }).register(registry);  
        }  
    }  
}
```
#### StockConfigV2  ( MeterBinder 로 간편하게 등록하기 )
```java
@Slf4j  
@Configuration  
public class StockConfigV2 {  
  
    @Bean  
    public MeterBinder stockSize(OrderService orderService) {  
        return registry -> Gauge.builder("my.stock", orderService, service -> {  
            log.info("stock gauge call");  
            return service.getStock().get();  
        }).register(registry);  
    }  
}
```


### 실행 결과
- 주기적으로 콘솔창에 찍히는 로그 확인하기
```console
2024-07-14T21:37:15.499+09:00  INFO 45060 --- [nio-8080-exec-3] hello.order.gauge.StockConfigV2          : stock gauge call
2024-07-14T21:37:16.510+09:00  INFO 45060 --- [nio-8080-exec-4] hello.order.gauge.StockConfigV2          : stock gauge call
2024-07-14T21:37:17.499+09:00  INFO 45060 --- [nio-8080-exec-5] hello.order.gauge.StockConfigV2          : stock gauge call
2024-07-14T21:37:18.495+09:00  INFO 45060 --- [nio-8080-exec-6] hello.order.gauge.StockConfigV2          : stock gauge call
```
- actuator 메트릭 확인하기
![image](https://github.com/user-attachments/assets/4678d0b3-ab9f-4449-a1b7-b05674d8abdd)


### 그라파나 패널 추가하기
- 재고 수 확인하기
: my_stock


<br>

## 참고 
[인프런 - 스프링 부트 핵심 원리와 활용](https://inf.run/7VBBx) 