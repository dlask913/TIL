## (aws) Incident Response - AWS Systems Manager
> Features, How Systems Manager Works, Resource Groups, Run Command, Documents, Automation, Parameter Store, Inventory, State Manager

<br>


## AWS Systems Manager

- EC2 와 온프레미스 시스템을 광범위하게 관리할 수 있도록 돕는다
- 내가 돌리고 있는 서버와 시스템(인프라)들이 지금 안전한지, 설정은 똑바로 되어 있는지, 문제는 없는지 한눈에 파악하고 관리할 수 있게 해준다
- 쉽게 문제를 감지할 수 있다
- compliance 강화를 위한 패치 자동화
- windows, linux os 모두 지원
- cloudwatch metrics / dashboards 와 통합 가능
- AWS Config 와 통합 가능
- EC2 인스턴스 대상 기본 기능 무료

<br>

### Features
- Node Tools : Fleet Manager, Compliance, Inventory, Hybrid Activations, Session Manager, Run Command, State Manager, Patch Manager, Distributer
- Change Management : Automation, Change Calendar, Maintenance Windows, Documents, Quick Setup
- Application Tools : Application Manager, AppConfig, Parameter Store
- Resource Groups
- Operation Tools : Explorer, OpsCenter

<br>

### How Systems Manager Works
- 우리가 컨트롤할 시스템에 SSM agent 를 설치해야한다
- Amazon Linux 2 AMI & some Ubuntu  AMI 에는 디폴트로 설치된다 
- SSM 으로 인스턴스 컨트롤이 안되면 SSM agent 문제일 수 있다
- EC2 가 SSM actions 을 허락하는 적절한 IAM role 을 가질 수 있도록 한다

<br>

### Resource Groups
- 태그로 논리적인 그룹을 생성하여 관리할 수 있다
- 리소스를 논리적인 그룹으로 묶어서 관리할 수 있도록 지원한다
  - Applications
  - Different layers of an application stack
  - Production versus development environments
- Regional service : 글로벌 서비스와 달리 특정 리전 안에서 생성한 리소스들만 그 리전 안의 그룹으로 묶을 수 있다

<br>

### Run Command
- Document(= script) 를 실행하거나 커맨드를 수행한다
- resource groups 을 통해 여러 인스턴스에 커맨드를 수행할 수 있다
- Rate Control(동시성 제어) / Error Control(명령을 수행하다가 에러가 발생하는 상황 제어)
- IAM & CloudTrail 과 통합 -> 누가 수행했는 지 알 수 있음
- SSH, 배스천 호스트가 필요없다
- 콘솔에서 command output 이 보여지며 S3 나 CloudWatch Logs 로 보내진다
- SNS 를 통해 알림을 보낼 수도 있다 (In progress, Success, Failed, .. )
- command 를 호출하기 위해 EventBridge 를 사용할 수 있다

<br>

### Documents
- JSON 혹은 YAML 로 구성된다
- 파라미터와 액션을 정의할 수 있고 많은 문서들이 이미 AWS 에 존재한다
- Run Command 를 통해 실행한다

<br>

### Automation
- EC2 및 기타 리소스의 일반적인 유지 관리 및 배포 작업을 간소화한다 
- Automation Runbook
  - Automation 유형의 SSM Document 로, 리소스에 실행할 actions 정의
  - AWS 가 미리 정의해둔 런북을 사용하거나 직접 만들수도 있다
#### 실행 방식
- AWS 콘솔, AWS CLI, 또는 SDK 를 이용한 수동 실행
- Amazon EventBridge 를 통한 이벤트 트리거 실행
- Maintenance Windows 설정을 통한 스케줄 기반 실행
- AWS Config 규칙 위반 시 Remediation를 통한 실행

<br>


### Parameter Store
- configuration 과 secrets 에 대한 Secure storage
- KMS 를 사용하여 끊김없는 암호화 (Optional)
- Serverless, scalable, durable, easy SDK
- configuration 과 secret 에 대한 버전 관리 가능
- IAM 권한을 체크하여 보안 강화
- Eventbridge 를 통한 알림
- Cloudformation 과의 통합
- Standard 와 Advanced 티어가 있으며 Advanced 는 비용이 발생한다
#### 계층 구조
```
- /my-department/
  - my-app/
    -  dev/
      - db-url
	  - db-password
    - prod/ 
	..
```
- API 호출을 통해 사용 가능 ( ex> prod/payment-service/db/username ) -> Lambda 사용 가능
- IAM 정책을 작성할 때 경로 단위로 접근 제어 가능
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "ssm:GetParametersByPath",
                "ssm:GetParameter"
            ],
            "Resource": "arn:aws:ssm:ap-northeast-2:123456789012:parameter/dev/*"
        }
    ]
}
```
#### Parameter Policies (advanced 만 가능)
- 패스워드와 같은 민감한 데이터의 강제 업데이트나 삭제를 유도하기 위해, 파라미터에 유효 기간(TTL)을 지정할 수 있다
- 한 번에 여러가지 정책을 할당할 수 있다 (ex> Expiration, ExpirationNotification, NoChangeNotification)
```json
{
  "Type": "NoChangeNotification",
  "Version": "1.0",
  "Attributes": {
    "After": "20",
    "Unit": "Days"
  }
}
```

<br>

### Inventory
- EC2, On-premises 인스턴스에 대한 metadata 수집
- ex> installed software, OS drivers, configurations, installed updates, running services ..
- AWS Console 이나 S3 에서 Athena 로 쿼리하거나 QuickSight 로 시각화하여 데이터를 볼 수 있다
- metadata 수집 주기를 정할 수 있다 (minutes, hours, days)
- 여러 AWS accounts 와 regions 으로부터 데이터를 쿼리할 수 있다
- 커스텀 inventory 를 생성할 수 있다 (e.g., rack location of each managed instance)

<br>

### State Manager
- EC2, on-premises 인스턴스의 상태를 정의한 대로 일관되게 유지해주는 자동화 구성 관리 서비스
- Use cases : bootstrap instances with software, patch OS/software updated on a schedule ..
- 정해진 주기에 맞춰 자동 실행
- State Manager Association -> 예약 작업 설정
```
-  무엇을 실행할 것인가? (Document)
- 어떤 서버에 적용할 것인가? (Target)
- 언제 실행할 것인가? (Schedule) 
```

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)