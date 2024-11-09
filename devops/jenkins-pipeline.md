## (ci/cd) Jenkins Pipeline 구축
> Jenkins 서버 세팅하기 (Vultr), Jenkins Pipeline 생성 및 설정

<br>

## Jenkins 서버 세팅하기 
### Vultr Jenkins 인스턴스 생성
> 최소 메모리 2GB 이상

- Type : Cloud Compute - Shared CPU 
- Region : Seoul
- OS : Rocky Linux 9
- Plan : AMD High Performance 50 GB NVMe
- Additional Features : Auto Backups Disbled, IPv6 만 선택
- Hostname : jenkins-instance

### [Jenkins 설치](https://github.com/lleellee0/application-deploy-advanced) 
0. Putty 접속 : 생성된 인스턴스의 ip, pw 활용
1. 리눅스 업데이트
```shell
sudo dnf update -y
```
2. 도커 설치 
```shell
sudo dnf install dnf-plugins-core -y
sudo dnf config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
sudo dnf install docker-ce docker-ce-cli containerd.io -y
```
3. 도커 서비스 시작
```shell
sudo systemctl start docker
sudo systemctl enable docker
```
4. 젠킨스 도커 이미지 PULL & 컨테이너 시작 ( 8081로 실행 )
```shell
sudo docker run -d -p 8081:8080 -p 50000:50000 --name jenkins --restart=always -v jenkins_home:/var/jenkins_home jenkins/jenkins:lts
```
5. 젠킨스 초기 비밀번호 보기
```shell
sudo docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```
6. 초기 비밀번호 입력 후, Install Suggested Plugins

<br>

### 추가 애플리케이션 실행을 위한 인스턴스 생성
- Type : Cloud Compute - Shared CPU 
- Region : Seoul
- OS : Rocky Linux 9
- Plan : AMD High Performance 25 GB NVMe  ( 메모리 1GB )
- Additional Features : Auto Backups Disbled, IPv6 만 선택
- Hostname : application-instance-1

<br>

## Jenkins Pipeline 생성
#### 1. 새로운 item 추가 → 원하는 파이프라인 입력 후, Pipline 생성

#### 2. Configure 하단 Pipeline Script 작성

#### ＊maven 설정 추가하기
> org.codehaus.groovy.control.MultipleCompilationErrorsException: startup failed:
WorkflowScript: 5: Tool type "maven" does not have an install of "M3" configured - did you mean "null"? @ line 5, column 15. 발생 가능

 : Jenkins 관리 → Tools → Maven Installations 의 Add Maven 클릭 후, Name 에 스크립트에 작성할 Maven 의 이름을 입력한다. ( 버전은 설정되어있는 대로 )
![image](https://github.com/user-attachments/assets/4c8b5dcc-7336-4441-ab0d-d47ffa8b0e61)

<br>

#### ＊SSH 인증 절차 추가
> Host key verification failed. scp: Connection closed 발생 가능

: 서버로 SSH 접속을 하기 위해서는 ip/pw 로 접속하거나 미리 등록된 키를 통해 접속하는 등의 인증 수단이 필요하다. 
- Jenkins 개인키와 공개키 쌍 생성
```shell
> docker exec -it [jenkins-conatinerid] /bin/bash
$ ssh-keygen -t rsa -b 4096 # 개인키와 공개키 쌍 생성
Generating public/private rsa key pair.
Enter file in which to save the key (/var/jenkins_home/.ssh/id_rsa): # Enter
Created directory '/var/jenkins_home/.ssh'.
Enter passphrase (empty for no passphrase): # Enter
Enter same passphrase again: # Enter
Your identification has been saved in /var/jenkins_home/.ssh/id_rsa
Your public key has been saved in /var/jenkins_home/.ssh/id_rsa.pub
The key fingerprint is:
SHA256:R7ir6AH0wYTykgh0AUYclJQ5p0E/pOP4VsNaKLpN8mY jenkins@5f0121399dfa
The key's randomart image is: 
# ...
```

- jenkins의 공개키 출력하여 복사
```shell
$ cat /var/jenkins_home/.ssh/id_rsa.pub
```

- application-instance 에 공개키 저장
```shell
docker exec -it application-instance-1 /bin/bash

$ vi ~/.ssh/authorized_keys 
# 위에서 복사한 공개키 붙여넣고 저장
```

<br>

#### ＊SSH Agent 설치
> java.lang.NoSuchMethodError: No such DSL method 'sshagent' found among steps [..] 발생 가능

- Jenkins 관리 > Plugins > SSH Agent 검색하여 install 
- Jenkins 관리 > Credentials > Domain - (global) 클릭하여, Add Credential 로 인증 방법 지정
  - Kind : SSH username with private key
  - ID : 인증 수단의 id ( ex> `deploy_ssh_key` )
  - Username : jenkins or root 
  - Private Key : Jenkins 서버 내 아래 경로에서 확인
```shell
  cat /var/jenkins_home/.ssh/id_rsa
```

<br>

#### ＊JDK 설정
> Error: A JNI error has occurred, please check your installation and try again .. this verions of the Java Runtime only recognizes class file veresion up to 52.0 발생 가능.

- 인스턴스 내 설정된 자바 버전 확인하기
```shell
java -version
```
- 실행할 애플리케이션의 자바 버전과 동일한 버전으로 설치 및 설정 변경
```shell
sudo dnf install java-17-openjdk
sudo alternatives --config java 
# 동일한 버전 선택
```

<br>

#### ＊서버 인스턴스의 방화벽 설정
> 웹으로 접속 시, ERR_CONNECTION_TIMED_OUT 발생 가능.

- 방화벽 오픈
```shell
sudo firewall-cmd --zone=public --add-port=8080/tcp --permanent # 오픈
sudo firewall-cmd --reload # 재시작
```

<br>

#### ＊전체 Pipeline Script

```groovy
pipeline {
    agent any
    tools {
        maven "M3" // Jenkins에서 설정한 Maven의 이름
    }
    stages {
        stage('Checkout') {
            steps {
                git url: '[Git 리포지토리의 원격 URL]', branch: 'main'
            }
        }
        
        stage('Build') {
            steps {
                script {
                    sh 'mvn clean package'
                }
            }
        }
        
        stage('Deploy') {
            steps {
                script {
                    def jarFile = '[Maven 빌드로 생성된 jar 파일의 위치]'
                    def serverIp = '[원격 서버의 IP 주소]'
                    def deployPath = '[배포 경로]'
                    def runAppCommand = "[애플리케이션을 실행하기 위한 명령어]"
                    
                    // 서버에 파일을 SCP (SSH로 접속) 로 전송
                    sh "scp -o StrictHostKeyChecking=no $jarFile root@$serverIp:$deployPath/"
                    
                    // 원격 서버에서 애플리케이션 실행
                    sshagent(['deploy_ssh_key']) { // 'server-ssh-credentials'는 Jenkins에서 설정한 credentials ID
                        sh "ssh -o StrictHostKeyChecking=no root@$serverIp '$runAppCommand'"
                    }
                }
            }
        }
    }
    
    post {
        success {
            echo 'Deployment was successful.'
        }
        failure {
            echo 'Deployment failed.'
        }
    }
}
```

<br>

## 참고
[인프런 - 애플리케이션 배포 자동화와 CI/CD](https://inf.run/WqKp9) 