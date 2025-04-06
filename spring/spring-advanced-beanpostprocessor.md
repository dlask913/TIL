# (spring) 빈 후처리기
> 컴포넌트 스캔 사용 시 프록시 적용 불가 문제 해결하기, 빈 후처리기

<br>

### 컴포넌트 스캔 사용 시 프록시 적용 불가 문제 해결하기
> - ProxyFactoryConfig 에서와 같이 무수히 많은 동적 프록시 생성 코드를 만들어야 한다는 문제가 있다.
> - 컴포넌트 스캔을 사용하면 스프링에서 실제 객체를 이미 컨테이너에 빈으로 등록을 다 해버린 상태이기 때문에 프록시 적용이 불가능하다.
> - 빈 후처리기를 사용하여 해결한다.

<br>

## 빈 후처리기 ( `BeanPostProcessor` )
- `@Bean` 이나 컴포넌트 스캔으로 스프링 빈을 등록하면 스프링은 대상 객체를 생성하고 스프링 컨테이너 내부의 빈 저장소에 등록한다. 
- 빈 후처리기는 빈을 생성한 후에 무언가를 처리하는 용도로, 객체를 조작하거나 완전히 다른 객체로 바꿔치기 하는 것이 가능하다.

### 등록 과정
![image](https://github.com/user-attachments/assets/5dbaf74b-5c2c-40c1-b7c6-cb8fc547f882)


1. 생성 : 스프링 빈 대상이 되는 객체 생성 ( `@Bean`, 컴포넌트 스캔 모두 포함 )
2. 전달 : 생성된 객체를 빈 저장소에 등록하기 직전에 빈 후처리기에 전달
3. 후 처리 작업 : 빈 후처리기를 통해 전달된 스프링 빈 객체를 조작하거나 다른 객체로 바꿔치기 할 수 있다.
4. 등록 : 빈 후처리기에서 반환된 빈이 빈 저장소에 등록된다.

<br>

### 바꿔치기 예제 
> 빈 후처리기를 통해 A 객체를 B 객체로 바꿔치기 해본다.

#### BeanPostProcessor 인터페이스 - 스프링 제공
- 빈 후처리기를 사용하려면 BeanPostProcessor 인터페이스를 구현하고 스프링 빈으로 등록해야 한다.
- `postProcessBeforeInitialization` : 객체 생성 이후에 `@PostConstruct` 같은 초기화가 발생하기 전에 호출되는 포스트 프로세서
- `postProcessAfterInitialization` : 객체 생성 이후에 `@PostConstruct` 같은 초기화가 발생한 다음에 호출되는 포스트 프로세서
```java
public interface BeanPostProcessor {  
    @Nullable  
    default Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {  
        return bean;  
    }  
    @Nullable  
    default Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {  
        return bean;  
    }  
}
```

#### 1. 바꿔치기를 위한 AToBPostProcessor 생성하기
- `BeanPostProcessor` 를 구현하고 스프링 빈으로 등록하면 스프링 컨테이너가 빈 후처리기로 인식하고 동작한다.
```java
@Slf4j  
static class AToBPostProcessor implements BeanPostProcessor {  
  
    @Override  
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {  
        log.info("beanName={} bean={}", beanName, bean);  
        if (bean instanceof A) { // bean 객체가 A 인스턴스이면 새로운 B 객체 반환
            return new B(); // → 스프링 컨테이너에 등록
        }  
        return bean;  
    }  
}

@Slf4j  
static class A {  
    public void helloA() {  
        log.info("hello A");  
    }  
}  
  
@Slf4j  
static class B {  
    public void helloB() {  
        log.info("hello B");  
    }  
}
```

#### 2. 실행 결과
- beanA 라는 스프링 빈 이름에 A 객체 대신 B 객체가 등록된 것을 확인할 수 있다.
- 일반적으로 스프링 컨테이너가 등록하는, 특히 컴포넌트 스캔의 대상이 되는 빈들은 중간에 조작할 방법이 없는데 이를 통해 **개발자가 등록하는 모든 빈을 중각에 조작하거나 프록시로 교체**할 수도 있다. 
```java
@Test  
void basicConfig() {  
    ApplicationContext applicationContext = new AnnotationConfigApplicationContext(BeanPostProcessorConfig.class);  
  
    B b = applicationContext.getBean("beanA", B.class); //beanA 이름으로 B 객체가 빈으로 등록  
    b.helloB();  
  
    Assertions.assertThrows(NoSuchBeanDefinitionException.class, () -> applicationContext.getBean(A.class));  
}

/**
BeanPostProcessorTest$AToBPostProcessor - beanName=beanA bean=hello.proxy.postprocessor.BeanPostProcessorTest$A@1c9f0a20
BeanPostProcessorTest$B - hello B
**/
```

#### @PostConstruct 참고사항
- `@PostConsturct`  는 스프링 빈 생성 이후에 빈을 초기화하는 역할로, `@PostConstruct `애노테이션이 붙은 초기화 메서드를 한 번 호출만 하면 된다. ( 한 번 조작 )
- 스프링은 `CommonAnnotationBeanPostProcessor` 라는 빈 후처리기를 자동으로 등록하는 여기에서 `@PostConstruct` 애노테이션이 붙은 메서드를 호출한다. → 스프링 내부의 기능을 확장하기 위해 스프링 스스로도 사용

<br>

### LogTrace 에 빈 후처리기 적용하기 
> 빈 후처리기를 사용해 **수동으로 등록하는 빈과 컴포넌트 스캔을 사용하는 모든 빈**까지 프록시를 등록한다.

#### 1. LogTraceProxyPostProcessor 생성하기
- 원본 객체를 프록시 객체로 변환하는 PostProcessor 를 생성한다. 
- 프록시 팩토리를 사용하기 때문에 advisor 를 외부에서 주입받는다.
- basePackage 를 통해 프록시를 적용할 패키지를 지정하여 그 하위에 위치한 빈들만 프록시를 적용하도록 한다. ( PointCut 적용 가능 )
```java
@Slf4j  
public class PackageLogTracePostProcessor implements BeanPostProcessor {  
  
    private final String basePackage;  
    private final Advisor advisor;  
  
    public PackageLogTracePostProcessor(String basePackage, Advisor advisor) {  
        this.basePackage = basePackage;  
        this.advisor = advisor;  
    }  
  
    @Override  
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {  
        log.info("param beanName={} bean={}", beanName, bean.getClass());  
  
        // 1. 프록시 적용 대상 여부 체크( 프록시 적용 대상이 아니면 원본을 그대로 진행 )        
        String packageName = bean.getClass().getPackageName();  
        if (!packageName.startsWith(basePackage)) {  
            return bean;  
        }  
  
        // 2. 프록시 대상이면 프록시를 만들어서 반환  
        ProxyFactory proxyFactory = new ProxyFactory(bean);  
        proxyFactory.addAdvisor(advisor);  
  
        Object proxy = proxyFactory.getProxy();  
        log.info("create proxy: target={} proxy={}", bean.getClass(), proxy.getClass());  
        return proxy; // ★ 프록시 반환
    }  
}
```

#### 2. PostProcessor 빈 등록하기
- 특정 패키지를 기준으로 프록시를 생성하는 `logTracePostProcessor` 빈 후처리기를 스프링 빈으로 등록한다. 
- 이제 프록시를 생성하는 코드가 설정 파일에 필요 없어진다. (★)
```java
@Slf4j  
@Configuration  
public class BeanPostProcessorConfig {  
  
    @Bean  
    public PackageLogTracePostProcessor logTracePostProcessor(LogTrace logTrace) {  
        return new PackageLogTracePostProcessor("hello.proxy.app", getAdvisor(logTrace)); // (basePackage, advisor)
    }  
  
    private Advisor getAdvisor(LogTrace logTrace) {  
        //pointcut  
        NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();  
        pointcut.setMappedNames("request*", "order*", "save*");  
        //advice  
        LogTraceAdvice advice = new LogTraceAdvice(logTrace);  
        return new DefaultPointcutAdvisor(pointcut, advice);  
    }  

	@Bean  
	public LogTrace logTrace() {  
	    return new ThreadLocalLogTrace();  
	}
}
```

#### 3. 실행 결과
- 컴포넌트 스캔을 통해 등록한 빈들도 ( `@RestController`, `@Service`, `@Repository` ) 프록시가 적용됨을 확인할 수 있다. 
- 패키지를 제한하지 않으면 직접 등록한 스프링 빈들 뿐 아니라 스프링 부트가 기본으로 등록하는 수 많은 빈들이 넘어오기 때문에 제한이 필요하다.
```console
[4cdaa8d4] OrderController.request()
[4cdaa8d4] |-->OrderService.orderItem()
[4cdaa8d4] |   |-->OrderRepository.save()
[4cdaa8d4] |   |<--OrderRepository.save() time=1017ms
[4cdaa8d4] |<--OrderService.orderItem() time=1024ms
[4cdaa8d4] OrderController.request() time=1034ms
```

<br>

## 문제 해결
- 너무 많은 설정 파일을 요구한다는 점과 컴포넌트 스캔을 사용하는 경우 프록시 적용이 불가했던 문제가 해결되었다. 
- 또한, 스프링은 프록시를 생성하기 위한 빈 후처리기를 만들어서 제공한다.

<br>

## 참고 
[인프런 - 스프링 핵심 원리 고급편](https://inf.run/FWeFN) 