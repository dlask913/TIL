## (spring) 그라파나
> 그라파나 사용하기 ( 대시보드 및 패널 생성 )

<br>

## 그라파나 사용하기
1. 설치 ( [공식문서](https://grafana.com/grafana/download?platform=windows) )
- window 기준 - zip file 을 다운받아 bin 폴더 내 grafana-server.exe 실행 ( 프로메테우스 실행하고 실행 )
2. 접속
- localhost:3000 접속하여, 초기 계정 없기 때문에 user/pw 는 admin 으로 입력하여 로그인.

### 동작 순서
1. micrometer 표준 방식에 맞추어 메트릭 측정
2. 메트릭 지속 수집
3. 프로메테우스 DB 에 메트릭 저장
4. **그라파나를 통한 조회**

<br>

## 그라파나 프로메테우스 연동
: 그라파나는 프로메테우스를 통해서 데이터를 조회하고 보여주는 역할
1. Home > Connections > Data sources > Add data source 탭 내 Prometheus 선택
<img src="https://github.com/dlask913/TIL/assets/79985588/57f90242-5f37-4783-ab96-38d320b1cd0a" width="800">

2. Connection URL 입력 후 Save & Test
<img src="https://github.com/dlask913/TIL/assets/79985588/6f2d14ea-9254-4f9f-9ffb-39f876aed7fe" width="800">

<br>

## 그라파나 대시보드 만들기
: 그라파나를 사용해서 주요 메트릭을 대시보드로 만들 수 있다. 그 전에, 애플리케이션/프로메테우스/그라파나는 모두 실행중인 상태여야한다.

1. Home > Dashboards > New dashbord - prometheus 선택 후 Edit panel 탭에서 Save 버튼 클릭하여 Details 작성하여 저장 ( Save )
<img src="https://github.com/dlask913/TIL/assets/79985588/ba864c39-79b7-42ae-856a-7be9e4a10261" width="800">

2. Home > Dashboards > hello dashboard ( 위에서 만든 대시보드 ) 에 CPU 사용량 패널 생성하기
- Add query 를 통해 추가 데이터 확인 가능 ( system_cpu_usage, process_cpu_usage, .. )
<img src="https://github.com/dlask913/TIL/assets/79985588/392a3ca6-0d0f-4ed3-a5dc-9788767f5d96" width="800">

- 패널 내 Legend 를 Custom 하여 이름을 변경할 수도 있다.
<img src="https://github.com/dlask913/TIL/assets/79985588/a6f76f78-6faf-4f97-b14d-b7ace779db57" width="800">

3. Home > Dashboards > hello dashboard ( 위에서 만든 대시보드 ) 에 디스크 사용량 패널 생성하기
- A Query : disk_total_bytes ( 전체 용량 )
- B Query : disk_total_bytes - disk_free_bytes ( 사용 용량 )
- Standard options 내 Unit 에서 bytes(SI) 클릭하여 단위 설정 ( ex> 000.. 에서 GB 등으로 표현 )
- Standard options 내 Min 을 0으로 설정하여 범위 0~Auto 로 조절
<img src="https://github.com/dlask913/TIL/assets/79985588/9fc539d6-beef-46c7-951c-1cc30fbb9cf4" width="800">

<br>

## 그라파나 공유 대시보드 활용하기 ( [공식문서](https://grafana.com/grafana/dashboards) )
: 이미 만들어져있는 대시보드를 import 하여 사용할 수 있다. ( Spring Boot 2.1 System Monitor, JVM 등 )

1. Home > Dashboards > Import dashboard 에서 가져오고싶은 대시보드의 ID 를 Load 한 후, 데이터 소스 선택하고 Import
<img src="https://github.com/dlask913/TIL/assets/79985588/99a304ce-157e-4f49-b78f-50af34cacab1" width="800">

2. 실행 결과, 다양한 유용한 패널들 확인
<img src="https://github.com/dlask913/TIL/assets/79985588/9af29a2a-da8d-4979-8da7-ce73a7c9fbb1" width="800">

3. Tomcat Statistics 내 Thread Config Max 와 Threads 를 Jetty → Tomcat 으로 설정 변경
- Thread Config Max Query : ```tomcat_threads_config_max_threads{instance="$instance", application="$application"} ```
- Threads : ```tomcat_threads_current_threads{instance="$instance", application="$application"}```
<img src="https://github.com/dlask913/TIL/assets/79985588/0f9c60c8-da39-41a8-8891-8c183796f957" width="800">

<br>

## 참고 
[인프런 - 스프링 부트 핵심 원리와 활용](https://inf.run/7VBBx) 