# 카프카 설치 및 실행
> 카프카 CLI는 카프카를 운영할 때 가장 많이 접하는 도구로 카프카 브로커 운영에 필요한 다양한 명령을 내릴 수 있다.

<br>

## 로컬 카프카 설치 및 실행 ( jdk 1.8이상 )
### 1. 카프카 바이너리 파일 다운로드 ( kafka.apache.org/downloads ) - kafka_2.12-2.5.0.tgz
### 2. 카프카 바이너리 압축 해제
### 3. 주키퍼 실행
: 주키퍼는 따로 설정하지 않고 바이너리에 있는 주키퍼를 실행할 수 있는데, 실제 운영 환경에서는 3 개 이상의 서버에서 앙상블로 묶어서 운영하는 것이 이상적이다.
```shell
$ bin/zookeeper-server-start.sh config/zookeeper.properties 
```
``` cmd
> .\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties
```
### 4. 카프카 바이너리 실행
```shell
$ bin/kafka-server-start.sh config/server.properties
```
```cmd
> .\bin\windows\kafka-server-start.bat .\config\server.properties
```

<br>

## config/server.properties 내 여러 옵션들
- **broker.id** : 브로커 초기 id
- **log.dirs** : 파일 시스템 위치
- **num.partitions** : 기본적으로 만들 파티션 개수
- **log.retention.hours** : 설정된 시간이 지나면 데이터를 삭제
- **log.segment.bytes** : 토픽의 로그 세그먼트 파일 크기의 최댓값
- **log.retention.check.interval** : 세그먼트 파일이 삭제되기 위해 검사되는 빈도

<br>

## 서버 실행 후 확인 방법
### 카프카 정상 실행 여부 확인
```shell
$ bin/kafka-broker-api-versions.sh --bootstrap-server localhost:9092
```
```cmd
> .\bin\windows\kafka-broker-api-versions.bat --bootstrap-server localhost:9092
```
![image](https://github.com/dlask913/TIL/assets/79985588/828e3e22-948e-47b7-a89b-6f30e3f0b9e7)

### 토픽 리스트 조회
```shell
$ bin/kafka-topics.sh --bootstrap-server localhost:9092 --list 
```
``` cmd
> .\bin\windows\kafka-topics.bat --bootstrap-server localhost:9092 --list
```

### 테스트 편의를 위한 hosts 설정
: PC 내 hosts 파일에 ```127.0.0.1 my-kafka``` 를 추가

<br>

## 참고
[아파치 카프카 애플리케이션 프로그래밍](https://inf.run/uCwV5) 