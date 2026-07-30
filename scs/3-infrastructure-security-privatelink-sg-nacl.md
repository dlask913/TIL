## (aws) Infrastructure Security - PrivateLink, SG, NACL
> PrivateLink, SG, NACL

<br>

## PrivateLink 
![image](https://docs.aws.amazon.com/images/vpc/latest/privatelink/images/privatelink-concepts.png)

- 서로 다른 VPC 나 AWS 서비스 간 통신을 인터넷에 노출시키지 않고 AWS 내부 백본 네트워크를 통해 1:N **단방향**으로 안전하게 연결해 주는 기술
- **수천 개의 VPC** 에 서비스를 노출하는 가장 안전하고 확장성 높은 방법 (VPC Peering 은 수가 많아지면 관리가 거의 불가능)
- VPC Peering, Internet GW, NAT, 별도의 라우팅 테이블 설정이 필요 없다 
- NLB 와 ENI 를 여러 가용 영역(Multi-AZ)에 분산 배치하면 내결함성(Falut Tolerance)이 확보된다 -> 특정 AZ 에 장애가 발생하더라도 다른 AZ 로 장애 조치되어 서비스 유지

<br>

### PrivateLink & ECS
- Amazon ECS 를 PrivateLink 를 통해 온프레미스(사내망) 및 다른 VPC 로 private 하게 노출할 수 있다
- PrivateLink 는 구조상 NLB(L4) 전면에만 붙일 수 있는데 ECS 는 경로 기반 라우팅이나 SSL 처리를 위해 ALB(L7)가 필요한 경우가 많다
- **NLB ➔ ALB ➔ ECS Tasks** 형태로 연결하여 위 문제 해결 가능
```
[요청] ──► Virtual Private Gateway / ENI
               │
               ▼
       [ AWS PrivateLink ]
               │
               ▼
   [ Network Load Balancer (NLB) ]  <-- PrivateLink 연동용 (L4)
               │
               ▼
 [ Application Load Balancer (ALB) ] <-- L7 경로 라우팅 및 헤더 처리
               │
               ├──────► ECS Task 1 (Private Subnet 1)
               ├──────► ECS Task 2 (Private Subnet 1)
               ├──────► ECS Task 3 (Private Subnet 2)
               └──────► ECS Task 4 (Private Subnet 2)
```

<br>

## SG & NACL

### Incoming & outgoing Request
#### Incoming Request 패킷 통과 순서
1. NACL Inbound Rules
2. SG Inbound Rules
3. EC2
4. SG Outbound Allowed - Stateful
5. NACL Outbound Rules - Stateless
#### Outgoing Request 패킷 통과 순서
1. SG Outbound Rules
2. NACL Outbound Rules

<br>

### Security Groups
- Stateful : 한 번 들어온 요청은 나갈 때 검사하지 않고 나간 요청의 응답은 들어올 때 검사하지 않는다 (상태 유지)
- Inbound 에서 포트를 열어주면 그에 대한 Outbound 트래픽은 SG Outbound 규칙과 상관없이 자동으로 허용된다 
- SG 를 변경한다고 현재 유지중인 연결이 중단되지 않는다
- 연결되어있는 connection 은 타임아웃 될 때까지 유지된다 -> NACL 을 사용하면 연결이 즉시 중단된다

#### Outbound Rules
- Default 는 모든 대역을 허용한다 ( 0.0.0.0/0 )
- 특정 prefix 를 지우거나 허용할 수 있다
#### Managed Prefix List
- 하나 이상의 CIDR blocks 세트
- SG 와 Route Tables 을 더 쉽게 구성하고 관리할 수 있다
- Customer-Managed Prefix List
  - CIDR 블록 세트를 직접 정의하고 관리할 수 있다
  - 다른 계정 혹은 Organization 과 공유할 수 있따
  - 많은 SG 를 한번에 변경할 수 있다
- AWS-Managed Prefix List
  - AWS 서비스에 대한 CIDR 블록 세트
  - 사용자가 생성하거나 공유하거나 변경할 수 없다
  - ex> S3, CloudFront, DynamoDB, Ground Station ..


<br>

### Network ACL (NACL)
- Stateless : 이전 연결 상태를 전혀 기억하지 못하기 때문에 모든 패킷을 독립적인 개체로 판단 (상태 미보유)
- 요청이 들어올 때 Inbound 규칙을 검사하고 그 요청에 대해 응답이 나갈 때도 Outbound 규칙을 따로 검사한다 
- 클라이언트가 서버로 요청을 보낸 뒤 응답을 받을 때, 클라이언트 측은 임의의 임시 포트(1024\~65535)를 사용한다 → **NACL 을 쓸 때는 Outbound 규칙에 해당 임시 포트(1024~65535)를 explicit 하게 Allow** 해두어야 한다
- ssh 통신을 하던 사용자가 있을 때 즉시 연결을 중단하고 싶은 경우 22 SSH DENY 로 SG 를 변경한다고 연결이 끊기지 않는데 NACL 은 즉시 적용된다

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)