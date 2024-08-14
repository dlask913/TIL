## (docker) 도커 네트워크 1
> 컨테이너 가상화와 가상 네트워크, 도커의 SDN 활용, 도커 네트워크 실습

<br>

### 컨테이너 가상화와 가상 네트워크
- 컨테이너 가상화 : 서버 한 대를 여러 컨테이너로 격리하여 각각의 컨테이너가 독립적인 환경에서 실행되게 한다.
- 가상 네트워크 : 서버 한 대 안에서 여러 네트워크를 논리적으로 구성하여, 각 컨테이너가 격리된 네트워크 환경에서 통신할 수 있다. → Docker 에서 활용

<br>

## SDN ( Software Defined Network )
: 논리적으로 네트워크 환경을 구성하는 기술
### 도커의 SDN 활용
1. 가상의 네트워크 브릿지 (docker0) 생성 : Docker 를 설치하고 실행하면 기본적으로 'docker0' 라는 브릿지 네트워크가 생성된다. 이는 각 컨테이너에 가상 IP 주소를 할당하는 역할을 한다. <br>
![image](https://github.com/user-attachments/assets/f0d6452d-1d2b-42b0-9f5a-5b5096c03784)

2. 컨테이너에 가상의 IP 할당 : Docker 는 브릿지 네트워크의 IP 주소 범위 내에서 각 컨테이너에 IP 주소를 할당한다. 
3. 브릿지를 통해 컨테이너 간 동일 네트워크 내의 컨테이너들끼리 통신 전달이 가능하다.

<br>

## 도커 네트워크 실습
### 브릿지 네트워크 생성하기
```bash
$ docker network create --driver bridge --subnet 10.0.0.0/24 --gateway 10.0.0.1 second-bridge
```
- --driver bridge : bridge 로 생성
- --subnet 10.0.0.0/24 : container 에 할당할 ip 대역
- --gateway 10.0.0.1 : bridge 주소

### 네트워크 정해서 컨테이너 실행하기
```bash
$ docker run -it --network second-bridge --name ubuntu [registry계정명/이미지] bin/bash
# bin/bash 를 통해 바로 접속
```
- docker container inspect 결과 ( second-bridge 확인 )
```bash
$ docker container inspect ubuntu
```
![image](https://github.com/user-attachments/assets/a2cf5fe4-63dd-410e-b072-5c6552880e63)

### 동일 네트워크 내에서 접속 확인하기
```bash
# 1. 동일 네트워크의 컨테이너 실행 ( 네트워크 설정 없다면 기본 bridge network )
$ docker run -it --name ubuntuA [registry계정명/이미지] bin/bash
$ docker run -it --name ubuntuB [registry계정명/이미지] bin/bash

# 2. ubuntuA 의 IP 주소 확인
$ docker container inspect ubuntuA

# 3. ubuntuB 쉘에서 ping 날려 통신 확인
> ping [ubuntA-ip주소]
```

<br>

## 명령어 정리
- docker network ls : 네트워크 리스트 조회
- docker network inspect [네트워크명] : 네트워크 상세 정보 조회
- docker network create [네트워크명] : 네트워크 생성
- docker network rm [네트워크명] : 네트워크 삭제

<br>

## 참고
[인프런 - 개발자를 위한 쉬운 도커](https://inf.run/wHHR8) 