## (aws) Detection - CloudWatch Logs, Alarms, Insights
> CloudWatch Logs, CloudWatch Alarms, CloudWatch - Contributor Insights

<br>

## CloudWatch Logs
- log groups : 로그가 저장될 디렉토리 역할, 보통 애플리케이션명 사용
- log stream : log groups 내 파일 역할, application / log files / containers 와 같은 인스턴스 정의
- 로그 만료 정책 정의 ( never expire, 1 day to 10 years ..)
- S3 (exports), Kinesis Data Streams, Kinesis Data Firehose, Lambda, OpenSearch 로 로그를 보낼 수 있다
- 로그들은 암호화되며 ( default ), KMS 기반 암호화도 가능하다
#### Sources
- SDK, CloudWatch Logs Agent, CloudWatch Unified Agent
- Elastic Beanstalk, ECS, Lambda, VPC Flow Logs, API Gateway, CloudTrail, Route53

<br>

## CloudWatch Alarms
- 메트릭(지표)에 대한 알람을 설정할 수 있다
- sampling, %, max, min 등 다양한 옵션이 있다
- 알람 상태 : `OK`, `INSUFFICIENT_DATA`, `ALARM` 
- Periods
  - 메트릭을 평가하기 위한 초 단위의 시간 길이 ( 데이터를 묶는 시간 단위 )
  - 고해상도 커스텀 메트릭(High-Resolution Custom Metrics)을 사용할 때는 데이터 수집 주기를 10초, 30초 또는 60초의 배수로 설정할 수 있다 
- 알림이 실제로 잘 작동하는지 테스트하기 위해 AWS CLI를 사용해서 경보의 상태를 강제로 `Alarm` 상태로 변경한다
```bash
aws cloudwatch set-alarm-state --alarm-name "myalarm" --state-value ALARM --state-reason "testing purposes"
```

### CloudWatch Alarm Targets
- EC2 Instance : Stop, Terminate, Reboot, Recover
- Tirgger Auto Scaling action
- Send notification to SNS 

### Composite Alarms
- cloudwatch alarms 은 하나의 메트릭을 대상으로 한다 
- composite alarms 은 여러개의 다른 알람들의 상태를 모니터링한다 
- AND, OR 연산을 통해 조건을 만들 수 있다 
  - ex> CPU 사용량이 높고 네트워크 아웃바운드가 높다면 알람을 하지 마라
- 이를 통해 alarm noise 를 줄일 수 있다

<img width="559" height="282" alt="image" src="https://github.com/user-attachments/assets/6019cb9a-7f36-4fdf-92a0-1847e47fb3a5" />


#### EC2 Instance Recovery
- 인스턴스 상태 체크 항목을 CloudWatch Alarm 에 정의할 수 있다 → `StatusCheckFailed_System`
```
1. Instance status = check the EC2 VM
2. System status = check the underlying hardware
3. Attached EBS status = check attached EBS volumes
```
- 조건에 따라 상태 체크 실패 시 EC2 인스턴스 복구를 시행할 수도 있다
- 복구 시 같은 private, public, elastic ip, metadat, placement group 을 갖는다
- SNS Topic 을 통해 복구 후 알람을 받을 수도 있다 

<br>

## CloudWatch - Contributor Insights
- 로그 데이터를 분석하여 어떤 요소가 로그를 발생시키고 있는지 시계열 차트를 생성한다 
- 네트워크 트래픽을 많이 유발하거나 에러를 던지는 주범을 찾아 원인을 한 눈에 파악할 수 있도록 한다 
- 활용 예시 
  - 수백 대의 서버 중 트래픽을 많이 발생시키는 서버 찾기
  - 대역폭을 가장 많이 쓰는 헤비 유저 찾기
  - 웹사이트에서 500 에러를 가장 많이 발생시키는 특정 API 주소 찾기
- VPC, DNS 등 AWS 에서 생성된 모든 로그에 대해 동작한다 
- AWS 가 만든 built-in rules 를 활용하거나 직접 규칙을 정의하여 사용할 수도 있다

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)