## (ci/cd) CI/CD 테스트
> CI/CD 테스트란, CI 테스트 환경 설정, CD 테스트 설정 및 실패 시 롤백 스크립트 추가

<br>

## CI/CD 테스트
### CI 테스트
- 코드 레벨에서 테스트를 수행한다. 
- 테스트 코드를 강제하지 않으면, 처음엔 편할 수 있으나 이후 기능을 수정하고 배포할 때마다 장애 날 확률이 커지게 된다. 
- CI/CD 프로세스 내에서 테스트 코드를 유지 및 관리하도록 한다.

#### 많이 사용되는 CI 테스트 실행 시점
- Push
- Pull Request 생성 시점
- Merge 된 후 
- 배포 시작 시점

<br>

### CD 테스트
- CI 테스트와 동일한 코드 레벨의 테스트를 수행하기도 하지만 보통 각각의 서버에 배포된 후 정상 동작을 하는 지 검증한다. 
- E2E 테스트를 통해 시스템이 정상 동작하는 지 확인한다.

#### 서버에 배포 됐을 때 문제가 되는 경우
- 인증에 사용하는 token 이 만료된 경우
- 방화벽 문제
- 네트워크 장애
- 저장소 장애

<br>

### 배포 전략
1. 롤링 배포
- 서버에 순차적으로 새로운 버전을 배포한다.
- 가장 간단하다.
- 여러 서버가 서로 다른 버전을 실행이 가능하여, 일관성 문제가 발생할 수 있다. 

2. 블루/그린 배포
- 기존 버전을 유지한 상태에서 새로운 버전을 별도 배포하고 새로운 버전으로 한 번에 전환한다. 
- 롤링 배포의 문제점을 해결할 수 있고, 롤백이 간단하다.
- 롤링 배포에 비해 상대적으로 구현이 어렵고 서버 리소스를 더 많이 사용한다.

3. 카나리 배포
- 새로운 버전을 배포해보고 반응을 지켜보며 점진적으로 배포 범위를 확대한다.

<br>

## CI 테스트 환경 설정
- Jenkins 스크립트 내 Test Stage 추가
```groovy
    stages {
	    stage('Checkout') {}
        stage('Build') {}
        
        stage('Test') { // 추가!
            steps {
                script {
                    sh 'mvn test'
                }
            }
        }
        
        stage('Deploy to Server 1') {}
        //..
	}
```

<br>

## CD 테스트 
- 실제 애플리케이션이 정상 동작한다고 판단할 수 있는 API 호출을 수행한다.
- CD 테스트 실패 시 이전 버전을 롤백하는 작업이 필요하다.
- Jenkins 스크립트 내 CD Test 추가 ( [참고](https://github.com/lleellee0/application-deploy-advanced/blob/main/3_3_rollback_fail_pipeline_script) )
```groovy
def deployToServer(serverIp) {
    ..
    def backupJarFile = '[애플리케이션명]-backup.jar' // 추가
    def portNumber = '8080'

    // 기존 애플리케이션 종료
    sshagent(['deploy_ssh_key']) {..}

    // 기존 JAR 파일 백업 // 추가
    sshagent(['deploy_ssh_key']) {
        sh script: "ssh -o StrictHostKeyChecking=no root@$serverIp 'cp $deployPath/[jar명].jar $deployPath/$backupJarFile'", returnStatus: true
    }

    // 서버에 파일을 SCP로 전송
    sh "scp -o StrictHostKeyChecking=no $jarFile root@$serverIp:$deployPath/"

    // 원격 서버에서 애플리케이션 비동기 실행
    sshagent(['deploy_ssh_key']) {
        ..
        if (!deploymentSuccess) {
            error "Deployment to $serverIp failed."
        }

        // CD 테스트 수행 // 추가
        int cdTestResult = sh script: "curl -s -o /dev/null -w '%{http_code}' http://$serverIp:$portNumber/[테스트할파일명].html", returnStatus: true
        if (cdTestResult != 0) { // 0은 curl 명령이 성공했음을 의미
            echo "CD test failed for $serverIp. Rolling back to previous version."
            rollbackToPreviousVersion(serverIp, backupJarFile, deployPath, runAppCommand, checkLogCommand)
            error "Deployment to $serverIp failed due to CD test failure." // CD Test 실패 시 에러 발생
        } else {
            echo "CD test passed for $serverIp."
        }
    }
}

def rollbackToPreviousVersion(serverIp, backupJarFile, deployPath, runAppCommand, checkLogCommand) { // 추가
    def maxAttempts = 40
    def sleepInterval = 3 // 3 seconds

    // 기존 애플리케이션 종료
    sshagent(['deploy_ssh_key']) {
        sh script: "ssh -o StrictHostKeyChecking=no root@$serverIp 'pgrep -f [ㅓjar명].jar && pkill -f [jar명].jar || echo \"No process found\"'", returnStatus: true
    }

    // 백업된 JAR 파일로 롤백
    sshagent(['deploy_ssh_key']) {
        sh script: "ssh -o StrictHostKeyChecking=no root@$serverIp 'mv $deployPath/$backupJarFile $deployPath/[jar명].jar'", returnStatus: true
        sh "ssh -o StrictHostKeyChecking=no root@$serverIp '$runAppCommand'"

        // 로그 파일을 주기적으로 확인하여 애플리케이션 실행 확인
        def attempts = 0
        def rollbackSuccess = false
        while (attempts < maxAttempts) {
            int result = sh script: "ssh -o StrictHostKeyChecking=no root@$serverIp '$checkLogCommand'", returnStatus: true
            if (result == 0) {
                echo "Rollback to previous version on $serverIp was successful."
                rollbackSuccess = true
                break
            }
            attempts++
            sleep sleepInterval
        }

        if (!rollbackSuccess) {
            error "Rollback to previous version on $serverIp failed."
        }
    }
}
```

<br>

## 참고
[인프런 - 애플리케이션 배포 자동화와 CI/CD](https://inf.run/WqKp9) 