## (ci/cd) 무중단 배포
> 무중단 배포, Nginx 를 활용한 무중단 배포 실습, artillery 를 활용한 무중단 배포 테스트

<br>

## 무중단 배포 
- 무중단 배포를 위해 여러 개의 서버를 이용할 경우 클라이언트가 모든 서버의 IP 주소를 알아야하고 이를 해결하기 위해 도메인 주소를 사용하더라도 배포 중 연결 끊김 현상이 발생할 수 있다.
- 끊김 현상을 해결하기 위해 여러 서버 앞에 Nginx 를 배치하여 클라이언트의 HTTP 요청을 받아 다양한 역할을 수행한다. 

### Nginx 주요 역할
1. 리버스 프록시 : 클라이언트의 요청을 받아 적절한 서버로 전달하여 서버를 숨기고 보호한다.
2. 웹 서버 : 정적 콘텐츠(HTML, JavaScript, 이미지 등)을 직접 제공한다.
3. 로드 밸런서 : 요청을 여러 서버로 분산시켜 부하를 조절한다. 
4. 게이트웨이 : API 요청 라우팅, 인증/인가, HTTPS 설정, 로깅 등의 작업을 수행한다. 

### 용어 정리
- 프록시 : 클라이언트가 자신을 숨기고 중계 서버를 통해 서버와 통신한다. (대리인을 의미)
- 웹 애플리케이션 서버 : 동적 콘텐츠 제공

<br>

## Nginx 를 활용한 무중단 배포 환경 만들기

### ① Vultr 인스턴스 생성
1. Nginx 인스턴스 생성
- OS : Rocky Linux 9
- Plan : AMD High Performance 50 GB NVMe
- Additional Features : Auto Backups Disbled, IPv6 만 선택
- Hostname : nginx-instance

2. Application 인스턴스 추가 생성 (2개)
- OS : Rocky Linux 9
- Plan : AMD High Performance 25 GB NVMe  ( 메모리 1GB )
- Additional Features : Auto Backups Disbled, IPv6 만 선택
- Hostname : application-instance-2 & application-instance-3

<br>

### ② Nginx 설치 ( nginx-instance )
1. 시스템 패키지 업데이트 ( 테스트 환경에서 생략 가능 )
```bash
sudo dnf update
```
2. EPEL (Extra Packages for Enterprice Linux) 리포지토리 설치 ( 테스트 환경에서 생략 가능 )
```shell
sudo dnf install epel-release
```
3. Nginx 설치 
```shell
sudo dnf install nginx
```
4. Nginx 실행
```shell
sudo systemctl start nginx
sudo systemctl enable nginx # 서버 부팅 시 Nginx 시작
```
5. 80 포트 접속하여 동작 확인
![image](https://github.com/user-attachments/assets/922860f4-aa40-4a06-9ea2-4d858579d739)

<br>

### ③ 추가 애플리케이션 인스턴스 설정 ( application-instance2&3 )
> 문제가 생겼을 경우, 이전 게시글을 참고한다.  ( [참고](https://github.com/dlask913/TIL/blob/main/devops/jenkins-pipeline.md) )

1. 공개 키 설정 
: application-instance-1 에 등록되어있는 jenkins 공개키를 동일하게 복사하여 application-2&3 에 저장한다. 

```shell
vi ~/.ssh/authorized_keys 
```

2. 실행할 애플리케이션과 같은 JDK 17 로 설정
```shell
sudo dnf install java-17-openjdk
sudo alternatives --config java 
```

- 방화벽 오픈 ( ACG 로 관리하는 경우 무시 가능, NCP 의 경우 ACG 설정으로 대체 )
```shell
sudo firewall-cmd --zone=public --add-port=8080/tcp --permanent # 오픈
sudo firewall-cmd --reload # 재시작
```

<br>

### ④ 젠킨스 배포 스크립트 추가
- 다른 애플리케이션에 대한 deply 과정 추가 ( [참고](https://github.com/lleellee0/application-deploy-advanced/blob/main/2_1_0_pipeline_script) )
```groovy
pipeline {
    agent any

    tools {
        maven "M3"
    }
    stages {
        stage('Deploy to Server 1') {
            steps {
                script { // application-instance-1
                    deployToServer("110.165.18.xxx")
                }
            }
        }
        stage('Deploy to Server 2') {
            steps {
                script { // application-instance-2
                    deployToServer("211.188.58.xxx")
                }
            }
        }
        stage('Deploy to Server 3') {
            steps {
                script { // application-instance-3
                    deployToServer("223.130.163.xxx")
                }
            }
        }
    }
    post {
        success {
            echo 'Deployment completed successfully.'
        }
        failure {
            echo 'Deployment encountered an error.'
        }
    }
}

def deployToServer(serverIp) {
    def jarFile = 'target/[jar명].jar'
    def deployPath = '/root'
    def runAppCommand = "nohup java -jar $deployPath/[jar명].jar > $deployPath/app.log 2>&1 &"
    def checkLogCommand = "grep -q 'Started [애플리케이션명] in' $deployPath/app.log"

    sshagent(['deploy_ssh_key']) {
        sh script: "ssh -o root@$serverIp 'pgrep -f [jar명].jar && pkill -f [jar명].jar || echo \"No process found\"'", returnStatus: true
    }

    sh "scp -o StrictHostKeyChecking=no $jarFile root@$serverIp:$deployPath/"

    sshagent(['deploy_ssh_key']) {
        sh "ssh -o StrictHostKeyChecking=no root@$serverIp '$runAppCommand'"
        sleep 20

		// StrictHostKeyChecking=no 설정 추가
        int result = sh script: "ssh -o StrictHostKeyChecking=no root@$serverIp '$checkLogCommand'", returnStatus: true
        if (result == 0) {
            echo "Deployment to $serverIp was successful."
        } else {
            error "Deployment to $serverIp failed."
        }
    }
}
```

> ※ StrictHostKeyChecking=no 
> - 호스트 키 검사를 무시한다. ( 신뢰하지 않는 애플리케이션 연결 강제 허용 )
> - 옵션 제거 시, Host key verification failed. scp: Connection closed 발생 가능 

<br>

### ⑤ Nginx 요청 포워딩 설정
1. nginx.conf 파일 수정
```shell
vi /etc/nginx/nginx.conf
```
- niginx.conf
```nginx.conf
upstream backend_servers { # application-instance-1&2&3
	server 110.165.18.xxx:8080;
	server 211.188.58.xxx:8080;
	server 223.130.163.xxx:8080;
}

server { # 요청을 받을 수 있는 경로 추가
	..
	location / { # 모든 요청
		proxy_pass http://backend_servers; # 아래의 header 정보와 함께 요청을 전달할(포워딩) 경로 설정
		proxy_set_header Host $host;
		proxy_set_header X-Real-IP $remote_addr;
		proxy_set_header X-Forwarded-For $proxy add x forwarded for;
		proxy_set_header X-Forwarded-Proto $scheme;
	}
}
```
- 문법 체크
```shell
sudo nginx -t
```

2. nginx reload
```shell
sudo systemctl reload nginx
```

<br>

### ⑥ Nginx 접속
- 백엔드 애플리케이션의 API 엔드포인트를 Nginx IP 주소로 접속하여 동작이 잘 되는 지 확인한다. 
- nginx 예외가 발생한다면, nginx error 로그 확인해보기
```shell
tail -f /var/log/nginx/error.log
```

<br>

## artillery 툴을 활용한 무중단 배포 테스트하기
1. test-config.yaml 작성
```yaml
config:
  target: 'http://211.188.58.xxx'
  phases:
    - duration: 100        # 테스트를 100초 동안 실행
      arrivalRate: 10      # 초당 10개의 요청
scenarios:
  - flow:
      - get:
          url: "/index.html"
```
2. artillery 실행
```shell
artillery run --output report.json test-config.yaml
```
3. 실행 결과 확인
```shell
artillery report report.json --output report.html
```

<br>

## sleepInterval 주기
- 애플리케이션이 배포되기까지 20초 이상이 걸리는 경우, 빌드가 실패하는 것을 대비하여 로그 파일을 주기적으로 확인하도록 변경한다.
- sleepInterval 을 길지 않게 하여 배포가 이미 된 상태에서 기다리는 경우가 없도록 하면 좀 더 빠르게 빌드를 완료할 수 있다. 
- 아래 [스크립트](https://github.com/lleellee0/application-deploy-advanced/blob/main/2_2_pipeline_script) 는, 3초 간격으로 40번 확인하여 120초 동안 배포가 되지 않을 경우 빌드 실패로 간주한다.

```groovy
def deployToServer(serverIp) {
// ..
    def maxAttempts = 40
    def sleepInterval = 3 // 3 seconds
    // ..
    sshagent(['deploy_ssh_key']) {
        sh "ssh -o StrictHostKeyChecking=no root@$serverIp '$runAppCommand'"

        // 로그 파일을 주기적으로 확인하여 애플리케이션 실행 확인
        def attempts = 0
        def deploymentSuccess = false
        while (attempts < maxAttempts) {
            int result = sh script: "ssh -o StrictHostKeyChecking=no root@$serverIp '$checkLogCommand'", returnStatus: true
            if (result == 0) {
                echo "Deployment to $serverIp was successful."
                deploymentSuccess = true
                break
            }
            attempts++
            sleep sleepInterval
        }

        if (!deploymentSuccess) {
            error "Deployment to $serverIp failed."
        }
    }
}
```

<br>

## 참고
[인프런 - 애플리케이션 배포 자동화와 CI/CD](https://inf.run/WqKp9) 