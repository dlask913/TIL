## (aws) Detection - Detective
> Detective Investigation, Architectures

<br>

## Detective
- GuardDuty, Macie, Security Hub 는 잠재적 보안 이슈에 대한 findings 을 얻을 수 있다
- Detective 는 Machine Learning, statistical analysis and graphs theory 를 통해 그러한 조사 결과 (findings) 를 분석하고 조사하여 빠르게 근본적인 원인을 식별한다
- VPC Flow logs, CloudTrail, GuardDuty 조사 결과를 자동으로 수집하고 처리하여 통합된 뷰를 생성한다 ( when data sources are enabled )
- EKS Audit Logs, Security Hub 와 같은 다른 데이터 소스도 선택적으로 사용 설정할 수 있다 - optional
- 모든 이슈에 대해 통합된 뷰를 생성할 수 있다 ( ex> affected resources, ip address connects to an ec2 instance ,.. )
- 최대 1년간의 집계된 데이터 분석을 조회할 수 있다

<br>

### Detective Investigation
- 보안 사고가 발생했을 때 주체가 누구인지 밝히기 위해 IAM user 와 role 을 조사하는 기능
- 콘솔에서 연관된 모든 IAM 엔티티를 조회할 수 있다

<br>

### Architectures : IAMUser/CloudTrailLoggingDisabled

<img width="1024" height="559" alt="Image" src="https://github.com/user-attachments/assets/f7ba0ffe-61fe-4b8a-b132-7af6c1569cfc" />

#### Detective 와 GruardDuty 가 있을 때 CloudTrail 가 비활성화되었다면?

1. Detect
- 누군가가 CloudTrail 을 비활성화했다면 Detective 가 findings 탐지

2. Triage
- 탐지 결과가 true positive 인지 false positive 인지 판단 

3. Scoping
- 어떤 시스템 혹은 사용자가 손상되었는지, 어디서 공격이 시작되었고 얼마나 지속되었는지 범위 지정

4. Response
- 공격을 멈추기위한 대응을 하고 비슷한 유형의 공격을 예방

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)