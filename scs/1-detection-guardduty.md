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

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)
[AWS 블로그](https://aws.amazon.com/ko/blogs/korea/amazon-guardduty-now-supports-amazon-eks-runtime-monitoring/)