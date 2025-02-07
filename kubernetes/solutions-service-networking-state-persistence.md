## (k8s) Solutions
> Service and Networking, State Persistence

<br>

## Service
- service 생성
```yaml
apiVersion: v1
kind: Service
metadata:
  name: my-service
spec:
  type: NodePort
  ports:
    - targetPort: 8080
      port: 8080
      nodePort: 30080
  selector:
    name: simple-webapp
```

<br>

## Ingress Networking
- ingress resource 조회
```shell
$ kubectl get ingress -A
```
- ingress 생성
```shell
$ kubectl create ingress <ingress-name> --rule="<path>=<service>:<port>"
```
- rewrite 옵션 추가하기
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
..
```
- roles and rolebindings 조회하기
```shell
$ kubectl get roles
$ kubectl get rolebindings
```
- ingress controller 에 NodePort Service 생성하기
```shell
$ kubectl expose deploy <ingress-controller-name> --name=<service-name> --port=<port-number> --target-port=<targetport-number> --type=NodePort 
```
- service 수정하기
```shell
$ k edit svc <service-name>
```

<br>

## Network Policies
- network policy 조회
```shell
k get netpol
```
- egress NetworkPolicy 생성
```yaml
..
spec:
  podSelector:
    matchLabels:
      name: internal
  policyType:
    - Egress
  egress:
    - to:
        - podSelector:
            matchLabels:
              name: payroll
      port:
        - protocol: TCP
          port: 8080
    - to: ..
```

<br>

## State Persistence
- pod 에서 명령어 실행하기
```shell
$ k exec <pod-name> -- <command> <args>
```
- host volume mounting 하기
```yaml
# kind: Pod 
..
spec:
  containers:
    - name: myapp
      image: myapp
      volumeMounts:
      - mountPath: /log
        name: log-volume
  volumes:
  - name: log-volume
    hostPath:
      path: /var/log
```
- pod 에 pvc 연결하기
```yaml
# kind: Pod
..
spec:
  containers:
    - name: myapp
      image: myapp
      volumeMounts:
      - mountPath: /log
        name: log-volume
  volumes:
  - name: log-volume
    persistentVolumeClaim:
      claimName: claim-log-1
```
- pvc 는 삭제하려면 사용중인 pod 를 먼저 제거해야 한다.


<br>

## 참고
[Kubernetes Certified Application Developer (CKAD) with Tests](https://www.udemy.com/share/1013BQ3@FHcQPh5fdtPOTP1ZXYZVcotPtN9ZvIN1IS37fa49ax7L0Kti3Q1cVKrL8WjJxV0YjA==/)