## (k8s) ReplicaSets
>  ReplicationController, ReplicaSet

<br>

## ReplicationController
- Kubernetes에서 특정 Pod의 원하는 개수가 항상 유지되도록 보장하는 컨트롤러.
- Replication Controller 는 Kubernetes Cluster 단일 Pod 의 다중 인스턴스가 실행되도록 하여 **High Availability** (고가용성) 을 제공한다. 
- 특정 Pod가 다운되었을 때 자동으로 새로운 Pod를 생성하여 항상 원하는 개수의 Pod가 실행되도록 보장한다.
- 여러 개의 Pod 를 만들어 Load Balancing 기능을 제공한다. 
- 사용자 요청이 증가하여 첫 번째 Node 리소스가 부족해지면 클러스터 내 다른 Node 에 걸쳐 배포할 수 있다. → **Load Balancing & Scaling**
- 오래된 기능으로 Replica Set 이 대체하고 있다.
- rc-definition.yml 
```yml
apiVersion: v1
kind: ReplicationController
medataa:
  name: myapp-rc
  labels:
    app: myapp
    type: front-end
spec:
  template: # 복제본을 만들기 위해 사용할 Pod template
    metadata:
      name: myapp-pod
      labels:
        app: myapp
        type: front-end
    spec:
      containers:
      - name: nginx-container
        image: nginx
  replicas: 3 # 필요한 복제본 수 정의
```
- kubectl
```shell
$ kubectl create -f rc-definition.yml # 생성
$ kubectl get replicationController # 조회
```

<br>

## ReplicaSet
- `selector` 를 꼭 정의해야 한다. ( ReplicationController 는 Optional )
- ReplicaSet 에서는 이전에 생성된 Pod 를 고려해야하기 때문에 `selector` 정의가 필요하다. 
- `selector` 에 정의된 `machLabels` 속성을 사용하여 특정 label을 가진 Pod만 관리할 수 있다. 
- replicaset-definition.yml
```yaml
apiVersion: apps/v1
kind: ReplicaSet
metadata:
  name: myapp-replicaset
  labels:
    app: myapp
    type: front-end
spec:
  template:
    metadata:
      name: myapp-pod
      labels:
        app: myapp
        type: front-end
    spec:
      containers:
      - name: nginx-container
        image: nginx
  replicas: 3
  selector:
    matchLabels:
      type: front-end
```
- kubectl
```shell
$ kubectl create -f replicaset-definition.yml # 생성
$ kubectl get replicaset # 조회
```

### Labels and Selectors
- 이미 생성된 기존의 Pod 를 모니터링하기 위해 사용할 수 있다. 
- Active Pod의 개수를 보장하고 필요 시 새로운 Pod를 배포한다. 

### ReplicaSet Scale
> 복제본 수를 3→6으로 늘리고 싶다고 가정한다.

1. yaml 파일의 Replicas 수 변경
- yaml 파일의 `replicas` 수를 변경하여 업데이트 명령을 한다.
```shell
$ kubectl replace -f [yaml 파일명].yml
```

2. Scale 명령
- 명령어로 직접 `replicas` 수를 조정한다.
- yaml 파일에는 반영되지 않는다. 
```shell
$ kubectl scale --replicas=6 -f [yaml 파일명].yml 
$ kubectl scale --replicas=6 replicaset [replicaset name]
```

<br>

## 참고
[Kubernetes Certified Application Developer (CKAD) with Tests](https://www.udemy.com/share/1013BQ3@FHcQPh5fdtPOTP1ZXYZVcotPtN9ZvIN1IS37fa49ax7L0Kti3Q1cVKrL8WjJxV0YjA==/)