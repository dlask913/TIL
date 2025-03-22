# (spring) 전략 패턴과 템플릿 콜백 패턴
> 템플릿 메서드 패턴를 적용한 로그 추적기의 상속 문제 해결, 전략 패턴, 템플릿 콜백 패턴

<br>

### 로그 추적기의 원본 코드 수정해야 하는 문제 해결하기
> - 템플릿 콜백 패턴으로 코드를 최소화 시켰음에도 원본 코드를 수정해야 한다는 문제가 있다.
> - 프록시를 적용하여 원본 코드를 수정하지 않고 로그 추적기를 적용한다.

#### ※ 참고: 스프링 부트 3.0 변경사항
- 3.0 미만에서는 `@Controller` 또는 `@RequestMapping` 이 있어야 스프링 컨트롤러로 인식한다.
- 3.0 이상부터는 `@Controller` 또는 `@RestConroller` 가 있어야 스프링 컨트롤러로 인식한다.

<br>

## 프록시
![image](https://github.com/user-attachments/assets/788008ab-67a4-44e4-aefb-25519759acd3)

- 일반적으로 클라리언트가 서버를 직접 호출하고 처리 결과를 직접 받는다.
- Proxy는 **대리인**을 의미하며, 클라이언트는 대리인을 통해 간접적으로 서버에 요청할 수 있다. 
- 객체에서 프록시가 되려면 클라이언트가 누구에게 요청했는지, 그 이후 과정을 몰라야 한다. 
- 서버와 프록시는 같은 인터페이스를 사용해야 하고 서버 객체를 프록시 객체로 변경해도 클라이언트는 코드 변경없이 동작이 가능해야한다. → DI 로 대체 가능
#### 주요 기능
1. 접근 제어 : 권한에 따른 접근 차단, 캐싱, 지연로딩
2. 부가 기능 추가 : 원래 서버가 제공하는 기능에 더해 부가 기능을 수행
#### 프록시 패턴과 데코레이터 패턴
- 둘 다 프록시를 사용하는 방법이지만 GOF 디자인 패턴에서는 이 둘을 의도에 따라 구분한다.
- 프록시 패턴 : 접근 제어 목적
- 테코레이터 패턴 : 새로운 기능 추가 목적

<br>

## 프록시 패턴
> 접근제어 - 캐싱 적용하기

#### 1. Server 와 Proxy 의 인터페이스 생성하기
```java
public interface Subject {  
    String operation();  
}
```

#### 2. Server 역할의 `RealSubejct` 구현체 생성하기 ( 실제 로직 수행 )
```java
@Slf4j  
public class RealSubject implements Subject {  
    @Override  
    public String operation() {  
        log.info("실제 객체 호출");  
        sleep(1000); // 캐싱 전후를 비교하기위해 1초 sleep  
        return "data";  
    }  
  
    private void sleep(int millis) {  
        try {  
            Thread.sleep(millis);  
        } catch (InterruptedException e) {  
            e.printStackTrace();  
        }  
    }  
}
```

#### 3. Proxy 역할의 `CacheProxy` 구현체 생성하기 ( 캐싱 )
```java
@Slf4j  
public class CacheProxy implements Subject {  
  
    private Subject target;  
    private String cacheValue;  
  
    public CacheProxy(Subject target) { // 서버와 동일한 인터페이스
        this.target = target;  
    }  
  
    @Override  
    public String operation() {  
        log.info("프록시 호출");  
        if (cacheValue == null) { // 최초 실행  
            cacheValue = target.operation(); // 실제 Server 호출
        }  
        // cacheValue 에 값이 있으면 실제 객체를 호출하지 않고 캐시 값을 그대로 반환 → 캐싱을 통한 빠른 조회  
        return cacheValue;  
    }  
}
```

#### 4. Client 코드에서 Server 로 요청하기 
```java
public class ProxyPatternClient {  
    private Subject subject;  
  
    public ProxyPatternClient(Subject subject) {
        this.subject = subject; // 인터페이스 주입 (구현체 모름)
    }  
  
    public void execute() {  
        subject.operation(); // 요청
    }  
}
```

#### 5. Client 코드 실행하여 동작 확인하기
```java
@Test  
void cacheProxyTest() {  
	/** Client → Proxy → Server **/
    RealSubject realSubject = new RealSubject();  
    CacheProxy cacheProxy = new CacheProxy(realSubject); // 서버 주입  
    ProxyPatternClient client = new ProxyPatternClient(cacheProxy); // 프록시 주입  
    client.execute(); // realSubject 호출 → sleep(1000), 결과를 캐시에 저장
    client.execute(); // cacheProxy 에서 즉시 반환 (0초)  
    client.execute(); // cacheProxy 에서 즉시 반환 (0초)  
}

/** 실행 결과
 * 21:24:29.117 CacheProxy - 프록시 호출
 * 21:24:29.121 RealSubject - 실제 객체 호출  
 * 21:24:30.129 CacheProxy - 프록시 호출  
 * 21:24:30.129 CacheProxy - 프록시 호출  
 */
```

<br>

## 데코레이터 패턴
> 부가 기능 - 실행 시간 측정하기 

#### 1. Server 와 Proxy 의 인터페이스 생성하기
```java
public interface Component {  
    String operation();  
}
```

#### 2. Server 역할의 `RealComponent` 구현체 생성하기 ( 실제 로직 수행 )
```java
@Slf4j  
public class RealComponent implements Component {  
    @Override  
    public String operation() {  
        log.info("RealComponent 실행");  
        return "data";  
    }
}
```

#### 3. Proxy 역할의 `TimeDecorator` 구현체 생성하기 ( 부가 기능 )
```java
@Slf4j  
public class TimeDecorator implements Component {  
    private Component component;  
  
    public TimeDecorator(Component component) { // 서버와 동일한 인터페이스
        this.component = component;  
    }  
  
    @Override  
    public String operation() {  
        log.info("TimeDecorator 실행");  
        long startTime = System.currentTimeMillis();  
        String result = component.operation(); // 실제 Server 호출  
        long endTime = System.currentTimeMillis();  
        long resultTime = endTime - startTime; // 실행 시간 측정 (부가기능)
        log.info("TimeDecorator 종료 resultTime={}ms", resultTime);  
        return result;
    }  
}
```

#### 4. Client 코드에서 Server 로 요청하기
```java
@Slf4j  
public class DecoratorPatternClient {  
    private Component component;  
  
    public DecoratorPatternClient(Component component) {  
        this.component = component; // 인터페이스 주입 (구현체 모름)
    }  
  
    public void execute() {  
        String result = component.operation(); // 요청
        log.info("result={}", result);  
    }  
}
```

#### 5. Client 코드 실행하여 동작 확인하기
```java
@Test  
void decorator() {
    /** Client → Proxy → Server **/
    Component realComponent = new RealComponent();  
    Component timeDecorator = new TimeDecorator(realComponent); // 서버 주입
    DecoratorPatternClient client = new DecoratorPatternClient(timeDecorator); // 프록시 주입  
    client.execute(); 
}

/** 실행 결과
 * TimeDecorator 실행  
 * RealComponent 실행  
 * TimeDecorator 종료 resultTime=1ms  
 * result=data 
 * */
```


### 프록시 체인
> Client → Proxy → Proxy → Server

- 프록시 객체를 추가하여 부가 기능을 계속 더할 수도 있다. 
```java
Component realComponent = new RealComponent();  
Component messageDecorator = new MessageDecorator(realComponent); // 프록시 객체 → 서버 주입  
Component timeDecorator = new TimeDecorator(messageDecorator); // 프록시 객체 → 프록시 주입  
DecoratorPatternClient client = new DecoratorPatternClient(timeDecorator); // 클라이언트 → 프록시 주입  
client.execute();
```
- 꾸며주는 역할을 하는 `Decorator` 들은 스스로 존재할 수 없기 때문에 내부에 호출 대상인 `componet` 를 가지고 있어야 한다.
- GOF 데코레이터 패턴을 참고하여 `Decorator` 추상 클래스를 생성하고 `component` 를 속성으로 하여 중복을 제거할 수도 있다. → Component 와 Decorator 구분

<br>

## 프록시 패턴과 데코레이터 패턴
![image](https://github.com/user-attachments/assets/d86b46e2-8409-4fe0-90cd-a7f90e042b15)

- 두 가지 패턴 모두 프록시를 사용한다.
- 모양과 쓰이는 상황이 유사하기 때문에 의도에 따라 구분해야 한다.
- **프록시 패턴** 의도 : 다른 개체에 대한 접근을 제어하기 위해 대리자를 제공
- **데코레이터 패턴** 의도 : 객체에 추가 기능을 동적으로 추가하고 기능 확장을 위한 유연한 대안 제공

<br>

## 참고 
[인프런 - 스프링 핵심 원리 고급편](https://inf.run/FWeFN) 