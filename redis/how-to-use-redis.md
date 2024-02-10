# (redis) Redis 실행 방법 및 데이터 타입
> Redis 실행 방법 및 데이터 타입과 명령어

<br>

##  Docker 로 Redis 사용하기 ( [공식문서](https://redis.io/download/) ) 
1. Redis pull 받아오기 ( [dockerhub](https://hub.docker.com/_/redis) 에서 확인 )
```shell
docker pull redis
```
2. 실행하기
```shell
docker run --name [NAME] -d redis
```
3. 접속하기
```shell
docker exec -it [컨테이너ID] redis-cli
```

<br>

## 데이터 타입 및 명령어
1. **Strings** : 문자열, 숫자, JSON string 등 저장
```shell
$ SET [key] [value] 
$ MSET [key1] [value1] [key2] [value2] .. # 여러개 저장

$ GET [key] 
$ MGET [key1] [key2] .. # 여러 키 값의 value 조회

$ INCR [key] # 해당 키 값의 value 를 1 증가
$ INCRBY [key] 5 # 해당 키 값의 value 를 5 만큼 증가
```

2. **Lists** : String 을 Linked List 로 저장 ( FIFO / FILO 구현에 사용 )
```shell
$ LPUSH [key] [value1] [value2] [value3] .. 
$ RPOP [key] # 가장 먼저 들어갔던 value 제거 및 출력
$ LPOP [key] # 가장 나중에 들어갔던 value 제거 및 출력

$ LRANGE [key] [시작index] [마지막index] # 시작index ~ 마지막 index 범위의 값 출력
$ LTRIM [key] [시작index] [마지막index] # 시작index ~ 마지막 index 빼거 제거
```

3. **Sets** : Unique string 을 저장하는 정렬되지 않은 집합 ( 집합 연산 사용 가능 )
```shell
$ SADD [key] [value1] [value2] [value3] .. # set 생성
$ SMEMBERS [key] # 해당 set 의 값 모두 조회
$ SCARD [key] # 개수 확인
$ SISMEMBER [key] [value] # 해당 set 에 value 가 포함되어있는 지 확인
$ SINTER [key1] [key2] # 교집합
$ SDIFF [key1] [key2] # key1 을 기준으로 차집합 
$ SUNION [key1] [key2] # 합집합
```

4. **Hashes** : field-value 구조를 갖는 데이터 타입 ( 딕셔너리와 유사 )
```shell
$ HSET [key] [field1] [value2] [field2] [value2] # hash 생성
$ HGET [key] [field] # 해당 키의 필드의 value 조회
$ HMGET [key] [field1] [field2] [field3] # 여러 value 조회

$ HINCRBY [key] [field] 5 # 숫자형인 경우 5 증가
```

5. **ZSets** : Unique string 이 연관된 score 를 통해 정렬된 집합 ( Set + score 속성 저장 )
```shell
$ ZADD [key] [score1] [value1] [score2] [value2] # zset 생성

$ ZRANGE [key] [시작idx] [마지막idx] # 시작idx~ 마지막idx 조회
$ ZRANGE [key] [시작idx] [마지막idx] REV WITHSCORES # 역정렬 + 스코어 같이 조회

$ ZRANK [key] [value] # index 반환
```

6. **Streams** : append-only log 에 consumer groups 과 같은 기능( 오직 추가만 되는 )을 더한 자료 구조 + * 옵션을 통해 unique id 할당 가능
```shell
$ XADD [key] * [field1] [value1] [field2] [value2] .. # stream 생성
$ XRANGE [key] - + # 발생한 모든 이벤트 조회
$ XDEL [key] [고유ID] # 고유ID 에 대한 이벤트 삭제
```

7. **Geospatials** ( Geospatial Indexes ) : 좌표를 저장하고 검색하는 데이터 타입. 거리 계산, 범위 탐색 등 지원
```shell
$ GEOADD [key] [경도1] [위도1] [member1] [경도2] [위도2] [member2] .. 
$ GEODIST [key] [member1] [member2] KM # 거리 계산 KM 로 반환
```

8. **Bitmaps** : 실제 데이터 타입이 아닌 String 에 binary operaiont 을 적용한 것

9. **HyperLogLog** : 집합의 cardinality 를 추정할 수 있는 확률형 자료구조 ( 1% 미만의 오차 범위 )

<br>

## 참고
[실전! Redis 활용](https://inf.run/7ctks)
