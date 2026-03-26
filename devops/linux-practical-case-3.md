## (os) 리눅스 성능분석 실제 사례 3
> 간헐적인 커넥션 종료 에러

<br>

### 실제 사례 3. 간헐적인 커넥션 종료 에러
> Keepalive Timeout 으로 인한 장애

- 장애 현상 : 간헐적으로 API 호출 시 커넥션 에러가 발생
```console
Connection prematurely closed BEFORE response
```
- 분석1 : 메트릭 수집 - netstat 을 통해 네트워크 연결 Established 확인, tcpdump 를 통해 패킷들의 흐름을 수집 후 분석
- 분석2 : timeout 이 간헐적이기 때문에 tcpdump 를 긴 호흡으로 패킷 수집 후 타임아웃이 발생한 순간의 pcap 분석
- HTTPS 는 HTTP 와 다르게 패킷의 내용은 볼 수 없다 
```bash
$ tcpdump -vvv -nn -A -G 3600 -w /var/log/tcpdump/$(hostname)_%Y%m%d-%H%M%S.pcap
# 3600 은 갱신되는 단위, 1시간에 1번씩
```
- 문제 상황 : 상대 서버에서 FIN 패킷을 보내 먼저 연결을 끊음
- 원인 발견 : 요청한 서버에서 다시 요청한 패킷이 도착하기 전에 keep-alive 설정 시간 5초가 종료되어 상대 서버에서 FIN 패킷을 보냄 ( 타이밍이 안맞음 )
- 해결 : Idle Timeout 을 3초로 수정 ( 상대방과 연결을 맺고 HTTP 요청을 보낸 후 3초 동안 새롭게 보낼 요청이 없다면 연결을 먼저 끊는다 )

<br>

#### Keep-Alive 헤더
- HTTP 통신을 하고 나면 연결을 끊는 것이 원래 스펙이지만 커넥션을 맺는 리소스가 크기 때문에 HTTP/1.1 이 되며 `Conncection: Keep-Alive` 헤더를 제공한다
- 요청이 또 들어올 수 있기 때문에 서버에 설정된 시간만큼 커넥션을 유지한다
- HTTP KeepAlive 를 사용하지 않으면 커넥션 종료 에러는 발생하지 않지만 성능이 나빠진다

<br>

#### Reactor Netty 의 대응 가이드
- 호출하는 쪽에서의 Idle Timeout 은 HTTP KeepAlive 를 맺은 후 다음 요청을 전달하기 까지 기다리는 시간을 의미
- 호출하는 쪽에서 상대방 서버보다 idle timeout 을 짧게 가져간다


<br>

## 참고
[인프런 - 리눅스 성능 분석 시작하기](https://inf.run/tahef)