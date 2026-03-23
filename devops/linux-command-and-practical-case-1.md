## (os) 리눅스 성능분석을 위한 명령어 및 실제 사례 1
> tcpdump, nginx miss configuration

### tcpdump
> 네트워크 패킷 수집 및 분석

- `-nn` : 프로토콜과 포트 번호를 숫자 그대로 표현
- `-vvv` : 출력 결과에 더 많은 정보를 포함
- `-A` : 패킷의 내용 함께 출력

```bash
$ tcpdump -nn -vvv -A
```

#### 트러블 슈팅
- 모든 패킷을 다 잡게 되면 복잡하기 때문에 목적지와 포트 명시 (필터링)
```bash
# 80 포트 통신 패킷 조회
$ tcpdump -vvv -nn -A port 80 
# 10.1.1.1 80 포트 통신 패킷 조회
$ tcpdump -vvv -nn -A host 10.1.1.1 and port 80
```

#### Wireshark 
> tcpdump - 생성 -> pcap 파일 - 읽기 -> wireshark

- pcap 파일 생성
```bash
$ tcpdump -vvv -nn -A host 10.1.1.1 and port 80 -w http_dump.pcap
```

- pcap 을 wireshark 로 열어서 HTTP 요청의 전체 과정을 볼 수 있다 

<br>

### 실제 사례 1. nginx miss configuration
> nginx workers 설정 미숙으로 인한 장애

- 장애 상황 : 트래픽 증가와 함께 컴퓨팅 리소스 부족으로, 서버의 응답 지연 발생
- 분석1 : 메트릭 수집 - CPU 사용량이 문제인지, 메모리 사용량이 문제인지
- 분석2 : 멀티 코어일 경우 반드시 모든 코어를 확인하여 특정 cpu 에 문제가 발생하였는 지 확인
- 문제 상황 : 하나의 CPU 에서 100% 사용량
- 원인 발견 : worker_processes ( nginx 설정 중 사용자의 요청을 처리하는 워커 프로세스 개수를 설정하는 항목 ) 가 1로 설정
- 해결 : auto 로 변경하여 cpu 개수에 맞게 워커프로세스를 생성하도록 한다 


<br>

## 참고
[인프런 - 리눅스 성능 분석 시작하기](https://inf.run/tahef)