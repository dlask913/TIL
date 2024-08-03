## (docker) 클라우드 네이티브 애플리케이션과 컨테이너 애플리케이션 구성 1
> 클라우드 네이티브 애플리케이션과 컨테이너 애플리케이션 구성 중 DB 컨테이너 구성하기.

<br>

## 클라우드 네이티브 애플리케이션
### 클라우드 
- 다른 회사의 서버를 빌려서 운영 ( ex> Azure, AWS, NCP, Google Cloud Platform, .. )
- 다른 회사가 모두에게 서버를 빌려줄 경우 : 퍼블릭 클라우드
- 다른 회사가 특정 조직에게만 서버를 빌려줄 경우 : 프라이빗 클라우드 
- 사용 요청 즉시 서버를 생성 ( Provisioning ) 
- 실제 사용한 시간 만큼만 비용 지불

### 클라우드를 사용하는 이유
1. 트래픽이 증가할 때 빠르게 대처할 수 있는가 ? ( **확장성, Scalability** ) <br>
: 온프레미스에서는 서버가 미리 준비되어 있지 않은 경우 새로운 서버를 증가하는데 오랜 시간이 소요되지만, 클라우드 환경에서는 서버 추가가 10분 내로 이루어진다.
→ 트래픽이 증가할 때 서버를 증가하여 트래픽을 수용할 수 있게 된다.
2. 장애 발생 시 빠르게 복구할 수 있는가? ( **복원력, Resilience** ) <br>
: 백업 및 복구가 빠르게 이루어질 수 있으며 장애에 대응하기 위한 다양한 지역에 서버를 구축할 수 있다. ( Disaster Recovery )
3. 운영 비용을 효율적으로 운영할 수 있는가? <br>
: 사용한 만큼한 지불할 수 있기 때문에 운영 비용에 더 효율적이다. 그러나 비효율적으로 운영할 경우 더욱 비용이 발생할 수 있으니 주의가 필요하다.

### 클라우드 네이티브 애플리케이션 ( [참고](https://12factor.net/ko/) )
: 클라우드 환경을 더 잘 활용할 수 있는 애플리케이션 구조
1. **MSA** : 트래픽 증가에 빠르게 대처하기 위해선 애플리케이션이 MSA 구조로 개발되어야 한다. 
2. **컨테이너** : 컨테이너를 활용해 실행 환경에 종속되지 않는 동작이 보장되어야 한다. 
3. **상태비저장 ( Stateless )** : 애플리케이션 서버는 상태를 가지지 않아야 하며, 그러한 애플리케이션은 어디에나 즉시 배포될 수 있다. 
4. **DevOps 및 CI/CD** : 배포가 자동화되어야 하고 빠르게 릴리즈가 수행되어야 한다. 

<br>

## 컨테이너 애플리케이션 구성하기

### 개발 환경
- Vue.js 3 기반 프론트엔드 소스, Nginx 웹 서버 배포
- Spring Boot 백엔드 애플리케이션, Spring Boot Tomcat WAS 서버
- PostgreSQL Database 서버, 초기 데이터 저장

### 컨테이너 구성 순서
1. PostgreSQL DB 서버 구성 및 실행
2. Spring boot 백엔드 애플리케이션 이미지 빌드 및 실행
3. Nginx 서버 이미지 빌드 및 실행

<br>

### [1] PostgreSQL 컨테이너 구성하기
1. OS 구성 및 PostgreSQL 설치 
```dockerfile
FROM postgres:13
```
2. 환경 설정 파일 작성
```dockerfile
COPY ./config/postgresql.conf /etc/postgresql/custom.conf
```
3. SQL 문 작성 
```dockerfile
COPY ./init/init.sql /docker-entrypoint-initdb.d/
```
4. 데이터베이스 실행 명령
```dockerfile
CMD ["postgres", "-c", "config_file=/etc/postgresql/custom.conf"]
```
- Dockerfile
```Dockerfile
# PostgreSQL 13 버전을 베이스 이미지로 사용
FROM postgres:13
  
# init.sql 파일을 /docker-entrypoint-initdb.d/ 로 복사,
# /docker-entrypoint-initdb.d/ 에 있는 sql 문은 컨테이너 초기 실행 시 자동 실행
COPY ./init/init.sql /docker-entrypoint-initdb.d/
  
# postgresql.conf 파일을 /etc/postgresql/postgresql.conf 로 복사,
# 기본 설정 파일을 덮어쓰기하여 새로운 설정 적용
COPY ./config/postgresql.conf /etc/postgresql/custom.conf
  
# 계정정보 설정
ENV POSTGRES_USER=myuser
ENV POSTGRES_PASSWORD=mypassword
ENV POSTGRES_DB=mydb
  
EXPOSE 5432
  
CMD ["postgres", "-c", "config_file=/etc/postgresql/custom.conf"]
```
- 실행 결과 
```bash
# 1. 빌드
$ docker build -t [레지스트리명/이미지명] [현재위치]

# 2. 컨테이너로 실행
$ docker run -d --name [컨테이너명] --network [네트워크명] [위에서 빌드한 이미지명]

# 3. Postgres DB 접속
$ docker exec -it [컨테이너명] su postgres bash -c "psql --username=myuser --dbname=mydb"

# 4. 쿼리 실행
mydb> SELECT * FROM [테이블명]; 
```

<br>

## 도커 명령어 정리
- docker network create 네트워크명 : 네트워크 생성
- docker logs -f 컨테이너명 : 컨테이너 로그 조회
- docker cp 원본위치 복사위치 : 컨테이너와 호스트 머신 간 파일 복사
- docker cp 컨테이너명:원본위치 복사위치 : 컨테이너 → 호스트머신으로 파일 복사
- docker cp 원본위치 컨테이너명:복사위치 : 호스트머신 → 컨테이너로 파일 복사
- docker image history 레지스트리계정명/이미지명 : 이미지에 대한 히스토리(레이어) 확인

<br>

## 참고
[인프런 - 개발자를 위한 쉬운 도커](https://inf.run/wHHR8) 