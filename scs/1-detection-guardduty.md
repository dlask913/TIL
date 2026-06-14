## (aws) Detection - GuardDuty
> GuardDuty Use Cases, Protection Plans, Findings

<br>

## Amazon GuardDuty

<img width="2858" height="1231" alt="Image" src="https://github.com/user-attachments/assets/e2b18522-3787-4df8-bda6-eadbc51fc93d" />

- AWS 계정, 워크로드, 컨테이너 및 데이터(S3 등)을 보호하기 위해 악의적인 활동을 탐지하는 지능형 **위협 탐지** 서비스
- SW 없이 클릭 한 번으로 활성화시킬 수 있으며 30일 평가판이 제공된다
- Extended Threat Protection : 여러 데이터 소스와 AWS 리소스에 걸친 multi-stage attacks 탐지 가능 (자동 활성화)
- Eventbridge 를 통해 탐지 결과를 받아 SNS 로 전송하여 알림 
#### 분석 대상
- CloudTrail Management Events - unusal API calls, unauthorized deployments (무단 배포), ..
- VPC Flow Logs - anomalous traffic, unusual IP addresses .. 
- Route 53 DNS Query Logs - compromised EC2 instances sending encoded data within DNS queries (DNS Tunneling)

<br>

### Use Cases
#### 1. Compromised Instances
- 손상된 EC2 와 ECS/EKS 컨테이너 감지 가능
- Malware activity, crypto-mining(암호화폐 채굴), communicate with malicious domains, communicate with TOR network, processes creating reverse shell(역방향 셸 생성 프로세스)..
#### 2. Anomalous Network Traffic
- 비정상적이거나 의심스러운 네트워크 동작 탐지
- 악성 IP 와의 통신, data exfiltration (데이터 탈취), port scanning, brute force attacks, outbound port scans ..
#### 3. Compromised AWS Account
- AWS account 리소스의 의심스러운 사용 탐지
- 익숙하지 않은 IP 주소 통신, 비정상적인 IAM credentials, enumeration of IAM roles, CloudTrail 로깅 비활성화 시도

<br>

### Protection Plans
- AWS 환경 전체가 아닌 특정 핵심 인프라나 서비스 영역별로 맞춤형 감시를 추가하는 확장 옵션
- 기본적으로 계정 활동(CloudTrail) 이나 전체 네트워크 흐름(VPC Flow Logs, DNS 로그)를 감시하지만 각 서비스 특성에 맞는 정밀 검사 수행을 위해 보호 계획들이 세분화되어 제공
```
1. Malware Protection for EC2 - scans EBS volumes to detect malwares
2. EKS Protection - 대상 Kubernetes 클러스터의 감사 로그 모니터링
3. Runtime Monitoring - EC2, ECS, EKS 인스턴스의 OS 레벨 모니터링
4. Lambda Protection - VPC Flow Logs 를 사용하여 Lambda 네트워크 activity 로그 분석
5. S3 Protection - data exfiltration 과 같은 데이터 유출 탐지
6. Malware Protectino for S3 - 새롭게 업로드된 데이터 탐지
7. Malware Protection for AWS Backup - Backup  리소스 탐지 (EBS Snapshots, AMIs)
8. RDS Protection - RDS and Aurora 로그인 활동 분석
```

<br>

### Findings
- GuardDuty 는 CloudTrail logs, VPC Flow logs, EKS logs 로부터 독립적으로 로그 데이터들을 직접 가져와서 감시한다 (Agentless)
- 각 결과는 0.1 ~ 8+ (High,Medium,Low) 라는 severity value (위험도 수치) 를 갖는다
- 탐지 대상인 리소스에서 만든 API calls 은 GuardDuty findings 콘솔에서 확인할 수 있다 
- Finding naming convention
```
ThreatPurpose:ResourceTypeAffected/ThreatFamilyName.DetectionMechanism!Artifact

[위협목적]:[리소스타입]/[위협패밀리].[탐지메커니즘]
```


#### Findings Automated Response
- 자동으로 security issue 관련 응답은 EventBridge 로 전송하여 자동화를 구축할 수 있다
- EventBridge 에서 SQS 로 데이터를 보내거나 Lambda 를 트리거하거나 SNS 를 호출하여 데이터를 Slack 이나 이메일로 전송할 수 있다
- 이러한 이벤트는 이벤트가 발생한 계정 내에서 게시되지만 GuardDuty 다중 계정을 활성화한 경우 관리자 계정으로 이벤트가 전송된다 → High Severity Notifications 

<br>

### Multi-Account Strategy
- 여러 계정을 관리할 수 있다
- AWS Organization 혹은 초대장을 보내, member account 들을 administrator account 와 연관시킨다 (연결한다)
- Administrator 계정은 member 계정들, findings, suppression rules, trusted IP lists, threat lists 를 관리할 수 있다
- AWS Oraganization 에서 guardduty 관리자가 조직의 관리자일 필요는 없다
- 위임 관리자 (delegated admin) 기능을 통해 회원 계정이 guardduty 관리자를 맡을 수 있다

#### Architectures ( generated via Gemini )

- GuardDuty 에서 발생한 Finding 결과를 EventBridge 를 통해 받아 Step Functions, SNS, Lambda 를 활용해 조치/알림하는 자동화 아키텍처 예시
#### 1. StepFunctions
<img width="1024" height="559" alt="Image" src="https://github.com/user-attachments/assets/78ae0fd5-2dd1-461b-acb8-112d95e062d9" />

#### 2. SNS or Lambda
<img width="1024" height="559" alt="Image" src="https://github.com/user-attachments/assets/8b4dddbd-885c-479b-96d0-89cd7a2bc230" />

<br>

### Trusted and Thread IP Lists
- public ip 주소에서만 동작
- Trusted IP List : IP 혹은 CIDR 범위 목록을 정의하면 findings 을 생성하지 않는다
- Thread IP List : IP 혹은 CIDR 범위 목록을 정의하면 이 리스트를 기반으로 findings 을 생성, 3rd party threat intelligence 에서 제공받거나 직접 정의할 수 있다
- multi-account 를 설정한 경우 관리자 계정만 이 리스트를 관리할 수 있다

#### Suppression Rules
- 새로운 findings 를 필터링하고 보관할 수 있는 방법
- 전체 탐지 결과 유형을 제외하거나 더 세부적인 기준을 정의할 수 있다
- 제외된 결과는 Security Hub, S3, Detective, or EventBridge 로 전송되지 않는다
- 제외된 탐지 결과는 삭제되지 않고 archive 된다
- ex> low-value findings, false-positive findings, or threats you don't intend to act on

<br>

### GuardDuty didn't generate any finding types
#### 문제점
- DNS based findings 를 생성하지 않는다 

#### 원인
- GuardDuty 는 default VPC DNS resolver (AmazonProvidedDNS) 를 사용하는 경우에만 DNS 로그를 처리한다
- 다른 모든 유형의 DNS 리졸버는 DNS 기반 탐지 결과를 생성하지 않는다 ( ex> 구글과 같은 외부 DNS 설정 )
- 만약 guardduty 를 비활성화하는 경우에도 결과를 생성하지 않는다

#### best practice
- 사용하지 않는 region 이어도 guardduty 를 활성화한다
- 해커들이 관리자의 관심 밖에 있는 리전으로 넘어가 대규모 DDos 공격 봇을 구축하는 경우에 GuardDuty 가 비활성화되어있으면 탐지를 할 수 없기 때문
- GuardDuty 비용은 켜놓은 시간이 아닌 **실제로 발생한 데이터의 처리 양을 기준으로 과금되기 때문에 모든 리전에서 활성화하는 것을 권장**한다

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)
[AWS 블로그](https://aws.amazon.com/ko/blogs/korea/amazon-guardduty-now-supports-amazon-eks-runtime-monitoring/)