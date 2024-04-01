# 카프카 CLI 활용하기 2
> kafka-console-producer.sh , kafka-console-consumer.sh

<br>

## kafka-console-producer.sh
- 카프카 토픽에 데이터를 넣기 위한 명령어
- 테스트 용도로 데이터를 넣을 때 많이 사용되며, 메시지 입력 후 엔터 키를 누르면 메시지가 전송된다.
- 메시지 키를 포함해서 데이터를 넣는 경우, 해당 키의 해시값에 기반하여 메시지가 특정 파티션에 할당된다. → 동일한 메시지 키를 가진 레코드가 동일한 파티션에 저장되도록 보장한다.
- 메시지 키가 null 인 경우, 프로듀서는 라운드 로빈 방식으로 파티션에 메시지를 전송한다.

### 키 없이 메시지 전송 ( 레코드 저장 )
```shell
$ bin/kafka-console-producer.sh --bootstrap-server my-kafka:9092 --topic hello.kafka
>hello # 메시지 전송
>kafka # 메시지 전송
>0 # 메시지 전송
>1 # 메시지 전송
```
```shell
> .\bin\windows\kafka-console-producer.bat --bootstrap-server my-kafka:9092 --topic hello.kafka
```

### 키 포함하여 메시지 전송
- 메시지 키를 가지는 레코드를 넣기 위해서는 parse.key 옵션을 추가하면 된다. 
- key.separator 를 선언하지 않으면 기본 설정은 Tab(\t) 이고 아래는 콜론(:) 으로 설정한 예시다.
```shell
$ bin/kafka-console-producer.sh --bootstrap-server my-kafka:9092 \
--topic hello.kafka \
--property "parse.key=true" \
--property "key.separator=:"
>key:no1 # 메시지 전송
>key:no2 # 메시지 전송
>key:no3 # 메시지 전송
```
```shell
> .\bin\windows\kafka-console-producer.bat --bootstrap-server my-kafka:9092 --topic hello.kafka --property "parse.key=true" --property "key.separator=:"
```
<br>

## kafka-console-consumer.sh
- 토픽에 저장된 레코드를 읽는 명령어
- 토픽으로부터 메시지를 수신할 수 있으며, ```--from-beginning``` 옵션을 통해 토픽에 저장된 가장 처음 레코드부터 출력할 수 있다. 

### 토픽에 저장된 레코드 확인 
```shell
$ bin/kafka-console-consumer.sh \
--bootstrap-server my-kafka:9092 \
--topic hello.kafka --from-beginning
hello # 레코드 출력
kafka # 레코드 출력
0 # 레코드 출력
1 # 레코드 출력
```
```shell
> .\bin\windows\kafka-console-consumer.bat --bootstrap-server my-kafka:9092 --topic hello.kafka --from-beginning
```

### 키 값 포함하여 레코드 확인
- 레코드의 메시지 키와 값을 확인하기 위해서 ```--property``` 옵션을 사용한다.
```shell
$ bin/kafka-console-consumer.sh --bootstrap-server my-kafka:9092 \
--topic hello.kafka \
--property print.key=true \
--property key.separator=":" \
--from-beginning 
key:no1 # 레코드 출력
key:no2 # 레코드 출력
key:no3 # 레코드 출력
```
```shell
> .\bin\windows\kafka-console-consumer.bat --bootstrap-server my-kafka:9092 --topic hello.kafka --property print.key=true --property key.separator=":" --from-beginning
```

### --max-messages 와 --partition 옵션 사용하기
- ```--max-messages``` 옵션은 출력하는 최대 메시지 개수를 정할 수 있다. 
- ```--partition``` 옵션은 특정 파티션의 메시지만 소비하고자 할 때 사용된다. 

```shell
$ bin/kafka-console-consumer.sh --bootstrap-server my-kafka:9092 \
--topic hello.kafka \
--max-messages 1 \ # (optional) 최대 메시지 개수 설정
--partition 2 \ # (optional) 특정 파티션만 컨슘할 수 있다.
--from-beginning
```
```shell
> .\bin\windows\kafka-console-consumer.bat --bootstrap-server my-kafka:9092 --topic hello.kafka --partition 0 --max-messages 1 --from-beginning
```

### --group 옵션 사용하기
- 컨슈머 그룹을 사용하여 여러 컨슈머가 메시지를 공유하고 처리할 수 있게 한다. 
- 이 옵션을 사용하면 ```__consumer_offsets``` 라는 시스템 토픽이 자동 생성되며 이는 컨슈머 그룹이 어느 레코드까지 읽었는지에 대한 커밋 데이터를 저장한다.

```shell
$ bin/kafka-console-consumer.sh --bootstrap-server my-kafka:9092 \
--topic hello.kafka \
--group hello-group \
--from-beginning
```
```shell
> .\bin\windows\kafka-console-consumer.bat --bootstrap-server my-kafka:9092 --topic hello.kafka --group hello-group --from-beginning
```

### __consumer_offsets 확인하기
```shell
> .\bin\windows\kafka-topics.bat --bootstrap-server my-kafka:9092 --list

=============== 실행 결과 ===============
__consumer_offsets # group 사용 시 자동 생성
hello.kafka 
```

<br>

## 참고
[아파치 카프카 애플리케이션 프로그래밍](https://inf.run/uCwV5) 