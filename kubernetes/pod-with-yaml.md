## (k8s) Pods ( with YAML )
>  Pods, Pods with YAML

<br>

## Pods
![image](https://github.com/user-attachments/assets/4a4ae473-3238-4944-a27b-3e5e54d07f02)

- Kubernetes 에서 생성할 수 있는 가장 작은 배포 단위로, 단일 컨테이너 또는 여러 컨테이너를 포함할 수 있다. 
- 애플리케이션을 컨테이너 형태로 클러스터 내 Worker Node 에 배포한다. 
- 컨테이너를 직접 Node 에 배포하지 않고 Pod 를 사용하여 컨테이너를 캡슐화하여 관리한다.
- User 가 늘어나서 트래픽이 증가한다면 로드 분산을 위해 Pod 인스턴스를 추가 생성한다. 
- 만약 Node 의 자원이 부족하다면 클러스터에  Node 를 추가하여 Pod 를 배포한다. → 클러스터의 물리적 용량 확장

### Multi-Container Pods
- Pod 하나에 Container 가 여러 개 일 수 있지만 규모를 늘리기 위해서는 Pod 를 추가로 만들어야 한다. 
- 파일 업로드, 로그 처리 등의 Helper Containers 가 애플리케이션 컨테이너와 함께 있어야 하는 경우는 같은 Pod 에 두는 것이 용이하다. 

### kubectl
- Pod 생성
```shell
$ kubectl run nginx --image nginx # Docker Hub 에서 이미지 다운로드
```

- Pod 리스트 조회
```shell
$ kubectl get pods
```

<br>

## Pods with Yaml

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: myapp-pod
  labels:
    app: myapp
spec:
  containers:
    - name: nginx-container
      image: nginx
```

1. apiVersion 
- 생성하려는 Kubernetes 리소스에 사용할 API 버전으로, 생성하는 종류에 따라 올바른 API 버전을 사용해야 한다. 
- Pod - v1, Service - v1, ReplicaSet - apps/v1, Deployment - apps/v1

2. kind
- 생성하는 리소스 유형을 말한다.
- Pod, Service, Deployment 등

3. metadata
- 리소스의 이름(name) 과 태그(label) 을 정의한다.
- label 을 기반으로 Pod 필터링이 가능하다. 

4. spec
- 리소스의 동작 관련된 추가 정보를 제공한다. 
- containers, 볼륨 설정 등


### kubectl
- yaml 파일로 Pod 생성
```shell
$ kubectl create -f [파일명].yml
```
- Pod 상세 조회
```shell
$ kubectl describe Pod myapp-pod
```

<br>

## 참고
[Kubernetes Certified Application Developer (CKAD) with Tests](https://www.udemy.com/share/1013BQ3@FHcQPh5fdtPOTP1ZXYZVcotPtN9ZvIN1IS37fa49ax7L0Kti3Q1cVKrL8WjJxV0YjA==/)