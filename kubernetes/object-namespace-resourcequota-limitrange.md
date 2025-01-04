## (k8s) 기본 오브젝트 - Namespace, ResourceQuota, LimitRange
>  Namespace, ResourceQuota, LimitRange

<br>

## Object - Namespace, ResourceQuota, LimitRange
- kubernetes cluster 에는 여러개의 Namespace 가 있을 수 있고 Namespace 에는 여러개의 Object 들이 있을 수 있다. 
- 클러스터 내 Pod 들은 필요한 자원을 공유해서 사용하는데 한 Pod 가 모두 사용해버릴 수 있기 때문에 Namespace 별로 ResourceQuota 로 최대 사용할 수 있는 한계 자원을 설정한다. 
- Namespace 내 Pod 의 자원 사용량이 크면 다른 Pod 들이 Namespace 에 들어올 수 없게 되므로 LimitRange 로 Pod 의 크기를 제한할 수 있다. → Pod 의 ResourceQuota 가 LimitRange 보다 작아야 Namespace 에 들어올 수 있다. 

### Namespace
- Namespace 내 같은 타입의 오브젝트들은 이름이 중복될 수 없다. 
- 타 Namespace 에 있는 자원들과 분리되어있기 때문에 다른 Namespace 에 있는 Pod 와 Service 는 연결할 수 없다. ( Pod 간 IP 로는 연결이 가능한데 Network Policy 로 제한 가능 )
- Namespace 를 지우면 그 안에 있는 자원들도 모두 함께 지워진다. 
- Namespace 생성 yaml 파일 예시 
```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: nm-1
```
- Namespace 연결 Pod / Service yaml 파일 예시
```yaml 
# Pod 생성
apiVersion: v1
kind: Pod
metadata:
  name: pod-1
  namesapce: nm-1
..

# Service 생성
apiVersion: v1
kind: Service
metadata:
  name: svc-1
  namespace: nm-1
..
```

### ResourceQuota
> 사용하고 있는 버전에서 어느 Object 들까지 제한할 수 있는 지 확인하고 사용 

- Namespace 에 제한하고 싶은 자원 스펙을 명시하여 자원 한계를 설정할 수 있다. 
- ResourceQuota 가 지정되어 있는 Namespace 에 Pod 를 만들 때, 이 Pod 는 제한하고 있는 스펙을 명시해야 한다. 
- Compute Resource : cpu, memory, storage 등
- Objects Count : Pod, Service, ConfigMap, PVC 등
- ResourceQuota 생성 yaml 파일 예시 
```yaml 
apiVersion: v1
kind: ResourceQuota
metadata:
  name: rq-1
  namespace: nm-1
spec:
  hard:
    requests.memory: 3Gi # Namespace 전체에서 요청할 수 있는 메모리 리소스 제한
    limits.memory: 6Gi # Namespace 전체에서 사용할 수 있는 메모리 리소스 제한
    requests.cpu: "2" # Namespace 전체에서 요청할 수 있는 CPU 리소스 제한
    limits.cpu: "4" # Namespace 전체에서 사용할 수 있는 CPU 리소스 제한
    pods: "10" # Namespace 내 생성할 수 있는 Pod 개수 제한
    configmaps: "20" # Namespace 내 생성할 수 있는 ConfigMap 개수 제한
    persistentvolumeclaims: "5" # Namespace 내 생성할 수 있는 PVC 개수 제한
```

### LimitRange
- 각 Pod 가 Namespace 에 들어올 수 있는 지 자원을 체크한다. 
- LimitRange 생성 yaml 파일 예시 
```yaml
apiVersion: v1
kind: LimitRange
metadata:
  name: lr-1
  namespace: nm-1
spec:
  limits:
  - type: Container
    min: 
      memory: 1Gi # 최소 메모리 제한
    max:
      memory: 4Gi # 최대 메모리 제한
    defaultRequest:
      memory: 1Gi # 설정되지 않은 Pod requests.memory 설정
    default:
      memory: 2Gi # 설정되지 않은 Pod requests.limits 설정
    maxLimitRequestRatio:
      memory: 3 # request 와 limits 값의 최대 비율 설정
```

<br>

## 참고
[인프런 - 대세는 쿠버네티스 [초급~중급]](https://inf.run/uATqg) <br>