## (aws) Incident Response - Compromised Something
> 모의 해킹, Compromised AWS Resource and Credentials

<br>

## 모의 해킹 (Penetration Testing) on AWS CLoud
- AWS 고객은 8가지 기본 서비스에 대해서는 사전 승인 없이도 자신의 AWS 인스턴스/인프라에 대한 보안 진단이나 모의해킹을 자유롭게 수행할 수 있다
```
1. EC2, NAT Gateways, Elastic Load Balancers
2. RDS
3. CloudFront
4. Aurora
5. API Gateways
6. Lambda and Lambda Edge functions
7. Lightsail resources
8. Elastic Beanstalk environments
```

<br>

## DDos Simulation Testing on AWS

- Controlled DDOS attack 은 애플리케이션의 회복탄력성을 평가하고 실제 공격 발생 시의 대응 프로세스를 연습할 수 있게 한다
- 오직 AWS DDos Test Partner 만 수행이 가능하다
- 타깃은 Protected Resources 혹은 Edge-Optimized API Gateway ( that is subscribed to Shield Advanced )
- AWS 내부에 있는 다른 서버(EC2 등)를 공격 도구로 써서 테스트 대상 시스템을 공격하면 절대 안 된다
- Attack 은 초당 20 Gigabits 를 초과해선 안된다
- Cloudfront 는 초당 5백만 패킷을 초과해선 안되고 다른 서비스는 초당 5만을 넘으면 안된다

<br>

## Compromised AWS Resource and Credentials

### Compromised EC2 Instance
- 각각 다른 AZ 에 같은 Auto Scaling Group 으로 묶여있는 EC2 Instance 2개
- EC2 Instance 는 WebApp SG 를 가지는데 그 중 하나가 침해됨
- offline investigation : instance 중지
- online investigation : snapshot memory or capture network traffic -> 외부에 뭘 유출하려는 지 알 수 있음
- 프로세스 격리 자동화는 Lambda, 메모리 캡쳐 자동화는 SSM Run Command 를 사용할 수 있다
#### 대응
1. instance 메타데이터 캡처
2. Termination Protection 활성화 ( 사라지지않도록 ) 
3. 침해된 instance 분리 - 외부 트래픽이 접근 불가한 Security Group으로 변경
4. ASG 에서 인스턴스 분리
5. ELB 에서 인스턴스 삭제
6. 심도있는 분석을 위해 인스턴스에 연결된 EBS volumes 을 Snapshot → 다른 새로운 인스턴스에 포렌식
7. 문제가 있는 인스턴스에 tag 남기기 ( ex. investigation ticket )

<br>

### Compromised S3 Bucket
1. GuardDuty 에서 침해된 bucket 감지
2. CloudTrail or Amazon Detective 에서 악성행위 (IAM user, role, ..) 및 API 호출 식별
  - 행위 소스가 승인된 액션이었는지 판단
3. S3 보안 강화 (사후 예방 조치)
  - S3 Public Access 차단, S3 Bucket Policies and User Policies, VPC Endpoints for S3, S3 Pre-Signed URLs, S3 Access Points, S3 ACLs

<br>

### Compromised ECS Cluster
1. GuardDuty 에서 영향받는 ECS Cluster 식별
2. 악성 행위의 근원지 확인 (e.g., container image, tasks)
3. 영향받은 tasks 격리 (deny all ingress/egress traffic to the task using security group)
4. 악성행위의 존재를 평가 (e.g., malware)

<br>

### Compromised Standalone Container
1. GuardDuty 에서 악성 컨테이너 감지
2. 악성 컨테이너 격리 (deny all ingress/egress traffic to the container)
3. 컨테이너 내 모든 프로세스 중지 (pause the container)
4. 아니면 컨테이너를 중지하고 GuardDuty에 의해 보관된 EBS 스냅샷(스냅샷 보관 기능)을 확인한다
5. 악성 행위 분석하기

<br>

### Compromised RDS Database Instance
1. GuardDuty 에서 영향받은 DB instance 와 user 감지
2. 정당한 행위가 아닐 경우
  - 네트워크 접근 제한 (SGs & NACLs)
  - 의심스러운 DB 사용자 DB 접근 제한
3. DB user password 변경
4. 유출된 데이터 확인을 위해 DB Audit Logs 확인
5. RDS 보안 강화 (사후 예방 조치)
  - Secrets Manager 를 사용하여 DB 비밀번호를 주기적으로 자동 변경 (로테이션)
  - 비밀번호 없이 DB 사용자 접근을 관리할 수 있도록 IAM DB 인증 사용

<br>

### Compromised AWS Credentials
1. 영향받은 IAM user 식별
2. 노출된 AWS Credentials 교체 (rotate)
3. STS 날짜 조건이 포함된 Explicit Deny 정책을 해당 IAM 사용자에게 연결하여 이미 발급된 임시 자격 증명을 무효화
4. 다른 인가되지않은 활동이 있었는지 CloudTrail 확인
5. 삭제된 Resource 가 있는지 확인하고 변경되었을지 모를 AWS 계정 정보를 확인한다 

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)