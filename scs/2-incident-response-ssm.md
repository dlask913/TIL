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


### Patch Manager
```
[ EC2 / 온프레미스 서버 ] 
       │ (태그 부여: Key="Patch Group", Value="Production-Windows")
       ▼
  [ Patch Group ] ("Production-Windows")
       │ (등록/매핑)
       ▼
[ Patch Baseline ] (예: "릴리스 7일 후 보안 패치 자동 승인" 규칙)
```
- SSM 에이전트가 설치되어 있고 Systems Maanger 가 제어할 수 있도록 IAM 권한이 올바르게 설정된, 관리형 인스턴스의 패치 프로세스를 자동화한다 
- OS updates, application updates, security updates 등 지원
- EC2 인스턴스와 온프레미스 서버 모두 지원
- Linux, macOS, Windows 지원
- 필요할 때 즉시 패치하거나 (on-demand) 유지관리 창 (Maintenace Window) 를 사용하여 예약된 일정에 따라 패치한다 
- 인스턴스를 스캔하여 누락된 패치 정보를 담은 patch compliance report 를 생성한다 
- patch complicance report 를 S3 로 전송할 수 있다 
- **Patch Baseline** 을 활용하여 어떤 패치를 설치하고 걸러낼지 정의한다
- **Patch Group** 으로 인스턴스들을 묶어 어떤 서버에 어떤 패치 기준서를 적용할 지 매핑할 수 있다 ( 인스턴스는 Patch Group 이라는 태그 키로 정의해야하며, 하나의 패치 그룹에만 속할 수 있다 )

#### Pre-Defined Patch Baseline
- AWS 가 직접 만들고 관리하며 사용자가 내용을 바꿀 수 없는 기본 제공 패치 규칙
- `AWS-RunPatchBaseline` (SSM 문서) 은 Linux, macOS, Windows Server 의 운영체제 패치와 애플리케이션 패치를 모두 적용한다

#### Custom Patch Baseline
- 규칙을 처음부터 끝까지 직접 커스텀하는 패치 규칙
- 자신만의 패치 기준서를 생성하고 어떤 패치를 자동 승인할지 선택한다. 
- 운영 체제, 허용된 패치(화이트리스트), 거부된 패치(블랙리스트) 등을 지정한다
- 기본적으로 Linux 인스턴스들은 OS 벤더가 제공하는 기본 원격 저장소나 AWS 가 미러링해둔 기본 Repo 에 접속하는데 인터넷 연결이 차단된 **프라이빗 서브넷 환경에서는 사설 저장소를 바라보도록** 할 수 있다

#### 패치 작업 프로세스
1. 패치 명령 : AWS Console(수동 클릭), AWS SDK(코드 호출), Maintenance Windows(예약된 일정) 중 하나를 통해 인스턴스들에 대한 패치 작업 시작 명령
2. Run command - Rate Control : 실행할 지침서인 AWS-RunPatchBaseline 문서 장착, Rate Control 적용으로 대상 인스턴스를 한 번에 다운시키지 않도록 동시 실행할 최대 서버 대수나 퍼센트를 계산하여 순차적으로 명령을 전달할 준비
3. 대상 인스턴스로 명령 송신 : 규칙에 맞는 인스턴스(with SSM agent)들에게 명령 송신
4. 인스턴스의 자기 상태(태그) 확인
5. Patch Manager 에게 매핑 규칙 쿼리
6. 패치 기준서(Patch Baseline ID) 최종 매칭
7. 지침에 따른 패치 스캔 및 설치 (Execution)
![image](https://docs.aws.amazon.com/ko_kr/systems-manager/latest/userguide/images/patch-groups-how-it-works.png)

<br>

### Session Manager
- EC2 와 온프레미스 서버에서 secure shell 을 실행할 수 있게 한다
- AWS Console, AWS CLI, or Session Manager SDK 를 통한 접속
- SSH, bastion hosts, SSH keys 가 필요없다 (인바운드 포트를 전부 닫아두어도 작동)
- Linux, macOS, Windows 를 지원한다
- 인스턴스 접속 및 실행한 명령어들을 로그로 남긴다
- Session log data 는 S3 나 Cloudwatch Logs 로 보낼 수 있다
- CloudTrail can intercept StartSession events

#### IAM 권한 및 통제 방식
- 어떤 사용자/그룹이 Session Manager 에 접근할 수 있는지, 어떤 인스턴스에 접속할 수 있는지 제어
```json
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "ssm:StartSession"
            ],
            "Resource": [
                "arn:aws:ec2:*:*:instance/*"
            ],
            "Condition": {
                "StringEquals": {
                    "aws:ResourceTag/Environment": "Dev"
                }
            }
        },
```
- 태그를 사용하여 특정 EC2 인스턴스에 대한 접근만 제한할 수 있다
- SSM 에 접근하는 권한 외에도 S3 및 CloudWatch 에 로그를 기록할 수 있는 권한이 필요하다
- 선택적으로, 사용자가 세션 내에서 실행할 수 있는 명령어를 제한할 수 있다 

#### SSH vs SSM Session Manager
- SSM Session Manager 를 사용하면 인바운드 규칙 설정은 필요없는데 EC2 에 SSM Agent 가 설치되어 있어야 하고 user 에게 올바른 IAM Permissions 을 부여하여 Session Manager 를 통해 EC2 에 접속하도록 한다
- 로그는 S3 나 CloudWatch 로 보내진다

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)