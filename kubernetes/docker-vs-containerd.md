## (k8s) Docker vs ContainerD
>  Docker, ContainerD (ctr, nerdctl, crictl)

<br>

## Docker vs ContainerD
- Kubernetes 가 초기에는 Docker Runtime 만을 기본으로 사용했고 이후 다른 컨테이너 런타임이 도입되었다.
- Kubernetes 사용자에게 Docker 외 다양한 컨테이너 런타임과 작업할 수 있게 CRI 인터페이스가 도입되었고 OCI 표준을 준수한다.
- OCI(Open Container Initiative) 는 imagespec 과 runtimespec 으로 구성되며, 이미지를 어떻게 만들지 정의하고 컨테이너 런타임에 대한 표준을 정의한다. 
- Docker 는 CRI 도입 이전에 만들어졌기 때문에 Kubernetes 는 Docker 를 지원할 수 있는 dockershim 을 도입했다. → v1.24 이후 지원 종료

<br>

### Docker
- CLI, API, Build, Volumes, Auth 등 다양한 도구와 ContainerD 를 포함한다.
- Dokcer 가 만든 이미지는 OCI 표준을 준수하므로 dockershim 을 제거하여 연결이 끊기더라도 이미지는 동작한다.
- Docker 자체로는 컨테이너 실행을 위한 런타임을 제공하지 않는다.

<br>

### ContainerD
- CRI 호환이 가능하고 다른 런타임처럼 Kubernetes 와 직접적으로 작업할 수 있다. 
- Docker 와 독립적으로 동작하며 Docker 설치 없이도 사용이 가능하다.
- 컨테이너 런타임에 필요한 핵심 기능들을 제공한다.

#### 1. CLI - ctr 
- 디버깅을 위한 목적으로 설계되었다. ( 잘 사용 X )
- ContainerD 를 위한 제한된 기능만 지원하며 사용자 친화적이지 않다. 
- 고급 기능을 사용하고 싶다면 직접 API 를 호출해야한다. 
- Purpose: Debugging, Community: CotainerD, Works With: ContainerD
- 사용 예시
```shell
$ ctr images pull [이미지주소] # 이미지 다운로드
$ ctr run [이미지] [컨테이너] # 컨테이너 실행
```

#### 2. CLI - nerdctl
- Docker 와 유사한 CLI 를 제공하며 Docker 가 지원하는 대부분의 옵션을 제공한다. 
- ContainerD 의 최신 기능을 지원한다. 
- Purpose: General Purpose, Community: CotainerD, Works With: ContainerD
- 사용 예시
```shell
$ nerdctl run --name webserver -p 80:80 -d nginx # 컨테이너 실행
```

#### 3. CLI  crictl ( [공식문서](https://kubernetes.io/docs/tasks/debug/debug-cluster/crictl/) )
> ContainerD ↔ crictl ↔ CRI ↔ Kubernetes 

- CRI 호환 컨테이너 런타임과 상호작용한다. 
- ContainerD 뿐 아니라 다른 컨테이너 런타임에 걸쳐 작동한다. 
- 다른 Utility 와의 차이점은 컨테이너 생성에 이상적이지 않다는 것이다.
- kubelet 과 함께 동작하며 특별한 디버깅 목적으로만 사용한다. 
- Purpose: Debugging, Community: Kubernetes, Works With: All CRI Compatible Runtimes
- 사용 예시 
```shell
$ crictl # 실행
$ crictl pull busybox # 이미지 다운로드
$ crictl images # 이미지 조회
```

<br>

## 참고
[Kubernetes Certified Application Developer (CKAD) with Tests](https://www.udemy.com/share/1013BQ3@FHcQPh5fdtPOTP1ZXYZVcotPtN9ZvIN1IS37fa49ax7L0Kti3Q1cVKrL8WjJxV0YjA==/)