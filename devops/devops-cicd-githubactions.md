## (devops) DevOps, CI/CD, GitHub Actions 개념
> Docker 와 DevOps, CI/CD 파이프라인, GitHub Actions 용어 정리 및 활용 방법

<br>

## Docker 와 DevOps
- DevOps 는 Development 와 Operations 의 합성어
- DevOps 는 애플리케이션과 서비스를 빠른 속도로 제공하기 위한 문화, 철학, 방식, 도구를 모두 포함한다. 
- 컨테이너, CI/CD, 자동화, MSA, IaC 등의 개념과 연관이 있다. 
- 컨테이너는 개발자의 개발 환경과 배포 환경과의 차이를 줄여 빠르고 안정적인 배포가 가능하다. 

<br>

## Docker 와 CI/CD 파이프라인
> 코드 Push → Build → Test → Deploy → Test

- 파이프라인은 소스코드에서 시작해서 배포 환경 관리까지의 모든 프로세스를 자동화하는 것을 말한다.
- 파이프라인이 없을 경우 사람이 직접 빌드 및 배포를 수행해야 하고 휴먼 에러가 발생할 가능성이 높으며 표준화가 어려워진다.
- **CI(Countinuous Integration)**: 지속적 통합, 배포 가능한 아티팩트(Jar/Image)를 빌드하는 단계
- **CD(Continous Delivery/Deployment)** : 지속적 배포, 실제 환경에 아티팩트를 배포하는 단계

<br>

## GitHub Actions
- GitHub 는 파이프라인을 구성하고 자동화할 수 있는 GitHub Actions 를 제공한다.
- GitHub 에 소스코드를 푸시하면 GitHub Actions 에서 CI/CD 파이프라인을 자동으로 실행시킬 수 있다. 
- GitHub Actions 를 사용하면 개발자의 PC 나 별도의 빌드용 서버가 없이 파이프라인을 실행할 수 있다. 
- 파이프라인은 `.github/workflows` 안에 있는 YAML 파일로 구성된다. → 소스코드의 `.github/workflows` 의 yml 파일을 GitHub 가 자동으로 인식해서 파이프라인 실행
### 용어 정리
#### 1. 러너(Runner)
- 워크플로우가 실제로 실행되는 서버
- 깃허브의 무료 러너를 사용하거나 자신의 서버에서 직접 실행 가능

#### 2. 워크플로우(Workflows) ⊃ 작업(Jobs) ⊃ 스텝(Steps)
- 하나 이상의 작업(Job) 으로 구성된 파이프라인
- `.github/workflows` 디렉토리에 YAML 파일로 정의
- 트리거를 통해 자동 실행 가능

#### 3. 트리거(Trigger)
- 특정 이벤트가 발생했을 때 워크플로우 자동 실행
- 소스코드 푸시, 특정 시간 ( ex> 매일 8시에 실행 등 )

<br>

## GitHub Actions 문법
```yml
name: # 워크플로우 이름 지정

on: # 트리거 설정

jobs: # 파이프라인이 실제로 실행하는 작업인 워크플로우를 작성
  build-and-push: # job 여러개 지정 가능
    runs-on: ubuntu-latest # 작업이 실제로 실행될 러너를 지정
    
    steps: # 작업에 해당하는 Step 여러개 지정 가능
```

### Trigger 지정 방법
- 시간 트리거 : 특정 시간에 워크플로우 실행
```yaml
on:
  schedule:
    - cron: '0 0 * * *'
```
- 푸시 트리거 : 소스코드가 변경되면 워크플로우를 실행
```yaml
on: 
  push:
    branches: # 트리거될 브랜치 지정 가능
      - [branch-name]
    paths: # 특정 경로(디렉) 지정 가능
      - [source-path] # ex> 'backend/**'
    tags: # 특정 태그 지정 가능
      - [tag-name] # ex> 'dev*'
```

### Step 지정 방법
- checkout ( GitHub 에서 제공 )
```yaml
steps:
  - name: [step-name]
    uses: actions/checkout@v2 # 사용할 Step 지정
```

- 도커 buildx 세팅 1 ( docker setup step ) : 도커의 buildx 기능 활성화
```yaml
steps:
  - name: [step-name]
    uses: docker/setup-buildx-action@v1
```

- 도커 로그인 정보 생성 : 도커 로그인 명령 실행
```yaml
steps:
  - name: [step-name]
    uses: docker/login-action@v1
    with:  # 보안상의 이유로 소스에 저장할 수 없기 때문에 GitHub secrets 활용
      username: ${{ secrets.DOCKERHUB_USERNAME }} # 허브 사용자명
      password: ${{ secrets.DOCKERHUB_TOKEN }} # 허브 인증 토큰
```

- 도커 buildx 세팅 2 ( docker build and push step ) : 이미지 빌드 및 푸시
```yaml
steps:
  - name: [step-name]
    uses: docker/build-push-action@v2
    with: 
      context: [빌드컨텍스트]
      file: [도커파일위치]
      push: [이미지푸시여부] # boolean
      tags: [이미지태그]
      platforms: [CPU아키텍처]
```

<br>

## 참고
[인프런 - 개발자를 위한 쉬운 도커](https://inf.run/wHHR8) 