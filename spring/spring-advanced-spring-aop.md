# (spring) 스프링 AOP
> 스프링 AOP 적용 방식, AOP 용어 정리

<br>

## 스프링 AOP 
- 부가 기능을 핵심 기능에서 분리하고 부가 기능과 부가 기능을 어디에 적용할 지 선택하는 기능을 합해서 하나의 모듈로 만들었는데 이것이 바로 Aspect 이다. → `@Aspect`
- 스프링이 제공하는 Advisor 도 어드바이스(부가 기능) 과 포인트컷(적용 대상) 을 가지고 있어서 개념상 하나의 Aspect 이다. 
- Aspect 는 애플리케이션을 바라보는 관점을 하나의 기능에서 횡단 관심사 관점으로 달리 보는 것으로, Aspect 를 사용한 프로그래밍 방식을 **관점 지향 프로그래밍 ( Aspect-Oriented Programming )** 이라고 한다.
- AOP 는 OOP 를 대체하기 위한 것이 아니라 횡단 관심사를 깔끔하게 처리하기 어려운 OOP 의 부족한 부분을 보조하는 목적으로 개발되었다.

#### 횡단 관심사 ( cross-cutting concerns )
- 애플리케이션 전반에 로그를 남기는 기능은 특정 기능 하나에 관심이 있는 기능이 아니라 애플리케이션의 여러 기능들 사이에 걸쳐 들어가는 관심사인데, 이것을 횡단 관심사라고 한다. 

<br>

### AOP 적용 방식
#### 1. 컴파일 시점 ( 컴파일 타임 - 위빙 )
- `.java` 소스 코드를 컴파일러를 사용해서 `.class` 를 만드는 시점에 부가 기능 로직을 추가할 수 있다. ( 디컴파일 해보면 Aspect 관련 호출 코드 확인 가능 )
- AspectJ 가 제공하는 특별한 컴파일러를 사용해야 하며, AspectJ 컴파일러는 Aspect 를 확인해서 해당 클래스가 적용 대상인지 먼저 확인하고 적용 대상인 경우 부가 기능 로직을 적용한다.
- 실제 대상 코드에 Aspect 를 통한 부가 기능 호출 코드가 포함되며, AspectJ 를 직접 사용해야 한다. 
- 단점 : 컴파일 시점에 부가 기능을 적용하려면 특별한 컴파일러도 필요하고 복잡하다. 

#### 2. 클래스 로딩 시점 ( 로드 타임 - 위빙 )
- 자바를 실행하면 자바 언어는 `.class` 파일을 JVM 내부 클래스 로더에 보관하는데 이 때 중간에서 `.class` 파일을 조작한 다음 JVM 에 올릴 수 있다. 
- 자바는 `.class` 를 JVM 에 저장하기 전에 조작할 수 있는데 수 많은 모니터링 툴들이 이 방식을 사용한다. ( java instrumentation )
- 실제 대상 코드에 Aspect 를 통한 부가 기능 호출 코드가 포함되며, AspectJ 를 직접 사용해야 한다. 
- 단점 : 자바를 실행할 때 특별한 옵션 ( `java -javaagent` ) 를 통해 클래스 로더 조작기를 지정해야 하는데 이 부분이 번거롭고 운영하기 어렵다. 

#### 3. 런타임 시점 ( 런타임 - 위빙 )
- 런타임 시점은 컴파일도 다끝나고 클래스 로더에 클래스도 다 올라가서 이미 자바가 실행되고 난 다음으로, 자바의 메인 메서드가 이미 실행된 다음이다. 
- 스프링 컨테이너의 도움을 받고 프록시와 DI, 빈 포스트 프로세서 같은 개념들을 활용하여 프록시를 통해 스프링 빈에 부가 기능을 적용할 수 있다. → 프록시 방식의 AOP 
- 프록시를 사용하기 때문에 **메서드로 실행으로 제한된다**는 제약이 있지만 복잡한 옵션과 클래스 로더 조작기를 설정하지 않아도 된다. 
- 실제 대상 코드는 그대로 유지되며 프록시를 통해야 부가 기능을 사용할 수 있다. 

#### AOP 적용 위치
- AOP 는 메서드 실행 위치 뿐 아니라 생성자, 필드 값 접근, static 메서드 접근, 메서드 실행 위치에도 적용할 수 있다. ( 조인 포인트 )
- AspectJ 를 사용해서 컴파일 시점과 클래스 로딩 시점에 적용하는 AOP 는 바이트 코드를 실제 조작하기 때문에 모든 지점에 다 적용할 수 있다. 
- AspectJ 를 사용하면 더 복잡하고 다양한 기능을 사용할 수 있지만 공부할 내용도 많고 자바 관련 설정이 복잡하다. 
- 스프링 AOP 는 별도 추가 자바 설정 없이 스프링만 있으면 편리하게 AOP 를 사용할 수 있다. 

<br>

### AOP 용어 정리
![image](https://github.com/user-attachments/assets/f3c11ec7-11a1-4760-b43b-a62c2ae33c1f)


#### 조인 포인트 ( Join point )
- 어드바이스가 적용될 수 있는 위치, 메소드 실행, 생성자 호출, 필드 값 접근, static 메서드 접근 같은 프로그램 실행 중 지점
- 조인 포인트는 추상적인 개념으로, AOP 를 적용할 수 있는 모든 지점이다.
#### 포인트컷 ( Pointcut )
- 조인 포인트 중에서 어드바이스가 적용될 위치를 선별하는 기능
- 주로 AspectJ 표현식을 사용해서 지정한다.
#### 타겟 ( Target )
- 어드바이스를 받는 객체, 포인트컷으로 결정된다. 
#### 어드바이스 ( Advice )
- 부가 기능으로, 특정 조인 포인트에서 Aspect 에 의해 취해지는 조치이다.
- Around(주변), Before(전), After(후) 와 같은 다양한 종류의 어드바이스가 있다. 
#### 애스펙트 ( Aspect )
- 어드바이스 + 포인트컷을 모듈화 한 것으로 @Aspect 를 생각하면 된다. 
- 여러 어드바이스와 포인트 컷이 함께 존재한다.
#### 위빙 ( Weaving )
- 포인트컷으로 결정한 타겟의 조인 포인트에 어드바이스를 적용하는 것을 말한다. 
- 위빙을 통해 핵심 기능 코드에 영향을 주지 않고 부가 기능을 추가 할 수 있다. 
#### AOP 프록시
- AOP 기능을 구현하기 위해 만든 프록시 객체
- 스프링에서 AOP 프록시는 JDK 동적 프록시 또는 CGLIB 프록시이다. 

<br>

## 참고 
[인프런 - 스프링 핵심 원리 고급편](https://inf.run/FWeFN) 