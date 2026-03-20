## (os) 리눅스 성능분석을 위한 명령어 3
> top, netstat(ss)

<br>

### top 
> 프로세스들의 상태와 CPU, 메모리 사용률 확인

```bash 
$ top
# Tasks : 프로세스 갯수
# CPU : ..us, ..sy, ..ni, ..io 및 status 확인
```

<img width="800" height="256" alt="image" src="https://github.com/user-attachments/assets/104c6e98-f0b4-4add-9bf8-3d8568501a4c" />

- us(user) : 프로세스의 일반적인 CPU 사용량 → 높으면 더 좋은 CPU 로 변경
- wa(waiting) : I/O 작업을 대기할 때의 CPU 사용량 → 높으면 더 좋은 블록 디바이스로 변경
- 멀티코어 환경이라면 모든 CPU 를 사용하고 있는지 확인

#### hot key 사용
- 1을 누르면 Cpu (s) 가 각각의 CPU 로 변함 → CPU 의 불균형 확인 가능 ★
<img width="800" height="400" alt="image" src="https://github.com/user-attachments/assets/4fd58c35-9f1b-44ff-88be-23cd2a5d5bec" />

- d 를 누르면 인터벌 변경 가능 ( 기본 3초 단위 ), 1초로 변경하기
<img width="800" height="199" alt="image" src="https://github.com/user-attachments/assets/5fb3c9da-684a-4d66-b5a5-89eb0be19dd1" />

#### 프로세스의 상태
- D : uninterruptible sleep (I/O). I/O 대기 상태로, vmstat 에서 b 상태와 동일 - load average 에 포함
- R : running (CPU) - load average 에 포함
- S : sleeping. 작업을 하고 있지 않은 상태
- Z : zombie. cpu 와 메모리를 사용하지는 않지만 이슈를 일으킬 가능성이 있음

#### 좀비 프로세스
- 부모 프로세스가 죽었는데도 살아있는 자식 프로세스
- 시스템 리소스를 사용하지는 않지만 PID 고갈을 일으킬 수 있다
```bash
# 커널 파라미터 pid_max 값 확인하기
$ sudo sysctl -a | grep -i pid_max
kernel.pid_max = 99999
```

<br>

### netstat, ss
> 네트워크 연결 정보 확인

- netstat 은 옛날 표준, ss 은 현재 표준으로 많이 쓰임
```bash
# ss 설치
$ apt update
$ apt install -y iproute2

# 네트워크 연결 정보 확인
$ ss -tuln
```

#### 주요 상태
- `LISTEN` : 서버가 연결 요청을 기다리는 상태
- `ESTABLISHED` : 클라이언트 ↔ 서버 연결이 실제로 성립된 상태
- `TIME_WAIT` : 연결이 끝났지만 바로 닫지 않고 잠깐 대기하는 상태
- `CLOSE_WAIT` : 정상적으로 소켓을 정리하는 등 연결을 끊기 위한 동작을 하지 못하는 상태 ( 애플리케이션 이상 ) → 꼭 원인 확인해서 해결 필요

#### keepalive_timeout 
- HTTP/1.1 스펙 중 하나로 연결을 유지하는 설정
- HTTP 요청에 대해 커넥션을 새로 맺지 않아도 되도록 설정된 시간 동안 새로운 요청을 기다린다
- 설정된 시간이 지나면 서버가 먼저 연결을 끊는다 → TIME_WAIT 소켓이 생성

<br>

## 참고
[인프런 - 리눅스 성능 분석 시작하기](https://inf.run/tahef)