## (docker) 도커 네트워크 2
> 도커의 네트워크 드라이버, 포트포워딩, 도커 DNS

<br>

## 도커의 네트워크 드라이버
#### 도커 네트워크 (Bridge)
: 도커 브릿지를 활용한 컨테이너간 통신이 가능하고 NAT 및 포트포워딩 기술을 활용해 외부 통신을 지원한다.
#### 호스트 네트워크 (Host)
: 호스트의 네트워크를 공유하고 모든 컨테이너는 호스트 머신과 동일한 IP를 사용하며 포트 중복이 불가능하다.
#### 오버레이 네트워크 (Overlay)
: 호스트 머신이 다수일 때 사용하여 여러 호스트에 걸쳐 컨테이너가 통신할 수 있도록 지원한다. ( Kubernetes 에서 사용 )
#### Macvlan 네트워크 
: 컨테이너에 실제 MAC 주소를 할당하여 물리 네트워크 인터페이스에 직접 연결한다.

<br>

## 포트포워딩
- 컨테이너가 호스트의 네트워크에 연결되었을 때, 컨테이너의 내부 네트워크 IP는 외부에서 직접 접근할 수 없다.
- 컨테이너의 IP 는 내부 가상 네트워크에서만 사용되는 IP 로, 실제 물리적인 네트워크에서 접근하기 위해서는 포트포워딩이 필요하다.
```bash
$ docker run -p [HostOS의-포트]:[컨테이너의-포트]
```

### 실습
```bash
$ docker run -d -p 8001:80 --name nginx nginx
```
- localhost:8001 접속하여 실행 결과 확인
![image](https://github.com/user-attachments/assets/fdaea0db-77d8-45fb-aa78-05aebc8866a9)

<br>

## 가상네트워크와 DNS
- 도커는 기본적으로 컨테이너들이 사용할 수 있는 DNS 서버를 제공한다.
- 도커의 DNS 서버는 컨테이너 이름과 IP 주소를 매핑하여 컨테이너들이 서로 이름을 통해 통신할 수 있도록 한다.
- 외부 DNS 서버와도 연동이 가능하여 도커 컨테이너는 인터넷상의 다른 도메인도 접근할 수 있다.
- 사용자가 생성한 브리지 네트워크 내에서는 DNS 기능이 제공되며, 브리지 내의 컨테이너 이름을 통해 통신이 가능히다. 

### 실습
```bash
# 1. 브릿지 네트워크 생성
$ docker network create --driver bridge --subnet 10.0.0.0/24 second-bridge

# 2-1. containerA 생성하여 쉘 접속
$ docker run -it --network second-bridge --name containerA [registry계정명]/이미지명 bin/bash

# 2-2. containerA 도커가 제공하는 DNS 정보 확인 
# → 실행 결과: nameserver 127.0.0.11
> cat /etc/resolv.conf

# 3. containerA 와 같은 네트워크로 containerB 생성
$ docker run -d --network second-bridge --name containerB [registry계정명]/이미지명

# 4. containerA 에서 컨테이너 이름을 사용하여 통신 확인
$ ping containerB
```

- 실행 결과

![image](https://github.com/user-attachments/assets/c907382c-7796-476f-8caa-9f5f5062538e)

<br>

## 참고
[인프런 - 개발자를 위한 쉬운 도커](https://inf.run/wHHR8) 