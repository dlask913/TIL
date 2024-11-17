## (ci/cd) WebHook 를 활용한 배포 자동화
> WebHook 를 활용한 배포 자동화 ( 젠킨스 파이프라인 구축 이후, [참고](https://github.com/dlask913/TIL/blob/main/devops/jenkins-pipeline.md) )

<br>

## WebHook 를 활용한 배포 자동화
> 커밋&푸쉬 → WebHook 이벤트 전달 → 소스코드 다운로드 → 빌드&패키징 → 패키징된 파일 전송 & 실행

### 1. GitHub personal token 생성 ( [참고](https://github.com/dlask913/TIL/blob/main/devops/github-actions-pipeline-guide.md) )
- `admin:repo_hook` 체크하여 generate ( private repo 인 경우 repo 도 체크 )

-  Jenkins 관리 > Credentials > Domain - (global) 클릭하여, Add Credentials
![image](https://github.com/user-attachments/assets/9fd4f653-d76e-4c76-a6d8-a9c186f19be5)


### 2. WebHook 설정
- Repository 내 Settings → Webhooks 클릭하여 add Webhook
- Payload URL 의 경우, jekins ip 와 port 뒤에 `/github-webhook/` 을 포함하여 작성한다.
- Content type 및 trigger 조건은 상황에 맞게 설정한다.
![image](https://github.com/user-attachments/assets/21ce3bec-cf02-493a-a061-bf818bb05412)

### 3. Jenkins 설정 변경
- 파이프라인 구성(Configure) 내 Github project 에 Repository URL 지정
![image](https://github.com/user-attachments/assets/206ef00f-398b-428f-97bb-9e8f198b8556)

- Build Trigger 하위 GitHub hook trigger for GITScm polling 체크 ( 빌드 트리거 지정 )
![image](https://github.com/user-attachments/assets/fcbdeb19-ef6d-4853-a05e-94493ae028f6)

- private repository 인 경우, pipeline script 에 credentialsId 추가
```groovy
stage('Checkout') {
	steps {
		git url: '', branch: 'main', credentialsId: 'github_token'
	}
}
```

### 4. push 및 배포 확인
- github repositoy 에서 push 를 하여 jenkins 에서 자동 빌드가 수행된 것을 확인한다.
- 만약, jenkins 에서 동작을 수행하지 않는다면 **직접 빌드를 한 번 수행하고 다시 push** 이벤트를 발생시킨다. 

<br>

### 애플리케이션 종료 로직 추가하기
: 애플리케이션을 연달아 빌드하는 경우 기존 애플리케이션이 특정 포트를 사용하고 있기 때문에 포트가 이미 사용중이라는 로그와 함께 빌드가 실패한다. 
- 스크립트 내 애플리케이션 종료 로직 추가하기
```groovy
script {
	..
	def checkLogCommand = ".."
	
	// 기존 애플리케이션 종료
	sshagent(['deploy_ssh_key']) {
		sh script: "ssh root@$serverIp 'pgrep -f [jar명].jar && pkill -f [jar명].jar || echo \"No process found\"'", returnStatus: true
	}

	sh "scp -o StrictHostKeyChecking=no $jarFile root@$serverIp:$deployPath/"
	..
```

<br>

## 참고
[인프런 - 애플리케이션 배포 자동화와 CI/CD](https://inf.run/WqKp9) 
