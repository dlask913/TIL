## (redis) 부하테스트로 Redis 성능 비교하기
> 부하테스트를 위한 환경 세팅 ( k6 ), Redis 전후 Throughput (처리량) 비교하기 

<br>

## 부하테스트를 위한 환경 세팅 ( k6 )

- Window 의 경우 powershell 을 활용하여 k6 설치 ( [참고](https://yscho03.tistory.com/101#google_vignette) )
```powershell
# 1. chocolately 패키지 매니저 설치
> Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# 2. k6 설치
> choco install k6

# 3. 설치 확인
> k6
```

- script.js 작성
```js
import http from 'k6/http';
import { sleep } from 'k6';

export default function () {
  http.get('{요청하고싶은 API 주소}');
}
```

<br>

## Redis 전후 Throughput (처리량) 비교하기

> ※ Throughput 과 TPS
> - Throughput : 부하 테스트에서 서비스가 1초당 처리할 수 있는 작업량 / 요청 수
> - TPS ( 단위 ) : Transaction Per Seconds, 1초당 처리한 트랜잭션의 수

#### 1. EC2 인스턴스에서 nohup 으로 애플리케이션 백그라운드 실행
```shell
nohup java -jar -Dspring.profiles.active=prod [애플리케이션-SNAPSHOT].jar &
```
- 8080 포트 사용중인 프로세스 확인
```shell
lsof -i:8080
```

#### 2. 작성한 script.js 가 있는 위치에서 k6 run
- `--vus 30` : 가상의 사용자 ( virtual users ) 30명이 요청
- `--duration 10s` : 10초의 지속 시간
```shell
k6 run --vus 30 --duration 10s script.js 
```

#### 3-1. Redis 적용하지 않은 경우, 실행 결과
- http_reqs 지표를 통해 Throughput 이 1.9 인 것을 알 수 있다.  <br>

![image](https://github.com/user-attachments/assets/0758007f-cd91-4c57-97e0-93b3fe5c6504)

#### 3-2. Redis 적용한 경우, 실행 결과
- http_reqs 지표를 통해 Throughput 이 118.4 인 것을 알 수 있다.  <br>

![image](https://github.com/user-attachments/assets/8407c68b-22cd-4a18-b005-ebf631f2ab79)

#### 4. Redis 를 적용한 경우, 62 배 정도의 Throughput (처리량)이 향상되었음을 알 수 있다.


<br>

## 참고
[인프런 - 비전공자도 이해할 수 있는 Redis 입문/실전 (조회 성능 최적화편)](https://inf.run/Pupon)