## (aws) Identity and Access Management - EC2 Instance Metadata Service (IMDS)
> EC2 Instance Role, Restrict Access, IMDSv2 vs. IMDSv1, Requiring the usage of IMDSv2, Require EC2 Role Credentials to be retrieved from IMDSv2

<br>

## AWS EC2 Instance Metadata Service (IMDS)
- EC2 가상 서버 자기 자신에 대한 주요 정보(메타데이터)를 서버 내주에서 직접 조회할 수 있도록 하는 전용 내부 서비스
- EC2 인스턴스에 대한 정보 : 서버 IP 주소, 인스턴스 ID, 인스턴스 타입, 보안 그룹, 연결된 IAM Role 등
- EC2 내부에서 [http://169.254.169.254/latest/meta-data](http://169.254.169.254/latest/meta-data) 로 요청을 보내 정보를 조회한다
- 메타데이터는 키-값 형태로 저장된다 
- 인스턴스의 호스트 이름 설정, 네트워크 구성, 소프트웨어 설치와 같은 작업 자동화에 유용하다

<br>

### EC2 Instance Role 
- EC2에 IAM Role 이 부여되었을 때, 인스턴스 내부 애플리케이션이나 CLI/SDK 가 Temporary Credentilas 을 얻을 수 있다
1. EC2 내부 애플리케이션이 S3 를 이용해야 할 때, 메타데이터 주소로 HTTP 요청을 보낸다 ([http://169.254.169.254/latest/meta-data/iam/security-credentials/role-name](http://169.254.169.254/latest/meta-data/iam/security-credentials/role-name))
2. IMDS 서비스 엔드포인트는 요청을 검증한 뒤 해당 EC2 에 부여된 IAM Role 의 임시 액세스 키/보안 토큰 정보(json)를 응답해 준다
```json
{
  "Code" : "Success",
  "LastUpdated" : "2026-08-13T14:20:00Z",
  "Type" : "AWS-HMAC",
  "AccessKeyId" : "ASIAIOSFODNN7EXAMPLE",
  "SecretAccessKey" : "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY",
  "Token" : "IQoJb3JpZ2luX2VjEAMaCXVzLWVhc3QtMSJHMEUCIQ...",
  "Expiration" : "2026-08-13T20:55:00Z"
}
```
3. 애플리케이션 개발자가 이 키를 가져오는 코드를 작성할 필요 없이, SDK 나 CLI 가 알아서 이 조회를 수행하고 만료 전 주기적으로 키를 갱신해준다 

<br>

### Restrict Access
- 로컬 방화벽 규칙을 사용해 일부 또는 전체 프로세스의 메타데이터 접근을 비활성화할 수 있다
- 리눅스에서는 `iptables`, FreeBSD에서는 `PF`나 `IPFW` 방화벽 도구를 활용한다
```bash
# apache 계정으로 실행되는 프로세스가 메타데이터 IP(169.254.169.254)로 나가는 TCP 요청 거부
sudo iptables --append OUTPUT --proto tcp --destination 169.254.169.254 \
--match owner --uid-owner apache --jump REJECT
```
- AWS 콘솔이나 AWS CLI 를 사용해 접근을 완전히 끌 수 있다 (`HttpEndpoint=disabled`)
- 접근을 꺼버린 경우, 부팅 시 실행되는 User Data 스크립트도 사용할 수 없게 된다 

<br>

### IMDSv2 vs. IMDSv1
- IMDSv1 은 [http://169.254.169.254/latest/meta-data](http://169.254.169.254/latest/meta-data) 주소로 직접 접근하여 별도 인증 없이 데이터를 바로 가져올 수 있어 SSRF 에 취약하다 
- IMDSv2 는 헤더에 ttl 을 지정하여 세션 토큰을 먼저 요청(PUT)하고, 이 토큰을 헤더에 담아 호출(GET)해야만 메타데이터를 응답해준다
```bash
TOKEN=`curl -X PUT "http://169.254.169.254/latest/api/token" -H "X-aws-ec2-metadata-token-ttl-seconds: 21600"`

curl http://169.254.169.254/latest/meta-data/profile -H "X-aws-ec2-metadata-token: $TOKEN"
```

<br>

### Requiring the usage of IMDSv2
- 보안이 취약한 기존 IMDSv1 을 차단하고 IMDSv2 만 사용하도록 시스템 설정을 적용한다
- 별도 설정을 하지 않으면 두 버전이 모두 열려 있으므로 IMDSv1 을 수동으로 꺼줘야한다 
- CloudWatch 지표인 `MetadataNoToken` 을 통해 IMDSv1 횟수를 모니터링할 수 있다 
- EC2 서버를 새로 만들 때 `HttpTokens` 옵션을 `required`로 지정하면 v1 요청은 차단되고 v2 요청만 허용된다
- AMI(이미지)를 등록할 때 `--imds-support v2.0` 옵션을 사용하여 IMDSv2 만 사용하도록 강제할 수 있다

<br>

### Require EC2 Role Credentials to be retrieved from IMDSv2
- IMDS 에서 제공하는 AWS credentilas 에는 `ec2:RoleDelivery`라는 IAM 컨텍스트 키가 포함된다 
- 위 정책을 IAM Role이나 S3 버킷 정책에 연결하여 API 를 호출할 때 IMDSv2만 사용하도록 한다
- 계정 조직의 서비스 제어 정책(SCP)으로 연결하여 EC2 자격 증명을 강제할 수 있다 
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Deny",
      "Action": "*",
      "Resource": "*",
      "Condition": {
        "NumericLessThan": {
          "ec2:RoleDelivery": "2.0"
        }
      }
    }
  ]
}
```

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)