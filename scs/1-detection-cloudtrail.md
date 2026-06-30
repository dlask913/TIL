## (aws) Detection - CloudTrail
> CloudTrail Events, Insights, Retention, Data Lake, EventBridge Integration, Log File Integrity Validation, Athena

<br>

## CloudTrail 

- AWS Account 에 대한 governanace, compliance, audit 을 제공한다
- cloudtrail 은 기본적으로 활성화된다
- AWS Account 가 Console, SDK, CLI, AWS Services 를 실행한 API calls, 히스토리를 볼 수 있다 → input
- CloudTrail 로그는 CloudWatch Logs 혹은 S3 에 저장할 수 있다 → output
- All regions (dafulat) 혹은 a single region 에 대해 적용된다
- 만약 AWS 에서 리소스가 지워졌다면, 이력을 조사하기 위해 CloudTrail 을 확인하면 된다
- 실시간 서비스가 아니며 api call 은 15분 이내에 전달되고 log files 은 S3 로 5분마다 전달한다.

<br>

### CloudTrail Events
#### 관리 이벤트 (Management Events)

- AWS 계정 내에 있는 리소스에 수행되는 작업들
- 새로운 Trail 을 만들면 AWS 는 기본적으로 관리 이벤트를 수집하도록 설정한다. 
- Read Events 와 Write Events 로 구성된다 
- Example
```
Configuring security (IAM AttachRolePolicy) 
Configuring rules for routing data (EC2 CreateSubnet) 
Setting up logging(CLoudTrail CreateTrail)
```

#### Data Events
- 리소스 내부 실제 데이터에 대한 작업을 기록하는 이벤트로, 작업량이 너무 많기 때문에 디폴트로 기록되지는 않는다
- 데이터가 많은 만큼 로그 저장 비용 및 수집 비용이 상당해진다. 
- S3 object-level activity (GetObject, DeleteObject, PutObject) -  Read 와 Write Event 
- Lambda function execution activity - 함수가 실제로 실행되는 작업

<br>

### CloudTrail Insights

<img width="1024" height="559" alt="Image" src="https://github.com/user-attachments/assets/a835d4b5-5ea7-4dfe-bd0b-fa882b69f346" />

- AWS 계정의 이상 행위를 탐지하기 위해 활성화한다
- 대표적인 탐지 예시 : 과도한 리소스 프로비저닝, AWS 서비스 한도 도달 및 초과 발생, IAM 권한 관련 명령이 폭발적으로 수행, 유지 관리 작업이 누락
- baseline 을 생성하기 위해 정상적인 관리 이벤트를 분석하고 비정상적인 패턴을 감지하기 위해 Write 이벤트를 지속적으로 분석한다. 
- 이상 행위를 발견한 경우 조회 방법
```
1. anomalies appear in the cloudtrail console  - 대시보드 시각화
2. event is sent to S3 - 장기 보관
3. eventbridge event is generated - 실시간 자동화 및 알림
```

<br>

### CloudTrail Events Retention
- 90일간의 관리 이벤트를 무료로 보관해준다.
- 그 이상 유지하고 싶으면 S3 로 이동시킨다. 로그를 지속적으로 전송하도록 설정한다.
- S3 에 쌓이는 trail 로그는 메모장으로 열어 분석하기 힘들기 때문에 Athena 를 통해 분석한다
```sql
-- example
SELECT eventTime, eventSource, eventName, sourceIPAddress
FROM your_cloudtrail_table
WHERE userIdentity.userName = 'user_gildong'
ORDER BY eventTime DESC
LIMIT 50;
```

<br>

### Data Lake
- 완전 관리형 서비스로서, 이벤트 데이터를 모아 저장하고 간편하게 조회(query events)할 수 있는 서비스
- 중앙집중식 data store 에서 서로 다른 데이터 타입의 이벤트들을 저장할 수 있다
( CloudTrail events, CloudTrail Insights Events, AWS Config Configuration Items, AWS Audit Manager, 3rd party events ) 
- SQL query language 를 사용하여 cloudtrail data 를 쿼리할 수 있다
- 데이터 변경이 불가능하고 최대 10년까지 저장 가능하다
- AWS Organizations 에 속한 모든 계정 이벤트들을 한 곳에 저장한다 (multiple Accounts and Regions)
- Data store 에 Resource-based Policy 를 attach 할 수 있다
- Query Results Validation - 쿼리 결과가 변하지 않도록 보장

<br>

### EventBridge Integration

1. API Call 발생 : 사용자가 AssumeRole 을 호출하거나 누군가 EC2 인스턴스를 시작 (RunInstances)
2. CloudTrail 기록 : CloudTrail 이 API 호출을 감지하고 이벤트 로그 기록
3. EventBridge 포착 : EventBridge 는 CloudTrail 이 기록한 이벤트를 실시간으로 전달받는다 
```json
{
  "source": ["aws.sts"],
  "detail-type": ["AWS API Call via CloudTrail"],
  "detail": {
    "eventSource": ["sts.amazonaws.com"],
    "eventName": ["AssumeRole"]
  }
}
```
4. Rule 매칭 및 트리거 : EventBridge 에 설정된 규칙에 따라 지정된 타깃 SNS 혹은 Lambda 로 메시지 전송

<br>

### Log File Integrity Validation
- 위 기능을 활성화하면, 매 시간마다 CloudTrail digest files 이 S3 에 저장
- 로그 파일과 별개로 Digest Files 저장되며 로그 파일들이 중간에 위조되거나 변조, 삭제되지 않았는지 확인하기위한 검증용 파일이다
- 다이제스트 파일에 적힌 해시값과 현재 S3 에 남아있는 실제 로그 파일의 해시값을 대조하여 위변조를 감지할 수 있다
- SHA-256 해싱과 RSA 디지털 서명을 조합하여 다이제스트 파일을 생성
- S3 버킷을 보호하기위해 bucket policy, versioning, MFA Delete protection, encryption, object lock 을 사용
- IAM 정책을 통해 CloudTrail 설정을 변경하거나 로그를 임의로 조작하지 못하도록 권한을 최소화한다

<br>

### Organization Trail 
- 관리자 계정 하나와 여러명의 멤버 계정으로 구성된다 
- 모든 멤버 계정에 대한 API 호출 이벤트를 로깅한다 
- 멤버 계정은 trail 설정 변경이 불가하고 (readonly) delegated admin 기능을 제공한다

<br>

### CloudTrail to CloudWatch Metrics Filter
- Metrics Filter 를 사용하면 특정 기간에 대한 임계값을 설정할 수 있다는 장점이 있다. 
![image](https://d2908q01vomqb2.cloudfront.net/972a67c48192728a34979d9a35164c1295401b71/2022/01/11/fig1bb.png)

<br>

### Integration with Athena
- 보안과 compliance 준수를 위해 S3 에 저장되어있는 CloudTrail Log 를 Athena 를 통해 분석한다
- CloudTrail Conole 에서 Athena table 를 직접 생성하고 S3 Bucket 위치를 지정한다
![image](https://d2908q01vomqb2.cloudfront.net/af3e133428b9e25c55bc59fe534248e6a0c0f17b/2023/04/13/Analyze-Connect-Activities-Figure-01.png)


<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)