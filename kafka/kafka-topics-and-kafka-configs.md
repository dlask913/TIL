# 카프카 CLI 활용하기
> kafka-topics.sh , kafka-configs.sh 활용하기

<br>

## kafka-topics.sh
카프카 클러스터 정보와 토픽 이름은 토픽을 만들기 위한 필수 값으로 이렇게 만들어진 토픽은 파티션 개수, 복제 개수 등의 옵션을 설정할 수 있으며 따로 설정하지 않으면 브로커에 설정된 기본 값으로 설정된다.
### 토픽 생성하기 ( --create )
```shell
$ bin/kafka-topics.sh --create \
--bootstrap-server my-kafka:9092 \
--topic hello.kafka2 \
--partitions 10 \ # optional
--replication-factor 1 \ # optional
--config retention.ms=172800000 # optional
```
```shell
> .\bin\windows\kafka-topics.bat --create --bootstrap-server my-kafka:9092 --topic hello.kafka
```

### 토픽 상세 정보 확인 ( --describe )
```shell
$ bin/kafka-topics.sh --bootstrap-server my-kafka:9092 --topic hello.kafka --describe
```
```shell
> .\bin\windows\kafka-topics.bat --bootstrap-server my-kafka:9092 --topic hello.kafka --describe
```

- 실행결과
![image](https://github.com/dlask913/TIL/assets/79985588/c931f439-e410-4971-96d4-f2cdecb09f6c)

### 토픽의 파티션 개수 변경 ( --alter )
<b>1. test 라는 토픽의 파티션 개수 1-> 10으로 늘리기</b>

```shell
$ bin/kafka-topics.sh \
--bootstrap-server my-kafka:9092 \
--topic test \
--alter --partitions 10 
```

```shell
> .\bin\windows\kafka-topics.bat --bootstrap-server my-kafka:9092 --topic test --alter --partitions 10 
```
- 실행 결과
![image](https://github.com/dlask913/TIL/assets/79985588/239d30c0-e9c4-4cb6-8328-d989c2c279de)

<b>2. test 라는 토픽의 파티션 개수 10-> 5로 줄이기</b> 

파티션 개수를 늘릴 수는 있지만 줄일 수는 없다. 다시 줄이려면 InvalidPartitionsException 발생. 분산 시스템에서 이미 분산된 데이터를 줄이는 방법은 매우 복잡하기 때문에 피치못할 사정으로 파티션 개수를 줄여야 할 때는 토픽을 새로 만드는 편이 좋다. 
- 실행 결과
![image](https://github.com/dlask913/TIL/assets/79985588/ab23baa9-bf4b-40ca-903e-5349d0ea396b)

<br>

## kafka-configs.sh
토픽의 일부 옵션을 설정할 수 있다. --alter 와 --add-config 옵션을 사용하여 min.insync.replicas 옵션을 토픽별로 설정할 수 있다. 
> min.insync.replicas : 특정 토픽에 대해 성공적으로 쓰기 작업을 완료하기 위해 동기화되어 있어야 하는 최소 복제 수
```shell
$ bin/kafka-configs.sh --bootstrap-server my-kafka:9092 \
--alter \
--add-config min.insync.replicas=2 \
--topic test
```

```shell
>.\bin\windows\kafka-configs.bat --bootstrap-server my-kafka:9092 --alter --add-config min.insync.replicas=2 --topic test
```
- 실행 결과![image](https://github.com/dlask913/TIL/assets/79985588/63f301e8-4641-4af7-81f6-71341fddee39)


브로커에 설정된 모든 기본값은 --broker, --all, --describe 옵션을 사용하여 조회할 수 있다. 
```shell
$ bin/kafka-configs.sh --bootstrap-server my-kafka:9092 \
--broker 0 \
--all \ 
--describe
```
```shell
>.\bin\windows\kafka-configs.bat --bootstrap-server my-kafka:9092 --broker 0 --all --describe
```

<br>

## 참고
[아파치 카프카 애플리케이션 프로그래밍](https://inf.run/uCwV5) 