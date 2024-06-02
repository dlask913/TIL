## (spring) 프로메테우스
> 프로메테우스와 그라파나, 프로메테우스 사용하기

<br>

## 프로메테우스와 그라파나
### 프로메테우스 
: 애플리케이션에서 발생한 메트릭을 과거 이력까지 모두 보관하는 DB 가 필요한데, 프로메테우스가 메트릭을 지속해서 수집하고 DB 에 저장하는 역할을 한다.

### 그라파나
: 프로메테우스가 DB 라면 그라파나는 이 DB 에 있는 데이터를 불러서 사용자가 보기 편하게 보여주는 대시보드 역할을 한다. 그라파나는 매우 유연하고 데이터를 그래프로 보여주는 툴로, 수많은 그래프를 제공하고 프로메테우스를 포함한 다양한 데이터소스를 지원한다.

### 구조에 따른 동작 순서
1. 스프링 부트 액츄에이터와 마이크로미터를 사용하여 메트릭을 자동 생성한다. 
( 마이크로미터 프로메테우스 구현체는 프로메테우스가 읽을 수 있는 포멧으로 메트릭을 생성 )
2. 프로메테우스는 이렇게 만들어진 메트릭을 지속해서 수집한다.
3. 프로메테우스는 수집한 메트릭을 내부 DB 에 저장한다.
4. 사용자는 그라파나 대시보드 툴을 통해 그래프로 편리하게 메트릭을 조회한다. 

<br>

## 프로메테우스 사용하기
### 1. 설치 ( [공식문서](https://prometheus.io/download/) ) 
window 기준 -  [prometheus-2.52.0.windows-amd64.zip](https://github.com/prometheus/prometheus/releases/download/v2.52.0/prometheus-2.52.0.windows-amd64.zip) 다운로드 이후 prometheus.exe 실행

<br>

### 2. 애플리케이션 설정
프로메테우스가 애플리케이션의 메트릭을 가져가려면 프로메테우스가 사용하는 포맷에 맞추어 메트릭을 만들어야하는데 마이크로미터가 이런 부분을 모두 해결해준다.

- build.gradle 내 마이크로미터 프로메테우스 구현 라이브러리 추가
> 스프링 부트와 액츄에이터가 자동으로 마이크로미터 프로메테우스 구현체를 등록해서 동작하도록 설정
```groovy
dependencies {  
    implementation 'io.micrometer:micrometer-registry-prometheus' // 추가
}
```
- /actuator/prometheus 접속하여 결과 확인

#### /actuator/metrics 과의 차이점
- jvm.info → jvm_info : 프로메테우스는 _ 포맷을 사용
- logback.events → logback_events_total : 로그수처럼 지속해서 숫자가 증가하는 메트릭을 카운트라고 하는데 프로메테우스는 카운터 메트릭의 마지막에는 관례상 ```_total``` 을 붙인다.
- http.server.requests → 이 메트릭은 내부에 요청수, 시간 합, 최대 시간 정보를 가지고 있었는데  프로메테우스에서는 아래와 같이 분리된다.
  a. http_server_requests_seconds_count : 요청 수 <br>
  b. http_server_requests_seconds_sum : 시간 합 ( 요청수의 시간을 합함 ) <br>
  c. http_server_requests_seconds_max : 최대 시간 ( 가장 오래걸린 요청 수 ) <br>

<br>

### 3. 수집 설정
: 프로메테우스가 애플리케이션의 /actuator/prometheus 를 호출해서 메트리을 주기적으로 수집하도록 설정해본다.
- prometheus.yml 내 scrape_configs 하위에 job_name 추가
> 아래 수집 주기는 1s 로 되어있지만, 수집 주기의 기본 값은 1m 이다. 수입 주기가 너무 짧으면 애플리케이션 성능에 영향을 줄 수 있기 때문에 운영에서는 10s~ 1m 정도를 권장 ( 시스템 상황에 따라 다름 )
```yml
  - job_name: "spring-actuator"
    metrics_path: '/actuator/prometheus' # 경로
    scrape_interval: 1s # 수집 주기
    static_configs:
      - targets: ["localhost:8080"] # 타깃 서버의 주소
```

- 연동 확인
  - 프로메테우스 서버 재실행 후 9090 포트로 접속하여 상단 Status > Configuration 에서 설정 확인
![image](https://github.com/dlask913/TIL/assets/79985588/7f09d291-254d-4a3e-8ef4-155f90b4eaa9)

  - Status > Targets 접속하여 확인
![image](https://github.com/dlask913/TIL/assets/79985588/470711ea-3f21-4d42-8c38-bad5df1171e3)

<br>

### 4. 기본 기능
- 중괄호 문법을 사용하여 필터 사용하기.
  - 특정 uri 제외하고 검색하기 : ```http_server_requests_seconds_count{uri!="uri"}```
  - GET 만 검색하기 : ```http_server_requests_seconds_count{method="GET"}```
  - GET, POST 인 경우 검색하기 : ```http_server_requests_seconds_count{method=~"GET|POST"}```
- 연산자 지원 : +, -, \*, /, %, ^
- sum : 값의 합계 계산
- sum by : SQL 의 group by 와 유사
- count : 메트릭 자체의 수 카운트
- topk : 상위 n 개의 메트릭 조회 가능
- offset : 현재를 기준으로 특정 과거 시점의 데이터 조회
- 범위 벡터 선택기 : 마지막에 [1m], [60s] 와 같이 표현하여 지난 1분간의 모든 기록값을 선택한다. 

<br>

### 5. 게이지와 카운터
#### 게이지 (Gauge)
- 임의로 오르내릴 수 있는 값
- ex> CPU 사용량, 메모리 사용량, 사용중인 커넥션 <br>
→ 현재 상태를 그대로 출력하면 된다. 

#### 카운터(Counter)
- 단순하게 증가하는 단일 누적 값
- ex> HTTP 요청 수, 로그 발생 수  <br>
→ 계속 누적해서 증가하는 값이기 때문에 이 그래프에서는 특정 시간에 얼마나 고객의 요청이 들어왔는지 한눈에 확인하기 어렵다. 이런 문제를 해결하기 위해 increase(), rate() 와 같은 함수를 지원.

#### increase() 와 rate()
- increase : 지정한 시간 단위별로 증가를 확인할 수 있다. 마지막에 '[시간]' 을 사용해서 범위 벡터를 선택해야 한다.  <br>
ex> increase(http_server_requests_seconds_count{uri="/log"}[1m])
- rate : 범위 벡터에서 초당 평균 증가율을 계산한다. → 초당 얼마나 증가하는지 나타내는 지표 <br>
ex> rate(http_server_requests_seconds_count{uri="/log"}[1m])
- irate() : rate 와 유사한데, 범위 벡터에서 초당 순간 증가율을 계산한다. → 급격한 증가를 확인하고 싶을 때  <br>
ex> irate(http_server_requests_seconds_count{uri="/log"}[1m])

<br>

## 참고 
[인프런 - 스프링 부트 핵심 원리와 활용](https://inf.run/7VBBx) 