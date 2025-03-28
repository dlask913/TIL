# (spring) 인터페이스 프록시와 구현체 프록시
> 로그 추적기의 원본 코드 수정해야 하는 문제 해결하기, 인터페이스 프록시와 구현체 프록시

<br>

### 로그 추적기의 원본 코드 수정해야 하는 문제 해결하기
> - 프록시를 적용하여 원본 코드를 수정하지 않고 로그 추적기를 적용한다.

<br>

## 인터페이스 프록시
- 로그 추적기에 인터페이스 프록시를 적용하기 위해 Controller, Service, Repostiory 각각 인터페이스에 맞는 프록시 구현체를 추가해야 한다. 
- 또한, 애플리케이션 실행 시점에 프록시를 사용하도록 의존 관계 설정을 해주어야 한다.

### 로그 추적기에 적용해보기 ( Service 예시 )
#### 1. Service 인터페이스와 구현체 생성하기
```java
// Service 인터페이스 생성
public interface OrderService {  
    void orderItem(String itemId);  
}

// Service 구현체 생성
@RequiredArgsConstructor  
public class OrderServiceImpl implements OrderService {  
    private final OrderRepository orderRepository;  
  
    @Override  
    public void orderItem(String itemId) {  
        orderRepository.save(itemId);  
    }  
}
```

#### 2. `OrderServiceInterfaceProxy` 생성하여 인터페이스 구현
```java
@RequiredArgsConstructor  
public class OrderServiceInterfaceProxy implements OrderService {  
  
    private final OrderService target;  
    private final LogTrace logTrace;  
  
    @Override  
    public void orderItem(String itemId) {
        /** 로그 추적기 로직 ( 부가 기능 ) **/
        TraceStatus status = null;  
        try {  
            status = logTrace.begin("OrderService.orderItem()");  
            target.orderItem(itemId);  //  실제 로직 호출
            logTrace.end(status);  
        } catch (Exception e) {  
            logTrace.exception(status, e);  
            throw e;  
        }  
    }  
}
```

#### 3. 프록시 객체 의존 관계 설정
- 실제 객체가 반환되지 않도록 프록시를 실제 스프링 빈 대신 등록한다.
- 프록시는 내부에 실제 객체를 참조하기 때문에 프록시를 통해 실제 객체를 호출할 수 있다. 
```java
@Configuration  
public class InterfaceProxyConfig {  
  
    @Bean  
    public OrderService orderService(LogTrace logTrace) {  
        OrderServiceImpl serviceImpl = new OrderServiceImpl(orderRepository(logTrace));  
        return new OrderServiceInterfaceProxy(serviceImpl, logTrace);  
    }
    ..
}
```

<br>

### 스프링 컨테이너 내 빈 저장소
- 빈 이름 : orderController, 빈 객체: OrderControllerInterfaceproxy@x04 -참조-> OrderControllerV1Impl@x01
- 빈 이름 : orderService, 빈 객체 : OrderServiceInterfaceProxy@x05 -참조-> OrderServiceV1Impl@x02
- 런타임 객체 의존 관계
![Image](https://github.com/user-attachments/assets/bf9731f2-b5e2-486a-9d59-6b10436413f4)

<br>

## 구체 클래스 기반 프록시
- 자바의 다형성은 인터페이스를 구현하든 클래스를 상속하든 상위 타입만 맞으면 다형성이 적용되기 때문에 인터페이스가 아니어도 프록시가 가능하다.
- 인터페이스 없이 클래스를 기반으로 상속받아 구체 클래스 기반 프록시를 생성한다.
- 동일하게 애플리케이션 실행 시점에 프록시를 사용하도록 의존 관계 설정을 해주어야 한다.

### 로그 추적기에 적용해보기 ( Service 예시 )
#### 1. Service 인터페이스와 구현체 생성하기 ( 인터페이스 프록시와 동일)
```java
// Service 인터페이스 생성
public interface OrderService {  
    void orderItem(String itemId);  
}

// Service 구현체 생성
@RequiredArgsConstructor  
public class OrderServiceImpl implements OrderService {  
    private final OrderRepository orderRepository;  
  
    @Override  
    public void orderItem(String itemId) {  
        orderRepository.save(itemId);  
    }  
}
```

#### 2. `OrderServiceConcreteProxy` 생성하여 인터페이스 상속
- 자바 문법에 의해 자식 클래스를 생성할 때는 항상 super() 로 부모 클래스의 생성자를 호출해야 하며, 생략하면 부모 클래스의 기본 생성자가 호출된다.
```java
public class OrderServiceConcreteProxy extends OrderService {  
  
    private final OrderService target;  
    private final LogTrace logTrace;  
  
    public OrderServiceConcreteProxy(OrderService target, LogTrace logTrace) {  
        super(null); // 부모 클래스의 생성자 호출
        this.target = target;  
        this.logTrace = logTrace;  
    }  
  
    @Override  
    public void orderItem(String itemId) {  
        TraceStatus status = null;  
        try {  
            status = logTrace.begin("OrderService.orderItem()");  
            target.orderItem(itemId); // 실제 로직 호출
            logTrace.end(status);  
        } catch (Exception e) {  
            logTrace.exception(status, e);  
            throw e;  
        }  
    }  
}
```

#### 3. 프록시 객체 의존 관계 설정
- 실제 객체가 반환되지 않도록 구체 프록시를 실제 스프링 빈 대신 등록한다.
```java
@Configuration  
public class InterfaceProxyConfig {  
  
	@Bean  
	public OrderService orderService(LogTrace logTrace) {  
	    OrderService serviceImpl = new OrderService(orderRepository(logTrace)); 
	    return new OrderServiceConcreteProxy(serviceImpl, logTrace);  
	}
    ..
}
```

<br>

## 인터페이스 기반 프록시와 클래스 기반 프록시 
- 프록시를 사용함으로서 원본 코드를 전혀 변경하지 않고 로그 추적기 기능을 적용할 수 있었다. 
- 인터페이스를 사용하면 역할과 구현을 명확하게 나눈다는 장점이 있다.
- 만약 구현을 변경할 가능성이 거의 없는 경우엔 인터페이스 사용이 번거롭기 때문에 구체 클래스를 바로 사용할 수도 있다. → 인터페이스가 항상 필요하지는 않다. 

### 문제점
- 프록시를 사용해서 기존 코드 변경 없이 기능을 적용할 수 있었지만 너무 많은 프록시를 생성해야 한다. 
- 프록시를 하나만 만들어서 적용할 수 있는 동적 프록시 기술을 사용해서 해결할 수 있다. 

<br>

## 참고 
[인프런 - 스프링 핵심 원리 고급편](https://inf.run/FWeFN) 