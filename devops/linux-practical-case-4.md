## (os) 리눅스 성능분석 실제 사례 4,5
> 간헐적인 타임아웃, EC2 CPU 이상 동작

<br>

### 실제 사례 4. 간헐적인 타임아웃
> JVM Full-GC로 인한 장애

- 장애 현상 : 간헐적으로 API 호출 시 타임아웃 에러가 발생
```console
[WARN] - from application in application-akka.actor.default-dispatcher-3 XXX is too slow [ElpasedTime: 10864 ms]
```
- 분석1 : 메트릭 수집 - netstat 을 통해 네트워크 연결 Established 확인, tcpdump 를 통해 패킷들의 흐름을 수집 후 분석
- 분석2 : timeout 이 간헐적이기 때문에 tcpdump 를 긴 호흡으로 패킷 수집 후 타임아웃이 발생한 순간의 pcap 분석
```bash
$ tcpdump -vvv -nn -A -G 3600 -w /var/log/tcpdump/$(hostname)_%Y%m%d-%H%M%S.pcap
# 3600 은 갱신되는 단위, 1시간에 1번씩
```
- 문제 상황 : 패킷과 패킷 사이의 시간차가 10초 -> 패킷 생성 자체가 늦어짐 (응답과는 무관)

1. 서버에 부하가 극심해서 패킷 생성 자체가 늦어졌다
  → Load Average 및 CPU 사용량이 높지 않았기 때문에 해당사항 없음
2. 애플리케이션의 프로세싱 기간이 길어져서 패킷 생성이 늦어졌다
  → 애플리케이션에 영향을 줄 만한 로직이 있는지?

- 원인 발견 : JVM Full-GC 로 인한 멈춤 현상 (Full-GC 가 발생할 때 GC 쓰레드를 제외한 다른 쓰레드들이 멈추는 현상)
- 해결 : 개발팀 요청

<br>


### 실제 사례 5. EC2 CPU 이상 동작
> 물리서버의 CPU 불량으로 인한 장애

- 장애 현상 : 간헐적으로 CPU Usage 가 높다는 알람 발생
- 분석1 : 메트릭 수집 - top 을 통해 어떤 프로세스가 CPU 를 사용하는지 확인
- 분석 결과1 
  - JAVA 프로세스 CPU 100% 사용 & top 명령어에도 CPU 100% 사용
  - 모든 서버에서 발생하지 않고 일부 서버에서만 발생

- 분석2 : 이슈가 발생하는 서버만 서비스에서 제외, ASG 에서도 제외시키고 삭제 방지 기능 활성화 후 분석 시작
- 분석 결과2
  - CPU 에 문제가 있는 지 확인 (`/proc/cpuinfo`) 해보니, CPU MHz 가 낮음

- 원인 발견 : 이상 서버의 CPU MHz 가 낮음
- 해결 : EC2 서버 하드웨어 문제로 확인

#### CPU 분석
- CPU 에 문제가 있는 지 확인하기 위해 `/proc/cpuinfo` 분석
- C-State : 전력 소모량을 줄이기 위해 일부 CPU 코어를 비활성화하는 기능
- P-State : 작업 부하에 따라 CPU 의 전압과 MHz 를 조절하는 기능
- 두 기능에 의해 MHz 가 유동적일 수 있으며, turbostat 명령을 통해 강제로 CPU MHz 를 끌어올릴 수 있다 

<br>

## 참고
[인프런 - 리눅스 성능 분석 시작하기](https://inf.run/tahef)