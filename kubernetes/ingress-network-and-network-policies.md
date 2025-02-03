## (k8s) Ingress Network and Network Policies
>  Ingress Network (Ingress Controller, Ingress Resource), Network Policies

<br>

## Ingress Network
> DNS → Load Balancer → Ingress Controller → Service → Pod

![image](https://github.com/user-attachments/assets/6490cedd-c662-490c-a424-743c3f9c2e2c)


- 외부 유저가 정의된 규칙에 따라 클러스터 내부의 서비스로 라우팅할 수 있도록 돕는다. 
- Layer 7 (HTTP/HTTPS) 트래픽을 처리하여 로드 밸런싱, SSL, URL 기반 라우팅 등의 기능을 제공한다. 
- Kubernetes 자체에 내장되어있지 않아 별도로 설치해야 한다. ( ex> Nginx Ingress Controller, HAProxy 등 )
- Ingress 를 사용하지 않으면 URL 기반 라우팅을 위해 추가 리소스와 복잡한 설정이 필요한데 Ingress Controller 를 배포하고 Ingress Resource 를 정의함으로서 간단하게 라우팅할 수 있다. 

### Ingress Controller
- ingress controller image 로 deployment 를 생성한다. 
- ingress controller 에 대한 설정을 configMap 에 정의하여 연결한다.
- ingress controller 가 API 서버와 통신할 수 있도록 serviceaccount 를 생성한다.
- Deployment 생성 yaml 파일 예시
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx-ingress-controller
spec: 
  replicas: 1
  selector: 
    matchLabels:
      name: nginx-ingress
  template:
    metadata:
      labels:
        name: nginx-ingress
    spec:
      containers:
      - name: nginx-ingress-controller
        image: quay.io/kubernetes-ingress-controller/nginx-ingress-controller:0.21.0
        args:
          - /nginx-ingress-controller
          - --configmap=$(POD_NAMESPACE)/nginx-configuration # configMap
        env:
          - name: POD_NAMESPACE
            valueFrom:
              fieldRef:
                fieldPath: metadata.namespace
        ..
        ports: # Service Port 와 연결
          - name: http
            containerPort: 80
          - name: https
            containerPort: 443      
```

### Ingress Resource
- ingress controller 에 적용할 rule 을 설정한다. 
- 트래픽을 전달하거나 url 에 따라 다른 애플리케이션으로 라우팅 할 수 있다.
- url path 에 대한 rule 을 여러개 설정할 수 있고 도메인을 분리하고 싶다면 `host` 옵션을 활용한다.
- ingress resource 생성 yaml 파일 예시
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: ingress-wear-watch
spec:
  rules:
  - http: # URL path 로 rule 정의
      paths:
      - path: /wear
        pathType: Prefix # Kubernetes 1.18+ 
        backend: # 라우팅하고싶은 서비스 정의
          service:
            name: wear-service
            port: 
              number: 80
  - host: watch.my-online-store.com # host name 으로 rule 정의
    http:
      paths:
      pathType: Prefix
      - backend:
          service:
            name: watch-service
            port: 
              number: 80
    
```

### 실습
- command 로 ingress resource 생성하기
```shell
$ kubectl create ingress <ingress-image> --rule="host/path=service:port"
# kubectl create ingress ingress-test --rule="wear.my-online-store.com/wear=wear-service:80
```
- 특정 `host:port/path` 를 다른 host 로 `rewrite` 하기 ( /path → / )
> rewire-target 옵션을 사용하지 않으면, host/path 로 연결하게 된다.
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: ingress-wear-watch
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  rules:
  - http:
      paths:
      - path: /pay
        backend:
          service:
            name: pay-service
            port: 
              number: 8082
```

<br>


## Network Policies
![image](https://github.com/user-attachments/assets/9c1217aa-35a9-4a80-a9dc-cffe116d4c0a)

- 클러스터 내부 Pod 들은 같은 네트워크 대역을 갖기 때문에 서로의 IP 를 통해 자유롭게 접근할 수 있다. ( default, All Allow rules )
- 클러스터 내부 Object (Pod, Service, ReplicaSet) 간 접근을 제한하기 위해 정책을 설정할 수 있다.
- Web Server 와 API Server, DB Server 가 있을 때 API 서버만 DB 서버에 접근할 수 있도록 ingress 설정을 할 수 있고 API 서버에서 DB 서버로만 요청하기 위해 egress 설정을 할 수 있다. 
- DB 서버에서 ingress 가 허용된 트래픽은 egress 설정을 하지 않아도 응답이 가능하다. 
- pod, namespace, ip 레벨로 정책을 설정할 수 있다. 
- 외부 서비스에 대한 정책이 필요한 경우 IP 레벨로 정책을 설정한다.
- `-`  를 기준으로 트래픽이 or 연산으로 ingress / egress 된다
- `-` 내부 rule 은 and 연산으로 ingress / gress 된다.
- Allow Ingress Traffic From API Pod to DB Pod on port 3306 예시
```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: db-policy
spec:
  podSelector:
    matchLabels:
      role: db
  policyTypes:
  - Ingress
  ingress:
  - from:
    - podSelector: # pod 레벨
        matchLabels:
          name: api-pod
      namespaceSelector: # namespace 레벨
        matchLabels:
          name: prod
    - ipBlock: 
        cidr: 192.168.5.10/32
    ports:
    - protocol: TCP
      port: 3306

```

<br>

## 참고
[Kubernetes Certified Application Developer (CKAD) with Tests](https://www.udemy.com/share/1013BQ3@FHcQPh5fdtPOTP1ZXYZVcotPtN9ZvIN1IS37fa49ax7L0Kti3Q1cVKrL8WjJxV0YjA==/)