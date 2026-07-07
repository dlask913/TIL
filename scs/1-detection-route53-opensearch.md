## (aws) Detection - Route 53, OpenSearch
> Route 53 - DNS Querry Logging/Resolver Query Logging, OpenSearch

<br>

## Route 53
### DNS Query Logging
- Route 53 Resolver 가 받은 public DNS 쿼리 로그 정보
- 인터넷 사용자들이 웹사이트 주소를 찾으려고 Route 53 서버에 물어보는 로그 (인바운드)
- 로그가 발생하는 곳 : Route 53 네임서버 (Public Hosted Zone 전용, Private은 불가)
- 로그는 CloudWatch Logs, S3, Data Firehose 등으로 보낼 수 있다 

<br>

### Resolver Query Logging
- 나의 AWS VPC 내부에 있는 자원들이 외부나 내부 주소를 찾으려고 물어보는 로그 (아웃바운드&내부)
- 로그가 발생하는 곳 : VPC 내장 DNS 서버 (Route 53 Resolver)
- 아래 모든 DNS 쿼리는 로깅된다
```
- Made by resources within a VPC
- From on-premises resources that are using Resolver Inbound Endpoints
- Leveraging Resolvers Outbound Endpoints
- Using Resolver DNS Firewall
```
- AWS Resource Access Manager 를 통해 다른 계정과 공유할 수 있다

#### 동작 순서 예시 (VPC 내부 ➡️ 외부)
1. EC2 Instance -> Route 53 Resolver : example.com?
2. Route 53 Resolver -> Resolver Query Logging : example.com? 
3. Resolver Query Logging -> S3 or CloudWatch Logs or Kinesis Data Firehose : send logs

<br>

### COMMON
- CloudWatch Logs 에 전달된 DNS Logs 에서 구체적인 데이터를 찾고 싶을 땐 CloudWatch Logs Insights 를 most common DNS queries 를 찾고 싶을 떈 CloudWatch Contributor Insights 를 본다

<br>

## Amazon OpenSearch Service
- 이전에는 Amazon Elasticsearch 였는데 이름이 바뀌엇나보다
- DynamoDB 에서는 오직 pk 와 인덱스로 조회를 할 수 있는데 OpenSearch 를 사용하면 필드 검색이 가능하고 심지어 부분 검색도 가능하다
- 단독으로 사용하기보다는 다른 database 를 보완하는 역할로 많이 쓰인다
- 기본적으로 JOSN 형태의 쿼리를 사용하나 SQL플러그인을 활성화하면 SQL 문법을 사용할 수 있다
- 실시간 데이터를 집어넣을 때 (Ingestion) 주로, Kinesis Data Firehose, AWS IoT, CloudWatch Logs 를 사용한다
- 보안 강화를 위해 Cognito & IAM, KMS encryption, TLS 를 지원한다
- Dashboard 를 통해 데이터 시각화가 가능하다
 
#### 관리 모드
- 관리형 클러스터(managed cluster) : 개발자가 직접 구성
- 서버리스 클러스터(serverless cluster) : 서버 관리를 AWS 가 하는 컴퓨팅 인프라 프리 방식

<br>

### DynamoDB with OpenSearch
![image](https://d2908q01vomqb2.cloudfront.net/887309d048beef83ad3eabf2a79a64a389ab1c9f/2024/07/18/DBBLOG-4305-img1.png)

<br>

### OpenSearch patterns Kinesis Data Streams & Kinesis Data Firehose
#### 1. 
```
Kinesis Data Streams
↓
Kinesis Data Firehose   ← data transformation - Lambda Function
(near real time)
↓
OpenSearch
```
#### 2. 
```
Kinesis Data Streams
↓
Lambda Function (real time)
↓
OpenSearch
```

<br>

### Public Access
- 인터넷이 연결된 곳이라면 어디서든 누구나 접속할 수 있는 공개된 주소를 가지고 있다
- Access Policies, Identify-based Policies, and IP-based Policies 를 통해 접근을 제한한다
#### 동작 순서
```
1. 사용자가 ID/PW 방식(HTTP Basic Auth) 등으로 요청을 보낸다
2. Access policy 에 IAM 서명(IAM Signing)이 필수 요구 사항이 아니라면 패스한다
3. 입력한 Basic Credentials가 마스터 사용자 정보와 일치하는지 검증한다
4. FGAC가 작동하여 이 사용자가 해당 인덱스(Index)나 API를 실행할 권한이 있는지 actions 단위로 체크한다
5. 권한에 따라 전체 데이터 또는 특정 필드/문서만 필터링하여(Full or Partial) 반환한다
```

<br>

### Deploy in VPC (VPC 내부 배포)
- 인터넷에 노출되지 않고 AWS VPC 내부 private subnet 안에 OpenSearch 클러스터를 밀어 넣는 방식
- Domain Access Policy : VPC 내에 배포하더라도 **누가 이 OpenSearch 내부 리소스(인덱스 등)에 어떤 액션(`es:*`)을 취할 수 있는가**를 제어하기 위해 Resource-based Policy(자원 기반 정책)를 작성해야 한다
- 도메인 접근 시 허용할 methods 를 정의할 수도 있다
```json
<!-- Grant IAM user full access on all the OpenSearch domain sub-resources-->
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AllowOpenSearchFullAccessOnSubresources",
      "Effect": "Allow",
      "Principal": {
        "AWS": "arn:aws:iam::123456789012:user/target-iam-username"
      },
      "Action": [
        "es:*"
      ],
      "Resource": "arn:aws:es:ap-northeast-2:123456789012:domain/my-opensearch-domain/*"
    }
  ]
}
```

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)