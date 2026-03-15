## (os) 리눅스 성능분석을 위한 명령어 1
> uptime, dmesg

<br>

### uptime
> 시스템의 가동 시간과 로그인한 사용자 수, Load Average 확인

```bash
$ uptime
00:00:00 up 0 min, 1 user, load average: 0.17, 0.05, 0.01

# up 0 min : 시스템 가동 시간
# 1 user : 현재 접속한 사용자 수 
# load average: 1분 부하 평균, 5분 부하 평균, 15분 부하 평균
```
- load average 는 서버가 받고 있는 부하 평균으로, 단위 시간 동안의 R 과 D 상태의 프로세스 개수를 말한다
- 두 서버가 똑같이 Load Average 가 1이라고 해도 이는 프로세스 개수이기 때문에 CPU 가 한 개일 때와 두 개일 때의 의미가 다르다
- **Load Average 가 CPU 개수보다 크면** 현재 처리 가능한 수준에 비해 많은 수의 프로세스가 존재한다는 의미
```bash
# CPU 확인, 만약 2라면 Load Average 가 2보다 작은 지를 확인
$ lscpu -e
```

#### Load Avearage 
- R 은 CPU 위주의 작업, D 상태는 I/O 위주의 작업
- R 상태의 프로세스가 많은 경우 → CPU or 스레드 개수 조절
- D 상태의 프로세스가 많은 경우 → IOPS 가 높은 디바이스로 변경하거나 처리량을 줄인다
- D 상태의 프로세스가 많아 Load Average 가 높은 경우에는 CPU 개수와는 상관없기 때문에 주의해야 한다 
- CUP 개수보다 많은 부하를 받고 있다면 어떤 종류의 프로세스 때문인지 확인 (★)
- 프로세스 상태 확인 명령어
```bash
$ vmstat 1
# procs 칼럼 확인, r 이면 CPU b 면 I/O
```

<br>

### dmesg
> 커널에서 발생하는 다양한 메시지들을 출력

```bash
$ dmesg -T
# -T 옵션 : 타임스탬프를 사용자 친화적이게 변경하여 출력
```
- OOME 혹은 SYN Flooding 공격 발생 여부를 확인할 수 있다
- OOME 발 생 시 더 많은 메모리를 확보하고 SYN Flooding 발생 시 방화벽을 확인한다

#### 1. OOME (Out-Of-Memory Error)
- 가용한 메모리가 부족해서 더이상 프로세스에게 할당해 줄 메모리가 없는 상황
- OOM Killer 가 메모리 회수를 위해 프로세스를 강제 종료한다
- OOM 상황 발생 → 종료할 프로세스 선택  → 프로세스 종료 → 시스템 안정화 ( 서비스와는 별개 )
- oom_score : OOM Killer 가 프로세스를 선택하는 기준으로, 스코어가 높은 프로세스가 더 먼저 종료된다 
- 프로세스 oom_score 확인하기
```bash
$ cat /proc/{pid}
$ cat oom_score
666 # score 출력

$ ps aux | grep {pid} # 프로세스 용도 확인
```
- dmesg 를 통해 OOME 메시지 확인하기
```bash
$ dmesg -T | grep -i oom
```

#### 2. SYN Flooding
- 공격자가 대량의 SYN 패킷만 보내서 소켓을 고갈시키는 공격
- TCP 3 way handshake 에서 Client 가 ACK 를 보내지 않으면 SYN Backlog 에 있는 소켓 정보가 넘어가지 않고 계속 쌓여 꽉 차게 되면 SYN 패킷을 drop 하여 새로운 연결 처리가 불가
<img width="469" height="316" alt="Image" src="https://github.com/user-attachments/assets/20d3b16e-7240-4912-8396-88cb57f03c90" />

- SYN Cookie :  SYN 패킷의 정보를 바탕으로 쿠키를 만들고 그 값을 SYN + ACK 의 시퀀스 번호로 만들어서 응답
- 쿠키는 계산 가능한 값이기 때문에 따로 저장할 필요가 없어 SYN Backlog 에 쌓지 않아 자원 고갈 현상이 발생 X
- 그러나 SYN Cookie 를 사용하면 TCP Option 헤더를 무시하기 때문에 성능 향상을 위한 옵션이 동작하지 않는다
- SYN Cookie 가 enable 되어 있어도 SYN Flooding 이 발생하는 것 같을 때에만 발행이 된다 
- dmesg 를 통해 syn flooding 메시지 확인하기
```bash
$ dmsg -TL | grep -i "syn flooding"
```

<br>

## 참고
[인프런 - 리눅스 성능 분석 시작하기](https://inf.run/tahef)