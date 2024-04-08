# 카프카 CLI 활용하기 3
> kafka-consumer-groups.sh, kafka-*-perf-test.sh, 파티션 위치 변경, 삭제, 로그 확인

<br>

## kafka-consumer-groups.sh
컨슈머 그룹은 따로 생성하는 명령 없이 컨슈머 그룹 이름을 정하면 생성된다. 생성된 컨슈머 그룹의 리스트는 kafka-consumer-groups.sh 로 확인할 수 있다. 
### 컨슈머 그룹 리스트 확인 ( --list )
```shell
$ bin/kafka-consumer-groups.sh \
--bootstrap-server my-kafka:9092 \
--list
hello-group # 출력 확인
```

```shell
> .\bin\windows\kafka-consumer-groups.bat --bootstrap-server my-kafka:9092 --list
```

### 그룹 상태 조회 ( --describe )
해당 컨슈머 그룹이 어떤 토픽을 대상으로 몇 번 오프셋의 레코드를 가져갔는 지 확인할 수 있다. 파티션 번호, 현재까지 가져간 레코드의 오프셋, 파티션 마지막 레코드의 오프셋, 컨슈머 랙, 컨슈머 ID, 호스트를 알 수 있기 때문에 컨슈머의 상태를 조회할 떄 유용하다. 
> 컨슈머 랙 : 마지막 레코드의 오프셋과 현재 컨슈머 그룹이 가져간 레코드의 오프셋 차이. <br> 컨슈머 랙이 4일 때 컨슈머가 0번을 처리하고 있고 가장 마지막 레코드가 4라면 컨슈머 랙만큼 컨슈머의 처리 속도가 지연되고 있다는 것을 뜻한다. <br>
-> 지연의 정도 ★ ( 그룹을 사용했을 때의 이점 )
```shell
$ bin/kafka-consumer-groups.sh --bootstrap-server my-kafka:9092 \
--group hello-group --describe # 그룹에 대한 상세 정보 함께 확인
```
```shell
> .\bin\windows\kafka-consumer-groups.bat --bootstrap-server my-kafka:9092 --group hello-group --describe
```
- 실행 결과
![image](https://github.com/dlask913/TIL/assets/79985588/a2053669-e698-4dd6-a25b-9d3067819505)

### 오프셋 리셋 ( --reset-offsets )
데이터를 어디서부터 다시 읽을지 ( 어느 오프셋부터 리셋할지 ) 설정하고 이는 메시지 재처리가 필요할 때 유용하게 사용된다.
- ```--to-earliest``` : 가장 낮은 오프셋(작은번호)로 리셋
- ```--to-latest``` : 가장 마지막 오프셋 (큰번호) 로 리셋
- ```--to-current``` : 현 시점 기준 오프셋으로 리셋
- ```--to-datetime``` {YYYY-MM-DDTHH:mmSS.sss} : 특정 일시로 오프셋 리셋 (레코드 타임스탬프 기준)
- ```--to-offset {long}``` : 특정 오프셋으로 리셋
- ```--shift-by {+/- long}``` : 현재 컨슈머 오프셋에서 앞뒤로 옮겨서 리셋

```shell
$ bin/kafka-consumer-groups.sh \ 
--bootstrap-server my-kafka:9092 \
--group hello-group \
--topic hello.kafka \
--reset-offsets --to-earliest --execute # 가장 낮은 오프셋으로 리셋
..
# 결과 확인
$ bin/kafka-console-consumer.sh --bootstrap-server my-kafka:9092 \
--topic hello.kafka \
--group hello-group
# 출력
1 
2
3
```
```shell
> .\bin\windows\kafka-consumer-groups.bat --bootstrap-server my-kafka:9092 --group hello-group --topic hello.kafka --reset-offsets --to-earliest --execute
..
# 결과 확인
> .\bin\windows\kafka-console-consumer.bat --bootstrap-server my-kafka:9092 --topic hello.kafka --group hello-group
```

<br>

## kafka-producer-perf-test.sh
카프카 프로듀서로 성능을 측정할 때 사용한다. 
- ```--producer-props```: 프로듀서가 메시지를 보낼 클러스터의 연결 정보를 설정한다.
- ```--topic```: 메시지를 보낼 대상 토픽의 이름을 지정한다.
- ```--num-records```: 전송할 메시지의 총 개수를 지정한다.
- ```--throughput```: 초당 전송할 메시지의 최대 개수를 지정한다. 1로 설정된 경우, 초당 최대 1개의 메시지를 전송하도록 제한하고 이는 부하를 제한하거나 특정 처리량을 시뮬레이션하기 위해 사용될 수 있다.
- ```--record-size```: 전송할 각 메시지의 크기를 바이트 단위로 지정. ( 메시지 크기는 성능 테스트 지표로 중요 )
- ```--print-metric```: 테스트 실행 후, 프로듀서 관련 성능 지표를 출력하도록 설정한다.
```shell
$ bin/kafka-producer-perf-test.sh \
--producer-props bootstrap.servers=my-kafka:9092 \
--topic hello.kafka \
--num-records 10 \
--throughput 1 \
--record-size 100 \
--print-metric
```
```shell
> .\bin\windows\kafka-producer-perf-test.bat --producer-props bootstrap.servers=my-kafka:9092 --topic hello.kafka --num-records 10 --throughput 1 --record-size 100 --print-metric
```

- 실행 결과
![image](https://github.com/dlask913/TIL/assets/79985588/11f49ea4-5dba-43a0-a704-9a8134faf72d)

<br>

## kafka-consumer-perf-test.sh
카프카 컨슈머의 성능을 측정할 때 사용한다. 카프카 브로커와 컨슈머 간 네트워크를 체크할 때 사용할 수 있다. 
- `--messages`: 소비할 메시지의 총 개수를 지정한다.
- `--show-detailed-stats`: 테스트 실행 결과에 상세한 통계를 표시하도록 지정한다. ( 메시지 처리량, 소비 지연 시간 등 )
```shell
$ bin/kafka-consumer-perf-test.sh \
--bootstrap-server my-kafka:9092 \
--topic hello.kafka \
--messages 10 \
--show-detailed-stats
```
```shell
> .\bin\windows\kafka-consumer-perf-test.bat --bootstrap-server my-kafka:9092 --topic hello.kafka --messages 10 --show-detailed-stats
```

<br>

## kafka-reassign-partitions.sh
카프카 클라이언트와 직접적으로 통신하는 리더 파티션들이 특정 브로커에 몰리는 경우가 생길 수 있는데, 카프카 클러스터 내에서 파티션의 리더 파티션과 팔로워 파티션을 재배치할 수 있다.
> 브로커 auto.leader.rebalance.enable 옵션의 기본값이 true 라서 클러스터 단위에서 리더 파티션을 자동 리밸런싱하도록 도와준다. 

<br>

## kafka-delete-record.sh
카프카 토픽의 특정 오프셋 이전의 데이터를 삭제할 수 있다. 이는 데이터 보존 정책을 수동으로 관리하거나, 필요하지 않은 데이터를 제거해 저장 공간을 확보할 수 있다. 

<br>

## kafka-dump-log.sh
카프카는 각 메시지가 저장되는 로그 파일을 관리하는데 이 스크립트를 통해 로그 파일의 상세 정보를 확인할 수 있다. 로그 파일 분석을 통해 데이터 복제 상태, 메시지 저장 형식 등을 검토할 수 있다. 

<br>

## 참고
[아파치 카프카 애플리케이션 프로그래밍](https://inf.run/uCwV5) <br>
http://cloudurable.com/blog/kafka-architecture-log-compaction/index.html
