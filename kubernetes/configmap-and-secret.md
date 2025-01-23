## (k8s) ConfigMaps 과 Secrets
> Environment Variables, ConfigMaps, Secrets

<br>

## Environment Variables
- 환경 변수를 사용하기 위해 yaml 파일에 `env` 를 정의할 수 있다. 
- `configMap` 과 `Secret` 을 사용하여 환경 변수를 분리할 수 있다.
- `env` 사용 예시
```yaml
apiVersion: v1
kind: Pod
metadata:
  name: simple-webapp-color
spec:
 containers:
 - name: simple-webapp-color
   image: simple-webapp-color
   ports:
     - containerPort: 8080
   env:
     - name: APP_COLOR
       value: pink
```

<br>

## ConfigMaps
- Pod 가 많아질수록 환경변수를 관리하기가 어려워지기 때문에 중앙에서 `ConfigMaps` 를 이용해 관리할 수 있다. 
- key: value 형태로 정의되며 환경 변수 데이터를 전달하는 데 사용한다. 

### 1. Create configMap
- 파일로 정의하는 방법과 command 로 바로 생성할 수도 있다. 

#### command 로 생성하기
- `--from-literal` 을 추가하여 원하는 만큼 환경 변수를 정의할 수 있다. 
- 많아질수록 명령어가 복잡해진다는 단점이 있다. 

```shell
$ kubectl create configMap app-config --from-literal=APP_COLOR=blue \
--from-literal=APP_MOD=prod
```

#### yaml 파일로 생성하기
- 다양한 목적을 위해 여러 파일을 정의할 수 있다. ( mysql-config, redis-config, .. )
- ConfigMap 에 적절한 이름을 붙여 관리하기 용이하도록 한다. 
```yaml 
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config # 적절한 이름 붙이기
data: # 환경 변수 정의
  APP_COLOR: blue
  APP_MODE: prod
```

### 2. ConfigMap in Pod : Pod 구성하기
- `configMap` 을 개별 환경변수로 설정할 수도 있고 전체 데이터를 파일로 볼륨에 넣을 수도 있다. 
- Pod 생성 시 `configMap` 구성 예시 
```yaml
apiVersion: v1
kind: Pod
metadata:
  name: simple-webapp-color
  labels:
    name: simple-webapp-color
spec: 
  containers:
  - name: simple-webapp-color
    image: simple-webapp-color
    ports:
      - containerPort: 8080
    envFrom: # 리스트이므로 여러 ConfigMap 지정 가능
      - configMapRef:
        name: app-config # ConfigMap name 지정
```

<br>

## Secrets
- Web 에서 MySQL 접속 시 hostname, username 등의 정보를 분리하여 관리하는 것을 권장하는데 configMap 의 경우 일반 텍스트로 저장되어 암호를 저장하기에 적절하지 않다. → Secret 사용
- 데이터를 인코딩된 형식으로 지정해야 한다. 
```shell
> echo -n 'mysql' | base64 # 인코딩
> echo -n 'bxlizWw=' | base64 --decode # 디코딩
```

### 1. Create Secrets

#### command 로 생성하기
- `--from-literal` 을 추가하여 원하는만큼 환경 변수를 정의할 수 있다. 
- 많아질수록 명령어가 복잡해진다는 단점이 있다. 

```shell
$ kubectl create secret generic <secret-name> --from-literal=<key>=<value> --from-literal=<key>=<value>
```

#### yaml 파일로 생성하기
- secret-data.yaml
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: app-secret
data: # value 를 인코딩한 형식으로 정의
  DB_Host: bxldlfk=
  DB_User: cmdk2l=
  DB_Password: cGFzld2f
```

### 2. Secret in Pod : Pod 구성하기
- 개별 환경 변수로 설정할 수도 있고 전체 데이터를 파일로 볼륨에 넣을 수도 있다. 
- Pod 생성 시 `secret` 구성 예시 
```yaml
apiVersion: v1
kind: Pod
metadata:
  name: simple-webapp-color
  labels:
    name: simple-webapp-color
spec: 
  containers:
  - name: simple-webapp-color
    image: simple-webapp-color
    ports:
      - containerPort: 8080
    envFrom: 
      - secretRef:
          name: app-secret # Secret name 지정
```

### Secret 사용 시 주의사항
- `Secret`은 인코딩되었을 뿐 암호화되지 않았으므로 Git이나 SVN에 업로드되지 않도록 주의해야 한다.
- 기본적으로 `etcd`에 암호화되지 않은 상태로 저장되므로 암호화 설정을 고려해야 한다.
- 같은 `Namespace` 내에서 Pods/Deployments를 생성할 수 있는 사용자는 Secret에 접근할 수 있으므로 역할 기반 접근 제어(RBAC)를 설정해야 한다.
- 클라우드 제공업체의 Secret 관리 서비스를 고려할 수 있다. (예: AWS Secrets Manager, Azure Key Vault, HashiCorp Vault 등)

<br>

## 참고
[Kubernetes Certified Application Developer (CKAD) with Tests](https://www.udemy.com/share/1013BQ3@FHcQPh5fdtPOTP1ZXYZVcotPtN9ZvIN1IS37fa49ax7L0Kti3Q1cVKrL8WjJxV0YjA==/)