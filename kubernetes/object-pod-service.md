## (k8s) 기본 오브젝트 - Pod, Service
> Pod (Container, Label, Node Schedule) 와 Service (ClusterIP, NodePort, Load Balancer)

<br>

## Obejct - Pod ( [공식문서](https://kubernetes.io/docs/concepts/workloads/pods/) )

### Container
- 한 파드에 여러 컨네이너들을 담을 수 있다. 
- 컨테이너는 하나의 독립적인 서비스를 구동할 수 있으며, 각각의 컨테이너들은 여러 포트를 가질 수 있지만 중복은 불가하다. 
- Pod 가 생성될 때 고유 IP 가 할당되며 클러스터 내에서만 접근 가능하다. ( 외부에서는 접근 X )
- Pod 에 문제가 생기면 Pod 는 새로운 IP 로 재생성된다. 
- Pod 에 컨테이너들을 생성하기 위한 yaml 파일 예시 
```yml
apiVersion: v1
kind: Pod
metadata:
  name: pod-1
spec:
  containers:
  - name: container1
    image: [사용할 이미지명]
    ports:
    - containerPort: 8000
  - name: container2
    image: [사용할 이미지명]
    ports:
    - containerPort: 8080
```

### Label
> 모든 오브젝트에 사용할 수 있는데 Pod 에서 가장 많이 사용한다. 

- 목적에 따라 오브젝트를 분류할 수 있으며, 분류된 오브젝트들을 따로 골라 연결하기 위해 사용한다. ( 사용 목적에 따라 label 등록 )
- key: value 가 한 쌍이다. 
- 한 Pod 에 여러 개의 Label 이 있을 수 있다. 
- 예를 들어 `type: web` 이라는 Label 을 가진 Pod 들을 분류해 서비스에 연결해서 서비스 정보를 웹 개발자에게 알려줄 수 있다. 
- Label 이 추가된 Pod 생성 yaml 파일 예시 
```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod-1
  labels:
    type: web # key1: value1
    io: dev # key2: value2
spec:
..  
```
- 특정 Label 을 고르는 Service 생성 yaml 파일 예시 
```yaml
apiVersion: v1
kind: Service
metadata:
  name: svc-1
  selector:
    type: web # key 가 web 인 Label 을 가진 Pod 연결
  ports:
..
```

### Node Schedule
> 특정 pod 는 한 node 에 올라가야 하는데 직접 선택을 하는 방법과 Kubernetes 가 자동으로 지정하는 방법 두 가지가 있다. 

1. 직접 선택
- Node 에 Label 을 달고 Pod 생성 시 NodeSelector 를 지정하여 직접 Node 를 선택한다. 
- Pod 생성 yaml 파일 예시 
```yaml 
apiVersion: v1
kind: Pod
metadata:
  name: pod-1
spec:
  nodeSelector:
    hostname: node1 # hostname key 로 node1 이라는 value 를 가진 node 선택
.. 
```

2. Kubernetes 의 스케줄러가 지정
- Node 의 사용 가능한 자원량 ( 메모리, CPU 등 ) 으로 스케줄러가 판단해서 지정
- Pod 생성시 요구될 리소스 사용량을 명시할 수 있으며 이를 설정하지 않으면 스케줄러가 무한정 노드에 있는 자원을 사용하려고 할거기 때문에 자원이 없어져 종료될 수 있다. 
- Pod 생성 yaml 파일 예시
```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod-1
spec:
  containers:
  - name: container1
    image: nginx:latest
    resources:
      requests: # 초과 시 request 로 낮추고 종료되지 않음.
        memory: 2Gi
        cpu: "500m"
      limits: # 초과 시 Pod 종료.
        memory: 3Gi
        cpu: "1"
```

<br>

## Object - Service ( [공식문서](https://kubernetes.io/ko/docs/concepts/services-networking/service/) )

### ClusterIP
- 각 Service 는 ClusterIP 를 가지며, 쿠버네티스 클러스터 내에서만 접근 가능하다. ( 외부 접근 불가 )
- Pod 에 연결시키면 서비스의 IP 를 통해 Pod 에 접근할 수 있다.
- 서비스의 IP는 사용자가 삭제하지 않는 한 유지되지만, 서비스를 삭제하면 IP가 재생성될 수 있다.
- 용도 : 인가된 사용자(운영자) 가 내부 대시보드를 통해 Pod 의 서비스 상태를 디버깅하는 등의 관리
- Service 생성 yaml 파일 예시 
```yaml 
apiVersion: v1
kind: Service
metadata:
  name: svc-1
spec:
  selector: # 아래 key: value 를 가진 Pod 연결
    app: pod
  ports:
    - port: 9000 # Service 가 노출하는 port
      targetPort: 8080 # 연결된 Pod Port 
  type: ClusterIP # 설정하지 않을 경우 ClusterIP 가 기본 값
```

### NodePort
- 쿠버네티스 클러스터의 **모든 노드에 동일한 포트**를 열어, 외부에서 클러스터 노드의 IP 와 해당 포트 (NodePort) 를 사용해 서비스에 연결할 수 있다. 
- 서비스는 어떤 노드에서 온 트래픽인지 상관없이 자신과 연결된 Pod 로 트래픽을 전달한다. 
- `externalTrafficPolicy` : Cluster 가 기본 값이며 `Local` 로 설정할 경우, 서비스가 트래픽을 전달한 해당 노드 위에 올려져 있는 Pod 한테만 트래픽을 전달한다. 
- 용도 : 내부망 연결, 외부에 보여줄 때 임시 연결용
- Service 생성 yaml 파일 예시 
```yaml
apiVersion: v1
kind: Service
metadata:
  name: svc-1
spec:
  selector:
    app: pod # 연결된 Pod의 label
  ports:
    - port: 9000
      targetPort: 8080
      nodePort: 31000 # 외부에서 접근 가능한 포트 (지정하지 않으면 30000~32767 중 자동 할당)
  type: NodePort
# externalTrafficPolicy: Local
```

### Load Balancer
- 외부에서 들어오는 트래픽을 여러 Pod에 분산시키는 역할을 하며, 클라우드 환경에서는 자동으로 로드 밸런서와 외부 IP를 제공한다.
- 온프레미스에서는 별도의 IP 설정 등 플러그인 설치가 필요하다. 
- 용도 : 외부 IP 를 통해 서비스 노출
- Service 생성 yaml 파일 예시
```yaml
apiVersion: v1
kind: Service
metadata:
  name: svc-1
spec:
  selector:
    app: pod # 연결된 Pod의 label
  ports:
    - port: 9000
      targetPort: 8080
  type: LoadBalancer
```

<br>

## 참고 이미지
![image](https://github.com/user-attachments/assets/08da9faa-d7cf-483c-868b-088736da67db)


<br>

## 참고
[인프런 - 대세는 쿠버네티스 [초급~중급]](https://inf.run/uATqg) <br>

https://jaffarshaik.medium.com/kubernetes-architecture-and-components-bf637dbd0526 