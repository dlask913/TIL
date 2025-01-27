## (k8s) Service Accounts
> User Account, Service Account

<br>

 Kubernetes에서는 인증을 위해 User Account와 Service Account를 분리하여 관리한다.

## User Account
- 사람이 사용하는 계정이다.
- 클러스터 관리자(admin), 개발자(developer) 등이 클러스터에 접근하기 위해 사용된다.
- RBAC(Role-based Access Control) 을 통해 권한을 부여할 수 있다. 

<br>

## Service Account
- Application 이 Kubernetes 리소스와 상호작용할 때 사용하는 계정이다. ( ex> jenkins, prometheus 등 )
- Pod 가 클러스터 내부에서 API 서버에 접근하기 위해 사용된다. 

### 특징
- 서비스 계정을 생성하면 자동으로 토큰이 생성되고, 외부 응용프로그램이 Kubernetes API 에 접근하기 위한 인증 방법으로 사용된다. 
- 서비스 계정을 생성하면 서비스 계정을 위한 토큰이 생성되고 Secret Object 를 생성하여 그 안에 토큰을 저장한 후 서비스 계정에 연결된다. 
- Kubernetes 의 모든 namespace 에는 기본적으로 default 서비스 계정이 생성되어 있다.  
- Pod 생성 시 default 서비스 계정의 token 이 자동으로 마운트된다. 
- default 서비스 계정은 몹시 제한적으로, 오직 basic Kubernetes API query 실행만 가능하다. 
- 외부 응용 프로그램이 Kubernetes 내부의 리소스에 접근할 때 서비스 계정 Token 을 인증 용도로 사용한다.
- default 서비스 계정의 토큰 정보는 아래 경로에 저장된다.
```shell
/var/run/secrets/kubernetes.io/serviceaccount
# 내부 파일 : 1. ca.crt     2. namespace    3. token ( actual token )
```
- Pod running 중에 다른 서비스 계정으로 변경할 수 없다. → Pod 삭제 이후 재생성하여 설정
```yaml
spec:
  containers: ..
  serviceAccountName: <serviceaccount-name> # 다른 서비스 계정 설정
```
- Deployment 는 서비스 계정을 변경할 수 있고, 이후 삭제되거나 재생성되는 Pod 들은 변경된 서비스 계정으로 관리가 가능하다. ( 기존 Pod 들은 영향 X)
- default 서비스 계정을 사용하기 싫다면 `automountServiceAccountToken` 옵션을 추가한다.
```yaml
spec:
  containers: ..
  automountServiceAccountToken: false # Default Service Account Token Mount 방지
```

### kubectl
- 서비스 계정 생성
```shell
$ kubectl create serviceaccount <serviceaccount-name>
```
- 서비스 계정 조회
```shell
$ kubectl get serviceaccount
```
- 서비스 계정 상세 조회 ( 토큰 정보 확인 가능 )
```shell
$ kubectl describe serviceaccount <serviceaccount-name>
```
- 서비스 계정 토큰 정보 상세 조회 : API 호출 시 인증 헤더로 사용
```shell
$ kubectl describe secret <secret-name> 
```

<br>

## v1.22/1.24 Update for Service Accounts
>  기존에는 Token 에 대한 접근과 만료일이 없었으며 Token 은 서비스 계정이 있는 한 계속 유효하고 계정별로 Secret Object 가 분리되어 있었다. 

### v1.22
- 토큰을 생성하는 `TokenRequestAPI` 가 도입되었으며 토큰에 대한 접근 대상, 시간, 객체를 제한하는 기능이 추가되었다. ( Recommended )
- Pod 생성 시 더 이상 default 서비스 계정의 토큰이 자동 마운트되지 않고, `TokenRequestAPI` 가 생성한 토큰이 정해진 볼륨으로 마운트된다. 

### v1.24
- 서비스 계정 생성 시 더 이상 Secret Object 와 토큰을 만들지 않고 `kubectl` 을 통해 서비스 계정에 대한 토큰을 생성해야 한다. 
```shell
$ kubectl create token <serviceaccount-name>
```
- 생성한 토큰에는 만료일이 있음 변경이 가능하다. 
- 기존의 default 서비스 계정 토큰을 사용하기 위해서는 아래와 같이 정의해야 한다.
```yml
# secret-definition.yml
apiVersion: v1
kind: Secret
type: kubernetes.io/service-account-token
metadata:
  name: mysecretname
  annotations:
    kubernetes.io/service-account.name: <serviceaccount-name>
```

<br>

## 참고
[Kubernetes Certified Application Developer (CKAD) with Tests](https://www.udemy.com/share/1013BQ3@FHcQPh5fdtPOTP1ZXYZVcotPtN9ZvIN1IS37fa49ax7L0Kti3Q1cVKrL8WjJxV0YjA==/)