## (docker) 이중화 DB 구성 및 컨테이너 애플리케이션 최적화
> Postgres 이중화 DB 구성 및 컨테이너 애플리케이션 최적화 ( 리소스 제한, JVM 튜닝 )

<br>

## 이중화 DB 구성

### 이중화를 구성해야하는 이유 
- 단일 서버 구성 시 단일 서버에 장애가 생기면 장애로 이어진다.
- 서버 이중화 ( Redundancy ) 구성 시 하나의 서버가 실패해도 다른 서버가 동일한 역할을 수행하여 고가용성을 보장한다. 
### 이중화 DB 를 구성하는 방법
####  동시에 같은 볼륨을 사용한다. 
- 구성이 간단하지만 볼륨에 문제가 생길 경우 대처가 어렵다.
- 볼륨의 성능에 부하가 생길 수 있다. 
#### 각각의 컨테이너에 별도의 볼륨을 연결한다.
- 데이터의 싱크를 맞추는 처리를 별도로 해야 한다. ( 데이터 동기화 필요 )

### 별도 볼륨을 연결하는 경우, 데이터 동기화 방법
1. Primary-Standby 복제 구조 
- Primary 서버에만 쓰기 작업을 수행하며, Primary 의 상태를 Standby 에 복제한다. 
- Standby 서버는 읽기 전용으로만 사용되며 일기 전용 Standby 서버를 여러 대 사용할 수 있다. 
2. Primary-Primary 복제 구조 
- 모든 서버에 읽기/쓰기 작업을 수행한다. 
- 여러 서버에서 동시에 쓰기 작업이 일어나기 때문에 동기화 구성 작업이 복잡하다.

<br>

## 이중화 DB 구성하기
1. 테스트용 네트워크 생성
```powershell
> docker network create postgres
```
2. Primary 노드 실행
```powershell
> docker run -d --name postgres-primary-0 --network postgres -v postgres_primary_data://bitnami/postgresql -e POSTGRESQL_POSTGRES_PASSWORD=adminpassword -e POSTGRESQL_USERNAME=myuser -e POSTGRESQL_PASSWORD=mypassword -e POSTGRESQL_DATABASE=mydb -e REPMGR_PASSWORD=repmgrpassword -e REPMGR_PRIMARY_HOST=postgres-primary-0 -e REPMGR_PRIMARY_PORT=5432 -e REPMGR_PARTNER_NODES=postgres-primary-0,postgres-standby-1:5432 -e REPMGR_NODE_NAME=postgres-primary-0 -e REPMGR_NODE_NETWORK_NAME=postgres-primary-0 -e REPMGR_PORT_NUMBER=5432 bitnami/postgresql-repmgr:15
```
3. Standby 노드 실행
```powershell
> docker run -d --name postgres-standby-1 --network postgres -v postgres_standby_data:/bitnami/postgresql -e POSTGRESQL_POSTGRES_PASSWORD=adminpassword -e POSTGRESQL_USERNAME=myuser -e POSTGRESQL_PASSWORD=mypassword -e POSTGRESQL_DATABASE=mydb -e REPMGR_PASSWORD=repmgrpassword -e REPMGR_PRIMARY_HOST=postgres-primary-0 -e REPMGR_PRIMARY_PORT=5432 -e REPMGR_PARTNER_NODES=postgres-primary-0,postgres-standby-1:5432 -e REPMGR_NODE_NAME=postgres-standby-1 -e REPMGR_NODE_NETWORK_NAME=postgres-standby-1 -e REPMGR_PORT_NUMBER=5432 bitnami/postgresql-repmgr:15
```
4. 각 컨테이너 로그 확인
```powershell
> docker logs -f postgres-primary-0
> docker logs -f postgres-standby-1
```
5. Primary 노드에 테이블 생성 및 데이터 삽입
```powershell
> docker exec -it -e PGPASSWORD=mypassword postgres-primary-0 psql -U myuser -d mydb -c "CREATE TABLE sample (id SERIAL PRIMARY KEY, name VARCHAR(255));"

> docker exec -it -e PGPASSWORD=mypassword postgres-primary-0 psql -U myuser -d mydb -c "INSERT INTO sample (name) VALUES ('John'), ('Jane'), ('Alice');"
```
6. Standby 노드에 데이터가 동기화되어 있는 지 확인
```powershell
> docker exec -it -e PGPASSWORD=mypassword postgres-standby-1 psql -U myuser -d mydb -c "SELECT * FROM sample;"
```
7. 환경 정리
```powershell
> docker rm -f postgres-primary-0 postgres-standby-1

> docker volume rm postgres_primary_data postgres_standby_data

> docker network rm postgres
```

<br>

## 컨테이너 애플리케이션 최적화
### 1. 컨테이너가 사용할 수 있는 리소스 사용량을 제한
```bash
# docker run --help 로 확인 가능
$ docker run .. --cpus={CPUcore수} # 컨테이너가 사용할 최대 CPU 코어 수 정의
$ docker run .. --memory={메모리용량} # 컨테이너가 사용할 최대 메모리 정의
```
- LIMIT 에 지정한 CPU 보다 사용량이 초과할 경우, CPU Throttling 발생 ( 컨테이너의 CPU 사용을 제한 ) <br> → **애플리케이션 성능 저하**
- LIMIT 에 지정한 Memory 보다 사용량이 초과할 경우, OOM ( Out of Memory ) Killer 프로세스가 실행되고 **컨테이너가 강제로 종료**된다. <br> → stats 및 events 명령어로 확인
- 실습
```bash
# 1-1. 제한없이 컨테이너 생성
$ docker run -d --name {컨테이너명} {이미지명}
# 1-2. 메모리 및 CPU 확인 ( 0은 제한 없음을 의미 )
$ docker inspect {컨테이너명} | grep -e Memory -e Cpus
            "Memory": 0,
            "NanoCpus": 0,
            "CpusetCpus": "",        
            "CpusetMems": "",        
            "MemoryReservation": 0,  
            "MemorySwap": 0,
            "MemorySwappiness": null,

# 2-1. 리소스 제한하여 컨테이너 생성
$ docker run -d --name {컨테이너명} --cpus=0.5 --memory=256M {이미지명}
# 2-2. 메모리 및 CPU 확인 ( byte 단위 )
$ docker inspect {컨테이너명} | grep -e Memory -e Cpus
            "Memory": 268435456,
            "NanoCpus": 500000000,
            "CpusetCpus": "",
            "CpusetMems": "",
            "MemoryReservation": 0,
            "MemorySwap": 536870912,
            "MemorySwappiness": null,
```

### 2. 자바 가상 머신 (JVM) 튜닝
- JVM(Java Virtual Machine) 은 자바를 실행할 수 있는 환경이다. 
- 자바 애플리케이션이 사용할 수 있는 메모리 영역인 힙(Heap) 메모리를 별도로 관리해야 한다. ( 전체 서버 메모리의 50~80% 할당하는 것이 일반적 )
- JRE(Java Runtime Environment) 에서 java -jar 명령어로 app.jar 파일을 실행 시킬 때 JVM 위에서 애플리케이션이 실행하게 된다. 따라서 JVM 의 Heap 메모리를 **컨테이너에 할당된 메모리에 맞추어 자동으로 조절**하게끔 환경 변수를 추가한다. ( Java 8u131 부터 지원, Java 10 이상에서는 기본 활성화 )
```powershell
# 힙메모리 최대 값을 4G로 지정하여 애플리케이션 실행 → 비효율적
> java -jar -Xmx=4G app.jar 

# JVM 튜닝을 위한 환경 변수 추가 ( Xmx 옵션을 지정하면 안됨 )
> java -jar app.jar -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap
```

<br>

## 도커 명령어
- docker stats ( 컨테이너명/ID ) : 컨테이너의 리소스 사용량 조회
- docker events : HostOS 에서 발생하는 이벤트 로그 조회

<br>

## 참고
[인프런 - 개발자를 위한 쉬운 도커](https://inf.run/wHHR8) 