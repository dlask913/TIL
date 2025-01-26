## (k8s) Security Contexts
> Docker Security, Security Contexts

<br>

## Docker Security
- Host 는 Docker 데몬, Eset 서버 등 다수의 운영체제 프로세스를 실행한다. 
- 도커가 Container를 실행하면 Container 와 Host 는 동일한 커널을 공유하고 리소스는 Namespace 와 cgroups를 통해 논리적으로 격리된다.
- Host 와 Container 는 각자의 고유한 Namespace 를 가지며 이를 통해 격리되고 일부 설정을 통해 공유할 수는 있다.
- Container 에서 실행되는 모든 프로세스는 호스트 커널에서 실행되지만 독립적인 Container Namespace 에서 실행되기 때문에 특정 권한 없이는 다른 Container 및 Host 에서는 볼 수 없다. → process isolation 

### Users 
- Docker Host 는 root 사용자와 다수의 root 가 아닌 사용자들로 구성된다.
- 기본적으로 Host 에서 실행되는 컨테이너 프로세스는 root 권한을 가지며, 원치 않는 경우 `--user=1000` 옵션을 사용해 root 가 아닌 사용자로 실행할 수 있다.
- 이미지 생성 시 기본 사용자 변경이 가능하다.
```Dockerfile
FROM ubuntu
USER 1000 
```
- 컨테이너 내 root 사용자와 Host 내 root 사용자는 다른데, 컨테이너의 경우 접근 권한을 제한하는 Linux 보안 기능을 가지고 있다. 

### Linux Capabilities
- root 사용자는 시스템 내 모든 권한을 가지며 해당 권한 목록은 아래에서 확인할 수 있다.
```shell
/usr/include/linux/capability.h # 사용자의 기능을 제어할 수 있다.
```
- 기본적으로 Docker 컨테이너는 일부 권한이 제거된 상태로 실행되며, 호스트를 재부팅하거나 다른 컨테이너를 방해할 수 있는 기능은 제한된다.
- `--cap-add=<>` 이나 `--cap-drop=<>` 옵션을 통해 컨테이너 실행 시 권한을 추가하거나 제한할 수 있다. 

<br>

## Security Contexts
- Kubernetes 에서 Container 는 캡슐화 되어 있으며, Container 레벨 혹은 Pod 레벨에서 보안 설정이 가능하다. 
- Pod 수준에서 적용하게 되면 내부 Container 모두에 적용된다. 
- Pod 와 Container 에 모두 `securityContext` 설정이 있다면 Container 설정을 따라간다.
- `securityContext` 설정 예시
```yaml
apiVersion: v1
kind: Pod
metadata:
  name: 
spec:
  securityContext:
    runAsUser: 1000
  containers:
    - name:
      image:
      securityContext: # Pod securityContext 보다 우선순위가 높다.
        runAsUser: 1000
        capabilities:
          add: ["MAC_ADMIN"]
```

<br>

## 참고
[Kubernetes Certified Application Developer (CKAD) with Tests](https://www.udemy.com/share/1013BQ3@FHcQPh5fdtPOTP1ZXYZVcotPtN9ZvIN1IS37fa49ax7L0Kti3Q1cVKrL8WjJxV0YjA==/)