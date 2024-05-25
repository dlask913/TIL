## (spring) 마이크로미터와 다양한 메트릭
> 마이크로미터와 다양한 메트릭 확인하기.

<br>

## 마이크로미터
: 마이크로미터는 애플리케이션 메트릭 파사드로, 애플리케이션의 메트릭(측정 지표)을 표준화된 방법으로 수집하고 제공하는 역할을 한다. 추상화를 통해 구현체를 쉽게 교체할 수 있으며 개발자는 마이크로미터 표준을 따르고 사용하는 모니터링 툴에 맞는 구현체를 선택하면 된다.

<br>

## 메트릭 확인하기
CPU, JVM, 커넥션 사용 등의 지표를 개발자가 수집하여 마이크로미터 표준에 따라 등록하면 된다. 마이크로미터는 다양한 지표 수집 기능을 제공하며, 스프링 부트 액추에이터는 마이크로미터가 제공하는 지표 수집 @AutoConfiguration 을 통해 자동으로 등록해준다.

#### - 접속해서 확인하기 ( /actuator/metrics )
: jvm 메모리 사용량( jvm.memory.used )이나 http 요청 수( http.server.requests ) 등 많은 메트릭을 확인할 수 있다. 

#### - 자세히 확인하기 ( /actuator/metrics/{metirc-name} )
> availableTags 를 통해 Tag 를 기반으로 정보를 필터링해서 확인할 수도 있다. <br>
ex> localhost:8080/actuator/metrics/{metirc-name}?tag=KEY:VALUE

- jvm.memory.used 실행 결과
![image](https://github.com/dlask913/TIL/assets/79985588/95c6b81f-b7d0-45ee-88af-4379d5c714bd)

<br>

## 다양한 메트릭
### 1. JVM 메트릭 ( jvm.* )
- 메모리 및 버퍼 풀 세부 정보
- 가비지 수집 관련 통계
- 스레드 활용
- 로드 및 언로드된 클래스 수
- JVM 버전 정보
- JIT 컴파일 시간

### 2. 시스템 메트릭 ( system.* , process.* , disk.* )
- CPU 지표
- 파일 디스크립터 메트릭 
- 가동 시간 메트릭
- 사용 가능한 디스크 공간

### 3. 애플리케이션 시작 메트릭 
- application.started.time : 애플리케이션을 시작하는데 걸리는 시간 ( ApplicationStartedEvent 로 측정, 스프링 컨테이너가 완전히 실행된 상태고 이후에 커맨드 라인 러너가 호출 )
- application.ready.time : 애플리케이션이 요청을 처리할 준비가 되는데 걸리는 시간 ( ApplicationReadyEvent 로 측정, 커맨드 라인 러너가 실행된 이후에 호출 )

### 4. 스프링 MVC 메트릭
- http.server.requests : 스프링 MVC 컨트롤러가 처리하는 모든 요청을 다룬다.
> TAG 를 통해 정보를 분류해서 확인하기
> - uri : 요청 URI
> - method : HTTP 메서드 ( ex> ?tag=method:POST)
> - status : 200, 400 등의 HTTP Status 코드
> - exception : 예외
> - outcome : 상태코드를 그룹으로 모아서 확인 ( ex> 1xx:INFORMATIONAL ,..)

### 5. 데이터소스 메트릭 ( jdbc.connections.* )
: Datasource, 커넥션 풀에 관한 메트릭을 확인할 수 있다. ( 최대/최소/활성/대기 커넥션 수 등 )
> 히카리 커넥션 풀을 사용하면 hikaricp. 를 통해 자세한 메트릭 확인 가능


### 6. 로그 메트릭
- logback.events : logback 로그에 대한 메트릭 확인 및 각 로그 레벨( trace, debug, info, warn, error ) 에 따른 로그 수를 확인할 수있다. 

### 7. 톰캣 메트릭 ( tomcat.* )
: 톰캣 메트릭을 모두 사용하려면 아래의 옵션을 켜야한다. ( 그렇지 않으면 tomcat.sessions.* 만 노출 )
- application.yml
```yml
server:  
  tomcat:  
    mbeanregistry:  
      enabled: true
```
- tomcat.threads.busy (★) : 실제 바쁘게 동작하는 스레드 수
- tomcat.threads.config.max (★) : 최대 요청을 처리할 수 있는 스레드 수

### 8. 기타
- HTTP 클라이언트 메트릭 ( RestTemplate, WebClient )
- 캐시 메트릭
- 작업 실행과 스케줄 메트릭
- 스프링 데이터 리포지토리 메트릭
- 몽고DB 메트릭
- 레디스 메트릭

<br>

**※ 사용자가 직접 메트릭을 정의할 수도 있으며, 사용자 정의 메트릭을 만들기 위해서는 마이크로미터의 사용법을 먼저 이해해야 한다.**

<br>

## 참고 
[인프런 - 스프링 부트 핵심 원리와 활용](https://inf.run/7VBBx) 