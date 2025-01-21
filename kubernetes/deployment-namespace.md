## (k8s) Deployment 와 Namespace
>  Deployment 와 Namespace

<br>

## Deployment
![image](https://github.com/user-attachments/assets/8be5a4cc-e37e-46ef-ae2a-57a7d52c50f0)

- 애플리케이션 build 의 새로운 버전이 Docker Registry 이용 가능할 때 Docker 인스턴스를 매끄럽게 업그레이드할 수 있다.
- 한 번에 모든 인스턴스를 업그레이드 하는 것이 아니라 하나씩 업그레이드 한다.  ( Rolling )
- 각 컨테이너는 캡슐 형태로 포장되고 다수의 Pod 가 ReplicationController 나 ReplicaSet 으로 배포되고 그 이후 Deployment 는 인스턴스를 매끄럽게 업그레이드 한다. 
- deployment-definition.yml
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myapp-deployment
  labels: 
    app: myapp
    type: front-end
spec:
  replicas: 3
  selector:
    matchLabels:
      app: myapp
      type: front-end
  template:
    metadata:
      labels:
        app: myapp
        type: front-end
    spec:
      containers:
      - name: nginx-container
        image: nginx
```
- kubectl
```shell
$ kubectl create -f deployment-definition.yml # 생성
$ kubectl get deployments # deployment 조회
```

<br>

## Namespaces
- Kubernetes 자체적으로 3개의 Namespaces 가 생성되며 직접 생성할 수도 있다. 
- 규모가 작다면 고려할 필요가 없다.
### 기본 제공되는 Namespaces
#### 1. default namespace
- 클러스터가 처음 설정될 때 Kubernetes 가 자동으로 생성한다. 
- 네트워킹 솔루션, DNS 서비스 등 내부 목적을 위해 Pod 와 Service Set 를 포함한다. 

#### 2. kube-system namespace
- Kubernetes 의 내부 구성 요소( 컨트롤러, 스케줄러 등 )가 배포된다.
- 실수로 수정하거나 삭제하지 않도록 사용자로부터 분리되어 있다. 

#### 3. kube-public namespace
- 모든 사용자가 사용할 수 있어야하는 공용 리소스를 저장한다.

<br>

### 특징
- Isolation : 기업이나 production 목적으로 Namespace 사용을 고려할 수 있고 환경별 (개발, 운영, 테스트) 분리를 위해 격리한다.
- Policies : Namespace 별로 고유한 정책을 가질 수 있다. ( 누가 뭘 할 수 있는 지 등을 정의 )
- Resource Limits : Namespace 별로 리소스 할당량을 정의하여 각 Namespace 는 일정량을 보장받고 허용된 한도 이상을 사용하지 않게 한다. 
-  DNS : resource 끼리 서로 이름을 정의하여 쉽게 연결할 수 있다. 

<br>

### DNS 예시 
> Default Namespace 내 web-pod, db-service, web-deployment 가 있다고 가정한다. 

- web-pod 에서 db 서비스 연결하기
```shell
mysql.connect("db-service")
```
- default namespace 의 web-pod 에서 dev namespace 의 db 서비스로 연결하기
```shell
# 서비스명.네임스페이스명.서비스를위한하위도메인.도메인명
mysql.connect("db-service.dev.svc.cluster.local")
```

<br>

### Namespace 생성 방법

1. yml 파일로 생성하기
```yml 
apiVersion: v1
kind: Namespace
metadata:
  name: dev
```
- kubctl 로 생성
```shell
$ kubectl create -f namespace-dev.yml
```

2. command 로 dev namespace 바로 생성하기
```shell
$ kubectl create namespace dev 
```

<br>

### Switch
- Pod 조회 시 Namespace 옵션 지정 없이 다른 Namespace 를 지속하여 조회하고 싶을 때 Namespace Switch 가 가능하다. 
- kubectl config 명령으로 current context 지정
```shell
$ kubectl config set-context $(kubectl config current-context --namespace=dev)
```

<br>

### Resource Quota
- Namespace 내 리소스 사용 제한을 설정할 수 있다.
- compute-quota.yml
```yml
apiVersion: v1
kind: ResourceQuota
metadata:
  name: compute-quota
  namespace: dev
spec:
  hard:
    pods: "10"
    requests.cpu: "4"
    requests.memory: 5Gi
    limits.cpu: "10"
    limits.memory: 10Gi
```
- kubectl 로 생성
```shell
$ kubectl create -f compute-quota.yml
```
<br>

### kubectl
- 다른 Namespace 조회하기
> namespace 옵션이 없을 땐, default namespace 에서만 조회한다. 

```shell
$ kubectl get pods --namespace=kube-system
```

- 다른 Namespace 에 Pod 생성하기
```shell
$ kubectl create -f pod-definition.yml --namespace=dev 
```
- 모든 Namespace 의 Pod 조회하기
```shell 
$ kubectl get pods --all-namespaces
```

<br>

## 참고
[Kubernetes Certified Application Developer (CKAD) with Tests](https://www.udemy.com/share/1013BQ3@FHcQPh5fdtPOTP1ZXYZVcotPtN9ZvIN1IS37fa49ax7L0Kti3Q1cVKrL8WjJxV0YjA==/)