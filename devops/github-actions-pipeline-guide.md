## (devops) GitHub Actions 파이프라인 구성 시 가이드
> GitHub 인증 정보 설정, DockerHub 토큰 생성 및 GitHub secrets 지정, GitHub 커밋 아이디로 Tag 관리, DockerHub 이미지 사용

<br>

## 인증 정보 설정 ( 토큰 생성 )
> vscode 에서 github 로 push 할 때 활용

- github 우측 상단의 User 클릭하여 Settings → Developer Settings → Personal access tokens 하위 Tokens (classic) → Generate new token (classic)
- repo 와 workflow 선택하여 Generate
![image](https://github.com/user-attachments/assets/71b9c82c-ef55-4f6f-8623-75f0a5de7529)

<br>

## 도커 허브 계정 설정 ( secrets 값 지정 )
> Error: Username and password required 발생 가능

- YAML 파일 작성
```yaml  
    steps:
    ..
    - name: Login to Docker Hub
      uses: docker/login-action@v1
      with:
        username: ${{ secrets.DOCKERHUB_USERNAME }}  
        password: ${{ secrets.DOCKERHUB_TOKEN }}    
```

1. DockerHub 에서 토큰 발급 ( hub.docker.com )
- Account Settings → Security 하위 Personal access tokens → Generate New Token
![image](https://github.com/user-attachments/assets/274c0be8-f60b-4529-9cbc-8b9d6db1853d)


2. GitHub secrets 생성 ( DOCKERHUB_USERNAME, DOCKERHUB_TOKEN )
- GitHub 레포지토리 내 Settings → Secrets and variables 하위 Actions → Repository secrets → 본인의 dockerhub 계정과 위에서 생성한 토큰 각각 생성
![image](https://github.com/user-attachments/assets/d8ff4bde-2236-497b-8d43-d9d2c6cec81c)

<br>

## GitHub 커밋 아이디로 Tag 관리하기
1. tags 내 `{{ github.sha }}` 지정
```yaml
    - name: Build and Push
      uses: docker/build-push-action@v2
      with:
        context: ./frontend # Dockerfile 위치
        file: ./frontend/Dockerfile # Dockerfile 경로
        push: true  
        # 커밋 아이디를 명시하여 해당 커밋을 식별하고 추적할 수 있다. 
        tags: ${{ secrets.DOCKERHUB_USERNAME }}/leafy-frontend:${{ github.sha }}
```

2. frontend 내 소스 수정하여 push 
3. github Actions 탭 내에서 `Build and Push` 가 성공적으로 되었는 지 확인
4. DockerHub 에서 태그명이 커밋 아이디로 되었는 지 확인
![image](https://github.com/user-attachments/assets/0a30c631-82a2-490b-b5d0-83504ae8f063)

5. GitHub 레포지토리에서 커밋 아이디로 추적 가능
![image](https://github.com/user-attachments/assets/d0088774-5bef-4300-8fae-d9193ff4c179)

<br>

## DockerHub 이미지 사용하기
- 기존 YAML 
```yaml
  application-front:
    build: ./frontend
    image: front:5.0.0-compose
```
- DockerHub 이미지 사용하기
```yaml
  application-front:
    image: {계정명}/frontend:7aa3c4f142899be2a9d64ba48dbd816171b8bf12
```
- 실행하기
```bash
docker compose up
# docker compose down -v
```

<br>

## 참고
[인프런 - 개발자를 위한 쉬운 도커](https://inf.run/wHHR8) 

