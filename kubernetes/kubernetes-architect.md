## (k8s) Kubernetes Architect
>  Kubernetes Architect, Master Node 와 Worker Node, Kubectl

<br>

## Kubernetes Architect

### Nodes
- kubernetes 가 설치되는 물리적, 가상의 머신으로 컨테이너를 실행한다.
- 노드가 하나 일 때 실패하면 애플리케이션이 다운되기 때문에 둘 이상의 노드가 권장된다.
### Cluster
- 노드의 집합으로, 클러스터 내 한 노드가 실패해도 다른 노드를 통해 애플리케이션을 실행하여 서비스 가용성을 유지할 수 있다. 
- 트래픽 분산 처리가 가능하다.
### Master
- 클러스터를 제어하고 관리하는 노드이다.
- 클러스터 상태를 유지하고 작업을 스케줄링하는 등의 역할을 한다.
### Componentes
> 시스템에 설치될 Kubernetes 의 구성요소

1. API 서버 
- Kubernetes 의 프론트엔드 역할을 하며 사용자와 클러스터가 통신할 수 있는 API 를 제공한다. ( ex> kubectl )
2. etcd
 -  신뢰할 수 있는 key: value 저장소로, kubernetes 가 클러스터 관리에 사용되는 모든 데이터를 저장한다. 
3. Scheduler 
 - 새로운 Pod 를 생성하고 클러스터 내 적절한 노드에 할당한다.
 - CPU, 메모리 등의 리소스 제약 조건과 노드 상태를 고려한다.
4. Controller
 - 클러스터 상태를 원하는 상태로 유지하기 위해 지속적으로 확인하고 조치한다.
5. Container Runtime
 - 컨테이너를 실행하는데 사용되는 기본 SW 로 Docker 등이 있다.
6. Kubelet 
 - 각 Node 에서 실행되는 agent 이다.
 - API 서버로부터 명령을 받고 컨테이너 상태를 모니터링한다.

<br>

## Master Node 와 Worker Node
### 1. Master Node
> 주요 구성 요소 : kube-apiserver, etcd, controller, scheduler

- kube-apiserver 를 가지며, 이를 통해 Worker Node 와 통신한다. 
- etcd 에 클러스터 메타데이터를 저장한다.
- controller 와 scehduler manager 가 있다. 

### 2. Worker Node
> 주요 구성 요소 : kubelet, container runtime

- kubelet agent 를 가지며, 이를 통해 Master Node 와 통신한다. 
- Node 의 건강 상태를 제공하고 Master 가 요청한 작업을 수행한다. 
- container 를 실행하기 위한 container runtime 이 설치되어야 한다. 

<br>

## Kubectl
- Kubernetes 클러스터 상의 응용 프로그램을 배포 및 관리하는 데 사용한다.
#### 1. run
- 클러스터 내 애플리케이션을 배포한다.
```shell
kubectl run hello-minikube
```
#### 2. cluster-info
- 클러스터 정보를 조회한다.
```shell
kubectl cluster-info
```
#### 3. get
- 클러스터 내 모든 Node 들을 조회한다.
```shell
kubectl get nodes
```

<br>

## 참고
[Kubernetes Certified Application Developer (CKAD) with Tests](https://www.udemy.com/share/1013BQ3@FHcQPh5fdtPOTP1ZXYZVcotPtN9ZvIN1IS37fa49ax7L0Kti3Q1cVKrL8WjJxV0YjA==/)