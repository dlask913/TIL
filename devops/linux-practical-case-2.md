## (os) 리눅스 성능분석 실제 사례 2
> 간헐적 네트워크 응답 지연

<br>

### 실제 사례 2.간헐적 네트워크 응답 지연
> Read Timeout 으로 인한 장애

- 장애 현상 : 간헐적으로 API 호출 시 타임아웃 발생
- 분석1 : 메트릭 수집 - netstat 을 통해 네트워크 연결 Established 확인, tcpdump 를 통해 패킷들의 흐름을 수집 후 분석
- 분석2 : timeout 이 간헐적이기 때문에 tcpdump 를 긴 호흡으로 패킷 수집
```bash
$ tcpdump -vvv -nn -A -G 3600 -w /var/log/tcpdump/$(hostname)_%Y%m%d-%H%M%S.pcap
# 3600 은 갱신되는 단위, 1시간에 1번씩
```
- 문제 상황 : POST 요청 이후 POST 에 대한 응답을 받기 전에 FIN 패킷이 날라감
- 원인 발견 : Read Timeout 보다 프로세싱 소요 시간이 더 길다 ( Read Timeout - 3초, 프로세싱 시간 - 5초)
- 해결 : Read Timeout 6초로 변경 ( 프로세싱 시간 + 1초 ) or 프로세싱 시간 분석?

<br>

#### 클라이언트 타임아웃 설정
- 현재 상태가 정상이라고 판단할 때까지 얼마나 기다릴 것인가 에 대한 설정
- Connection Timeout : 종단 간 연결을 처음 맺을 때, 맨 처음 TCP Handshake 를 맺을 때 사용
- Read Timeout : 종단 간 연결을 맺은 후 데이터를 주고받을 때 사용
- Round Trip Time (RTT) : 패킷이 종단 간 이동할 때 걸리는 시간 ( 즉, 물리적 거리에 따른 시간 )
- Timeout 은 응답에 소요되는 시간 + 이동할 때 걸리는 시간(RTT) 보다 커야한다

<br> 

#### 고려 사항
1. RTT 를 모를 때 ( 종단 간 커넥션을 처음 맺을 때 )
- InitRTO : RTT 를 모를 때 사용하는 커널의 패킷 초기 타임아웃 값 ( ex> linux 는 1초 )
- Connection Timeout 설정 시, Handshake 과정 중 최소 한 번의 패킷 유실 정도는 방어할 수 있어야 하기 때문에 3초 (1초+RTT고려) 정도로 설정 권장

2. 패킷이 유실되었을 떄 
- 패킷에 대한 응답이 RTO (Retransmission TimeOut) 이내에 도착하지 않으면 유실로 간주
- 패킷이 중간에 유실되면 평균 응답 시간 300ms + 이동 시간 10ms + 재전송에 소요된 시간 200ms → 총 510ms 이상이 소요될 수 있다
- Read Timeout 설정 시, 프로세싱을 고려하고 최소한 한 번의 패킷 유실 정도는 방어할 수 있어야 하기 때문에 1초 (프로세싱 시간+RTO고려) 정도로 설정 권장
- Read Timeout 이 1초인데 프로세싱 시간이 1초가 넘으면 빈번한 타임아웃 에러 발생 가능

<br>

## 참고
[인프런 - 리눅스 성능 분석 시작하기](https://inf.run/tahef)