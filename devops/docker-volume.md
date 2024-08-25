## (docker) 도커 볼륨
> 마운트, 도커 볼륨 및 실습하기 

<br>

## 컨테이너의 영속성(Persistence)
- 컨테이너가 삭제되거나 재생성될 경우 컨테이너 레이어가 초기화된다. ( 이미지 상태로 돌아감 ) → DB 데이터 초기화가 될 수 있다.  
- 컨테이너 환경에서는 같은 서버의 대수가 여러 개 존재 ( 트래픽 분배, 이중화 ) → 요청에 대한 응답이 서버마다 다를 경우 문제가 될 수 있다. 
- 컨테이너의 특정 디렉터리에 볼륨을 마운트해서 사용한다.
#### => **영속성이 필요한 데이터를 위해 도커 볼륨 기능 제공 → 데이터를 컨테이너 외부에 보관**

<br>

## 마운트 ( Mount)
- 마운트를 통해 외부 저장 공간을 특정 경로에 연결할 수 있다. 
- 외부 저장공간은 물리적으로 연결하거나 네트워크에 연결할 수 있다 ( ex> NFS, 한 대만 연결 가능한 USB 와 달리 NFS 는 동시에 여러 PC 를 연결할 수 있다. ) 

<br>

## 도커 볼륨 (Volume)
- 컨테이너 실행 시 볼륨을 컨테이너의 내부 경로에 마운트할 수 있다. ( USB를 꽂는 것과 유사 )
- 컨테이너가 삭제되어도 볼륨은 남아 있다. 
- 컨테이너 실행 시 다시 마운트 할 수 있다. 
- 하나의 컨테이너가 여러 개의 볼륨을 사용할 수 있다. 
- 여러 개의 컨테이너가 하나의 볼륨을 공유할 수 있다. ( 하나의 DB 데이터를 모든 DBMS 컨테이너가 공유하도록 설계 )
- 바인드 마운트 : 볼륨이 저장하는 경로에 사용자가 직접 접근하기 어려운데 HostOS 에서 직접 데이터를 관찰하고 싶은 경우 사용. 호스트 OS 의 경로를 직접 지정할 수 있다. 
```bash
# window 환경인 경우 컨테이너 내부 경로 앞에 / 하나 더 붙여야 한다. 
$ docker run -v {도커볼륨명}:/{컨테이너의 내부 경로}

# Bind Mount 사용하기
$ docker run -v {마운트할 HostOS 의 경로}:{컨테이너의 내부 경로}
```

<br>

## 실습하기
### Case1. 데이터 저장 및 공유: PostgreSQL
1. volume 생성
```shell
> docker volume create mydata

# docker volume ls 로 생성된 'mydata' 볼륨 확인
```
2. volume inspect
```shell
> docker volume inspect mydata
```
- 실행 결과
> - Driver 가 local 이라는 것은 실제 데이터가 호스트 OS 에 저장된다는 것을 의미
> - MountPoint 는 저장되는 데이터의 실제 경로

![image](https://github.com/user-attachments/assets/652d7c31-009d-40a7-8df5-e4bb874ed878) <br>

3. 볼륨을 마운트한 postgres 컨테이너 실행
```shell
> docker run -d --name my-postgres -e POSTGRES_PASSWORD=password -v mydata://var/lib/postgresql/data postgres:13
```
4. container inspect
```shell
> docker container inspect my-postgres
```
- 실행 결과

![image](https://github.com/user-attachments/assets/98d3cb77-71ea-4fbd-8833-527e3442a713) <br>

5. postgres db container 내 database 생성
```shell
> docker exec -it my-postgres psql -U postgres -c "CREATE DATABASE mydb;"
```
6. postgres container 삭제
```shell
> docker rm -f my-postgres
```
7. 동일한 마운트 경로로 컨테이너 재생성
```shell
> docker run -d --name my-postgres-2 -e POSTGRES_PASSWORD=password -v mydata://var/lib/postgresql/data postgres:13
```
8. 이전에 생성한 database 확인
```shell
> docker exec -it my-postgres-2 psql -U postgres -c "\list"
```
- 실행 결과

![image](https://github.com/user-attachments/assets/a39aff2e-f276-4b79-b533-f67a92796e7d)

<br>

### Case2. Nginx index.html 공유 ( 바인드마운트 실습 )
- 호스트OS 의 디렛터리를 2개의 컨테이너가 마운트하여 공유
- nginxA에서 변경한 파일이 nginB도 변경된다
- 바인드마운트는 별도로 볼륨이 만들어지지 않는다

1. 동일한 마운트 경로로 nginx container 2개 실행
```shell
> docker run -d -p 8000:80 --name {컨테이너명-a} -v {마운트할 실제 경로}:/usr/share/nginx/html nginx

> docker run -d -p 8001:80 --name {컨테이너명-b} -v {마운트할 실제 경로}:/usr/share/nginx/html nginx
```
2. 8000 및 8001 포트 접근하여 응답 페이지 확인 ( 실제 경로 내 파일 없는 상태 )

![image](https://github.com/user-attachments/assets/38d84d5e-a2cc-430b-a19a-75d11fa60162) <br>

3. 마운트한 실제 경로 내 index.html 파일 생성
```shell
> echo Hello Volume! > index.html
```
4. 8000 및 8001 포트 접근하여 동일한 응답 페이지 확인

![image](https://github.com/user-attachments/assets/e2eae164-dd96-4fda-b730-79aa99b8b396) <br>

5. 첫 번째 nginx 쉘 접근하여 index.html 파일 수정
```shell
> docker exec -it {컨테이너명} /bin/bash

>/ echo Bye Volume! > /usr/share/nginx/html/index.html
```
6. 8000 및 8001 포트 접근하여 동일한 응답 페이지 확인

![image](https://github.com/user-attachments/assets/612ceced-db16-49b8-8b47-d0d1e9b8c972) 

<br>

## 명령어 정리
- docker volume ls : 볼륨 리스트 조회
- docker volume inspect 볼륨명 : 볼륨 상세 정보 조회
- docker volume create 볼륨명 : 볼륨 생성
- docker volume rm 볼륨명 : 볼륨 삭제 

<br>

## 참고
[인프런 - 개발자를 위한 쉬운 도커](https://inf.run/wHHR8) 