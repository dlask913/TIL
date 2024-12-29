## (redis) AWS 에서 Redis 활용하기
> EC2, RDS, Spring Boot, Redis 세팅하기, Redis 적용하기 전후 성능 비교 (Postman)

<br>

## EC2, RDS, Spring Boot, Redis 세팅하기
### EC2 인스턴스 생성
- OS 이미지 : Ubuntu 24.04 LTS ( default 설정 )
- 인스턴스 유형 : t3a.small 
- 키 페어의 경우, 테스트이기 때문에 없이 진행
- 네트워크 설정 : 퍼블릭 IP 활성화, 보안 그룹 - 사용자 지정 TCP / 8080 / 위치무관 추가 ( 애플리케이션 접근 포트 )
- 스토리지 구성 : 8 GiB gp3 루트 볼륨 ( default 설정 )

### RDS 데이터베이스 생성
- 데이터베이스 생성 방식 : 표준 생성
- 엔진 옵션 : MySQL
- 템플릿 : 프리 티어
- 자격 증명 설정 : admin / password
- 인스턴스 구성 : db.t3.micro ( default 설정 )
- 스토리지 : 범용 SSD(gp2) / 20 ( default 설정 )
- 연결 : EC2 컴퓨팅 리소스에 연결 안 함, 퍼블릭 액세스 - 예, 추가 구성 - DB 포트 3306
- 추가 구성 : 초기에 생성되어야하는 데이터베이스 지정 ( ex> mydb ), 테스트이기 때문에 자동 백업은 비활성화
- 생성 후 보안그룹 내 인바운드 규칙에서 3306 포트를 모든 사용자에게 열어주기
![image](https://github.com/user-attachments/assets/fdda190c-b9cb-4bff-9ed3-f85c9215e66d)

### 애플리케이션 내 datasource 재설정
> 아래 datasource 맞게 더미 데이터도 재생성

```yaml
spring:  
  config:  
    activates:  
      on-profile: prod  
  datasource:  
    url: jdbc:mysql://{RDS의 엔드포인트 주소}:3306/mydb  
    username: admin # RDS 자격증명에서 설정한 아이디
    password: password # RDS 자격증명에서 설정한 비밀번호
```

### EC2 환경 세팅 및 애플리케이션 실행
1. 생성한 EC2 인스턴스에 연결하여 콘솔 진입
2. 소프트웨어 패키지의 최신 정보 업데이트 
```shell
sudo apt update
```
3. redis 설치
```shell
sudo apt install redis
```
4. redis 접속하여 설치 확인
```shell
$ redis-cli
> PING
# PONG
```
5. jdk17 다운로드
```shell
sudo apt install openjdk-17-jdk
# java -version 으로 확인
```
6. 애플리케이션이 위치한 repository 가져오기
```shell
git clone [repo 주소] # private repo 인 경우, username / token 입력 필요
```
7. repo 디렉토리로 들어가 애플리케이션 빌드
> Permission Denied 인 경우, `chmod +x ./gradlew` 로 실행 권한 부여

```shell
./gradlew clean build -x test # 테스트 제외
```
8. build/libs 폴더에 접근하여, prod 로 jar 애플리케이션 실행
```shell
java -jar -Dspring.profiles.active=prod [애플리케이션-SNAPSHOT].jar
```

<br>

## Redis 적용하기 전후 성능 비교
1. vi 로 `@Cacheable` 을 제거하고 Postman 을 활용해 API 요청하기.
- 실행 결과 : 10번 연달아 실행했을 때 평균적으로 500ms 정도
![image](https://github.com/user-attachments/assets/63be4876-969e-45e1-baa5-6e5a33d3bb9d)

2.  `@Cacheable` 적용된 상태에서 API 요청하기
- 실행 결과 : 초기 실행 이후, 10번 연달아 실행했을 때 평균적으로 25ms 정도
![image](https://github.com/user-attachments/assets/22280d1e-ea6e-49d2-8620-a75e34d58e7f)

<br>

## 참고
[인프런 - 비전공자도 이해할 수 있는 Redis 입문/실전 (조회 성능 최적화편)](https://inf.run/Pupon)