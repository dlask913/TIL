# (redis) Redis 데이터 타입별 활용 사례
> Redis 데이터 타입별 활용 사례

<br>

## String
### 1. One-Time Password 
- 사용자 인증을 위해 한 번만 사용할 수 있는 임시 비밀번호
- OTP를 Redis에 사용자 식별자와 함께 저장하고, TTL 기능을 사용하여 유효 시간을 설정한다.
- Redis에서 해당 사용자 식별자로 저장된 OTP를 조회하여 제출된 OTP와 비교한다.
```shell
SETEX [key] [TTL(second)] [value] # SET EXPIRE
SET [key] [value] EX [TTL(second)]
```

### 2. Distributed Lock ( [공식문서](https://redis.io/docs/manual/patterns/distributed-locks/) )
- 분산 환경에서 다수의 프로세스가 동일한 자원에 접근할 때 동시성 문제를 해결
- Lock 에 키를 생성하여 키가 존재하지 않는 경우 요청 허용, 존재하는 경우 대기시키고 기존 요청이 끝나면 키를 제거하여 다른 요청을 허용한다.
```shell
SET [lock_key] [unique_value] NX # lock 고유키, 사용자 식별자, 키가 존재하지 않을 때만
```

### 3. Rate Limiter - Fixed Rate Limiting
- 고정된 시간 안에 요청 수를 제한하는 방법
** Rate Limiter : 시스템 안정성/보안을 위해 요청의 수를 제한( IP-Based, User-Based, Application-Based 등 )

<br>

## List ( [공식문서](https://redis.io/docs/data-types/lists/) )
### 1. SNS Activity Feed
- Activity Feed : 사용자 또는 활동이나 업데이트를 시간 순으로 정렬하여 보여주는 기능
- Fan-Out : 단일 데이터를 한 소스에서 여러 목적지로 동시에 전달하는 메시징 패턴
```shell
LPUSH [key] [element] # 사용자의 새로운 활동을 피드에 추가
LRANGE [key] [start] [stop] # 사용자 피드의 최근 활동 검색
LTRIM [key] [start] [stop] # 피드의 크기를 제한하여 오래된 활동 자동 제거
```

<br>

## Set
### 1. Shopping Cart
- 사용자가 구매를 원하는 상품을 임시로 모아두는 가상의 공간
- 수시로 변경이 발생할 수 있고 실제 구매로 이어지지 않을 수 있다는 것이 특징
```shell
SADD cart:user1 product1 product2 # key, value1, value2
SMEMBERS cart:user1 # key 에 담긴 상품들 모두 조회
```

<br>

## Hash
### 1. Login Session
- 사용자의 로그인 상태를 유지하기 위한 기술
- 동시 로그인 제한 : 세션의 개수를 제한을 통해 동시에 로그인 가능한 디바이스 수 제한

<br>

## Sorted Set
1. Sliding Window Rate Limiter ( vs. Fixed Window )
- 시간에 따라 Window를 이동시켜 동적으로 요청 수를 조절하는 기술 

<br>

## Geospatial
### 1. Geofencing
- 위치를 활용하여 지도 상의 가상의 경계 또는 지리적 영역을 정의하는 기술
- GEOADD : 지리적 위치 데이터 추가
- GEORADIUS : 특정 반경 내의 위치 데이터 조회
```shell
GEOADD [key] [longitude1] [latitude1] [member1] [longitude2] [latitude2] [member2]..
GEORADIUS [key] [longitude] [latitude] [radius]
```

<br>

## Bitmap
### 1. User Online Status
- 사용자의 현재 상태를 표시하는 기능
- 실시간성을 완벽히 보장하지는 않으며 수시로 변경된다.
- Onine 일 때는 비트 값이 1, Offline 인 경우 0

<br>

## HyperLogLog
### 1. Visitors Count Approximation
- 방문자 수(또는 특정 횟수) 를 대략적으로 추정하는 경우
- 정확한 횟수가 아닌 대략적인 수치만 알고자 하는 경우
```shell
PFADD [key] [value1] [value2] ..
PFCOUNT [key]
```

<br>

## BloomFilter
### 1. Unique Events 
- 동일 요청이 중복으로 처리되지 않기 위해 해당 item 이 중복인지 확인

<br>

## 참고
[실전! Redis 활용](https://inf.run/7ctks)