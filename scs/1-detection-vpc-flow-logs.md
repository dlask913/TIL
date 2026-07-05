## (aws) Detection - VPC Flow Logs
> VPC Flow Logs, Troubleshoot SG & NACL issues

<br>

## VPC Flow Logs
- 사용자의 interface 로 들어오는 ip traffic 정보를 수집한다
```
 vpc flow logs, subent flow logs, elastic network interface flow logs
```
- helps to monitor & troubleshoot connectivity issues
- flows log data 는 S3, CloudWatch Logs, Kinesis Data Firehose 로 보낼 수 있다
- AWS 가 관리하는 inerface 의 network 정보도 수집할 수 있다  (ELB, RDS, ElasticCache, Redshift, Workspaces, NATGW, Transit Gateway .. )
- S3 에서 Athena 로 VPC flow logs 를 쿼리하거나 CloudWatch Logs Insights 로 볼 수 있다

<br>

### Troubleshoot SG & NACL issues
#### Incoming Requests
- Inbound REJECT → NACL or SG
- Inbound ACCEPT, Outbound REJECT → NACL

#### Outgoing Requests
- Outbound REJECT → NACL or SG
- Outbound ACCEPT, Inbound REJECT → NACL

<br>

### Architectures
```
1. VPC Flow logs → CloudWatch Logs → CloudWatch Contributor Insights 
   
2. VPC Flow logs → CloudWatch Logs → CW Alarm → SNS
   
3. VPC FLow Logs → S3 → Athena → QuickSight
```

- VPC Flow Logs 와 연결된 IAM 은 CloudWatch Logs로 로그를 Publish 하는 권한이 있어야 한다 (logs:CreateLogGroup, logs:CreateLogStream, logs:PutLogEvents)

<br>

### 수집되지 않는 트래픽
- Traffic to Amazon DNS Server ( custom DNS server traffic 은 로깅 가능 )
- Traffic to Amazon Windows license activation
- Traffic to and from 169.254.169.254 for EC2 instance metadata
- Traffic to and from 169.254.169.123 for Amazon Time Sync Service
- DHCP
- Mirrored traffic
- Traffic to the VPC router reserved IP address (e.g., 10.0.0.1 )
- Traffic Between VPC endpoint ENI and Network Load balancer ENI

<br>

## VPC Network Access Analyzer
- 서버, 데이터베이스 등이 어디랑 통신할 수 있고, 외부에서는 어떻게 내 자원으로 들어올 수 있는지 그 이동 경로를 가상으로 시뮬레이션해서 보여준다 
- 분석하고 싶은 네트워크 접근 기준을 정의 (ex: identify publicly available resources)
- 위에서 설정한 네트워크 보안 기준을 토대로 취약점을 찾아내어 해결하고 보안 정책이 잘 지켜지고 있음을 검증
```
 - Evaluate network access to resources in your VPCs (EC2, RDS, Aurora, OpenSearch, Redshift )
 - Match against the configurations of your VPC reousrces (SG, NACL, NATGW, IGW .. )
```
- json 포맷으로 Network Access Scope 과 알고싶은 network security policy 조건을 정의한다  (e.g., detect public databases)
```json
{
  "MatchPaths": [
    {
      "Source": {
        "ResourceStatement": {
          "ResourceTypes": ["AWS::EC2::InternetGateway"] 
        }
      },
      "Destination": {
        "ResourceStatement": {
          "ResourceTypes": ["AWS::EC2::Instance"],
          "TagFilters": [
            { "Key": "Stage", "Value": "Production" },
            { "Key": "Role", "Value": "DB" }
          ]
        }
      }
    }
  ]
}
```

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)