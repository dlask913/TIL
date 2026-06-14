## (aws) Detection - Security Hub
> Security Hub 동작 및 비용, Main Features, Ingetration with GuardDuty, Findings, Insights, Custom Actions

<br>

## Security Hub
- 중앙 보안 툴로서 여러 AWS 계정의 보안과 자동 체크되는 보안 사항을 관리
- 통합된 대시보드를 통해 현재 security 와 compliance(준수 상태)를 확인할 수 있고 신속히 조치를 취할 수 있도록 한다
- Security Hub 활성화 시 준수하고싶은 보안 기준을 선택할 수 있다
- 다양한 AWS 서비스 및 서드파티 툴로부터 Predefined formats 또는 Personal findings formats 으로 보안 알림을 자동으로 통합한다
```
# AWS 서비스
Config, GuardDuty, Inspector, Macie, IAM Access Analyzer, AWS Systems Manager, AWS Firewall Manager, AWS Health, AWS Partner Network Solutions
```

#### 동작
- Security Hub 를 사용하기 위해 먼저 **AWS Config 서비스를 활성화**해야 한다
- 한 번에 여러 계정을 커버할 수 있고 (Multi Account) AWS 서비스에서 잠재적 이슈를 모아 Automated checks 한 결과를 Security Hub Findings 대시보드에서 볼 수 있다 
- 확인 결과 문제가 발생하면 EventBridge 이벤트가 발생한다
- 문제의 근원을 조사하기 위해 Amazon Detective 를 이용하면 원인 파악을 빠르게 할 수 있다

![AWS Image](https://d2908q01vomqb2.cloudfront.net/22d200f8670dbdb3e253a90eee5098477c95c23d/2022/11/18/img9-2.png)


#### 비용
- 사용 시간이 아니라 보안 표준 검사 건수 (Security Checks) 횟수에 따라 비용이 청구
- 최초 100000 건의 검사는 건당 $0.001, 이후 400000건의 검사는 건당 $0.00075
- 주의할 점은 AWS Config 비용으로, 계정 내 리소스들의 상태 변경 로그를 전부 기록하기 때문에 여기서 수백 달러 이상 청구될 수 있다

<br>

### Main Features
#### Cross-Region Aggregation
- 여러 리전들의 findings, insights, security scores 를 하나의 통합된 리전으로 집계한다
#### AWS Organizations Integration
- AWS Organization 과 연동하여 특정 계정을 Delegated Administrator 로 지정하면 다른 Member 계정들을 관리 및 새로운 계정을 탐지할 수 있다
- 기본적으로 조직 관리자 계정이 Security Hub 관리자가 된다
#### AWS Config must be enabled
- Security Hub 는 security checks 를 위해 AWS Config 의 많은 부분을 사용하기 때문제 먼저 활성화가 필요하다
- AWS Config 는 모든 계정에서 활성화되어있어야 한다.

<br>

### Ingetration with GuardDuty
```
[ GuardDuty ] ────────(최초 발견 시 1회 전송)────────> [ Security Hub ]
  (위협 탐지)                                          (종합 관제 센터)
      │                                                      │
[Archive 클릭]                                            [ACTIVE]
  (처리 완료)                                          (상태 변함 없음 ❌)
```
1. GuardDuty 를 켜면 Security Hub 와의 연동이 자동으로 활성화된다 (수동으로 비활성화 가능)
2. GuardDuty 는 탐지 결과를 Security Hub 로 보낸다
3. 탐지 결과는 AWS Security Finding Format (ASFF) 로 보내진다
4. 탐지 결과는 보통 5분 이내로 전송된다 
5. GuardDuty 에서 특정 탐지 결과를 아카이브하더라도 Security Hub 로 넘어간 탐지 결과는 자동으로 업데이트 되지 않는다 (일방통행 구조)

<br>

### Findings
- AWS Security Finding Format (ASFF) 포맷을 사용한다
- 자동으로 인프라 상황에 따라 findings 을 업데이트하고 삭제한다 
- 90일이 지난 findings 는 자동으로 삭제된다
- Region, Integration, Security Standard, Insights 에 따라 필터링할 수 있다

<br>

### Insights
- 수만개씩 쌓이는 개별 보안 알림(Findings)들을 사용자가 보기 편하게 특정 기준별로 묶어서 보여주는 실시간 자동 필터링 그룹(컬렉션)
- 예를 들어, 보안 설정이 미흡한 EC2 인스턴스들만 리스트업할 수도 있다
- Security Hub Findings 뿐 아니라 GuardDuty, Inspector, 서드파티 솔루션 등 다양한 소스에서 수집된 알람을 한데 모아서 분석 대상으로 삼을 수 있다
- 각 insight 는 하나의 Group By(그룹화) 기준과 선택적인 filter 에 의해 정의된다
- Built-In Managed Insights : AWS 가 보안 best practice 기반 미리 만들어두고 제공하는 100여개의 통계 템플릿
- Custom Insights : 기본 템플릿 외에 운영자가 직접 필터와 그룹화 조건을 조합해서 만든 맞춤형 인사이트

<br>

### Custom Actions
- 수많은 보안 알림 중 특정 위협을 발견했을 때 운영자가 어떤 방식으로 처리할 것인지 수동으로 제어하는 자동화 파이프라인을 트리거하기위해 사용
- 예를 들어 중요도가 높은 알림에 대해서만 Jira 티켓 발행 등 업무 프로세스를 깔끔하게 정돈 가능
- Custom Actions 을 생성하면 AWS 내부적으로 고유한 Amazon Resource Name (ARN) 이 발급되어 EventBridge 의 고유한 이벤트 소스로 매핑된다 
- 사용자가 콘솔에서 직접 정의한 Custom Action 버튼을 클릭하여 사용자가 정의한 액션을 수행시킨다
- 조치 완료 후 감사 로그 및 대응 이력은 설정을 통해 S3 나 Cloudwatch 에 보관할 수 있다

![aws blog](https://d2908q01vomqb2.cloudfront.net/22d200f8670dbdb3e253a90eee5098477c95c23d/2022/07/13/image1.png)

#### Architecture
```
1. DETECT (감지)
2. INGEST (집계)
3. REMEDIATE (조치)
4. LOG (기록)
```


<img width="800" height="400" alt="Image" src="https://github.com/user-attachments/assets/4974a48f-8fc2-461b-a2a6-069ff9364ec7" />


<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)