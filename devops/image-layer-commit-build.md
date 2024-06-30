## (docker) 이미지 레이어 구조와 이미지 생성 방법
> 이미지 레이어 구조, 이미지 커밋과 빌드

<br>

## 이미지 레이어 구조
- Docker 이미지는 여러 레이어로 구성되며, 각 레이어는 이전 레이어 위에 쌓여 구성된다.
- 이미지를 수정하거나 업데이트할 때 변경 사항은 새로운 레이어로 추가되어 저장된다.
- 컨테이너 실행 시 읽기, 쓰기 가능한 새로운 레이어가 추가된다. ( 컨테이너 레이어 )
> ※ 이미지의 레이어와 컨테이너의 레이어
 > - 이미지의 레이어 : 컨테이너를 실행하기 위한 세이브 포인트 역할
 > - 컨테이너의 레이어 : 컨테이너 실행 후 발생하는 모든 데이터 변경을 실시간으로 기록한다. 컨테이너가 종료되면 이 레이터의 데이터는 일반적으로 삭제되기 때문에 영구 저장이 필요한 데이터는 다른 방법으로 관리해야 한다.

### 레이어 구조와 이미지 관리 기법
- **Layering** : 각 레이어는 이전 레이어 위에 쌓이며, 여러 이미지 간에 공유가 가능하다. 레이어 방식은 중복 데이터를 최소화하고 빌드 속도를 높이며 저장소를 효율적으로 사용할 수 있게 해준다. 
- **Copy-on-Write (CoW) 전략** : 다음 레이어에서 이전 레이어의 특정 파일을 수정 할 때 해당 파일의 복사본을 만들어서 변경 사항을 적용한다. → 원래 레이어는 수정되지 않고 그대로 유지된다.
- **Immutable Layers (불변 레이어)** : 이미지의 각 레이어는 불변으로, 한 번 생성되면 변경되지 않는다. 이렇게 이미지의 일관성을 유지하고 여러 컨테이너에서 안전하게 공유할 수 있게 된다.
- **Caching (캐싱)** : 레이어를 캐시하여 이미 빌드된 레이어를 재사용할 수 있다. 이는 이미지 빌드 시간을 크게 줄여주며 같은 레이어를 사용하는 여러 이미지에서 효율적으로 작동한다.

<br>

## 이미지 커밋과 빌드
###  이미지 커밋 
: **현재 컨테이너의 상태를 이미지로 저장**하여 새로운 이미지 생성
1. Nginx 이미지를 컨테이너로 실행
```bash
$ docker run -it --name officialNginx nginx bin/bash
```
2. 컨테이너의 내부 파일 변경
```shell
> echo hello-my-nginx > /usr/share/nginx/html/index.html
```
3. Commit 으로 새로운 이미지 저장
> powershell 로 하면 "docker commit" requires at least 1 and at most 2 arguments. 발생하여 git bash 로 실행
```bash
$ docker commit -m "edited index.html by devwiki" -c 'CMD ["nginx", "-g", "daemon off;"]' officialNginx dlask913/commitnginx
```

### 이미지 빌드
> - IaC : 인프라 상태를 코드로 관리
> - Dockerfile : 이미지를 만드는 단계를 기재한 명세서 <br>
> → 도커는 IaC 방식에 따라서 이미지를 도커 파일이라는 소스 코드로 관리할 수 있다. <br>

: **Dockerfile 을 통해** 이미지 저장하여 새로운 이미지 생성
1. 원하는 이미지 상태를 Dockerfile 코드로 작성
```Dockerfile
""" 기본 Dockerfile 지시어
- FROM 이미지명 : 베이스 이미지 지정
- COPY 파일경로 복사할경로 : 파일을 레이어에 복사
- CMD ["명령어"] : 컨테이너 실행 시 명령어 지정
"""
FROM nginx:1.23

# index.html 의 위치는 Dockerfile 위치와 동일
COPY index.html /usr/share/nginx/html/index.html

CMD ["nginx", "-g", "daemon off;"]
```
2. docker build 명령어를 통해 이미지 생성
```shell
$ docker build -t dlask913/buildnginx . # . 은 현재 위치를 의미
```
3. 생성한 이미지 컨테이너로 실행
```shell
$ docker run -d -p 80:80 --name build-nginx dlask913/buildnginx
```
<br>

## 도커 명령어 정리
- docker image history 이미지명 : 이미지의 레이어 이력 조회
- docker run -it --name 컨테이너명 이미지명 bin/bash : 컨테이너 실행과 동시에 터미널 접속
- docker commit -m 커밋명 실행중인-컨테이너명 생성할-이미지명 : 실행 중인 컨테이너를 이미지로 생성
- docker build -t 이미지명 Dockerfile-경로 : 도커파일을 통해 이미지 빌드

<br>

## 참고
[인프런 - 개발자를 위한 쉬운 도커](https://inf.run/wHHR8) 