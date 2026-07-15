## (aws) Incident Response - AWS Systems Manager
> Features, How Systems Manager Works, Resource Groups, Run Command, Documents, Automation

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

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)