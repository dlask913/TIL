## (aws) Detection - EventBridge
> EventBridge (formerly CloudWatch Events), Event Bus, Schema Registry, Resource-based Policy

<br>

## EventBridge (formerly CloudWatch Events)

- 예정된 스크립트를 실행하도록 cron jobs 스케줄을 생성한다 
```
Schdule Every Hour -> Trigger script on Lambda function
```
- 무언가를 수행하는 서비스에 대한 이벤트를 설정할 수도 있다.  
```
IAM Root User Sign in Event -> SNS Topic with Email Notification
```

#### Example Source (input)
- EC2 Instance (ex> Start Instance)
- CodeBuild (ex> failed build)
- S3 Event (ex> upload object)
- Trusted Advisor (ex> new Finding)
- CloudTrail (any API Call)
- Schedule or Cron (ex> every 4 hours)

#### Example Destination - json (output)
- Compute : Lambda, AWS BAtch, ECS Task
- Integration : SQS, SNS, Kinesis Data Streams
- Orchestration : Step Functions, CodePipeline, CodeBuild
- Maintenance : SSM, EC2 Actions

<br> 

### Event Bus
- Default Event Bus : AWS 서비스 자체에서 발생하는 이벤트 수집
- Partner Event Bus : AWS 와 제휴된 외부 SaaS 애플리케이션 이벤트를 AWS 내부로 가져올 때 사용 (ex> Datadog, Zendesk 등)
- Custom Event Bus : 개발자가 직접 만들어 웹 애플리케이션이나 MSA 간 이벤트를 주고받을 때 사용
- 이벤트 버스는 리소스 기반 정책을 사용하여 다른 AWS 계정에서 접근 가능하다.
- 이벤트 버스로 전송된 이벤트들을 보관할 수 있다 
- 보관된 이벤트들을 다시 replay 할 수 있다

<br>

### Schema Registry
- EventBridge 는 이벤트 버스로 들어오는 이벤트들을 분석하여 그 구조(스키마)를 추론할 수 있다 
- schema registry 를 사용하면 그 스키마를 기반으로 Java, Python 등 코드로 변환해준다 
- 스키마에 버전을 매길 수 있다 

<br>

### Resource-based Policy
- 특정 이벤트 버스에 대한 권한을 관리한다 
- example : allow/deny events from another AWS account or AWS region
- Use case : aggregate all events from your Organization in a single AWS account or AWS region
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "AllowPutEventsToCustomBus",
            "Effect": "Allow",
            "Action": "events:PutEvents",
            "Resource": "arn:aws:events:ap-northeast-2:123456789012:event-bus/MyCustomEventBus"
        }
    ]
}
```

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)