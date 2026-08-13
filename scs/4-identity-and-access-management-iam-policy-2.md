## (aws) Identity and Access Management - IAM Policy 2
> IAM Permission Boundaries, IAM Evaluation Logic, IAM Roles vs Resource-Based Policies

<br>

## IAM Permission Boundaries
- IAM 사용자나 역할이 수행할 수 있는 권한의 최대 상한선을 설정하는 보안 기능
- 사용자(User)와 역할(Role)에 대해서만 지원되며 groups 에는 지원되지 않는다 
- AWS 관리형 정책이나 고객 관리형 정책을 **Boundary** 용도로 지정하여 아무리 강력한 정책을 부여받더라도 이 경계를 벗어날 수 없도록 한다
- AWS Organizations의 SCP 와 조합하여 사용할 수 있다
#### USE CASE
- 일반 사용자에게 IAM 사용자를 직접 생성할 수 있는 권한을 주되 그 사용자가 지정한 Permission Boundary 를 반드시 새로 생성되는 사용자에게 강제 첨부하도록 제한하여 안전하게 권한 관리를 위임한다
- 개발자가 스스로 정책을 할당하고 자신의 권한을 관리할 수 있도록 허용하면서도 권한을 승격할 수 없도록 보장한다
- Organizations 및 SCP 를 사용해 계정 전체를 제한하는 대신 특정 한 명의 사용만 제한할 때 유용하다

<br>

## IAM Evaluation Logic
- 기본적으로 모든 요청은 **묵시적 거부(Implicit Deny)** 상태이고 모든 권한을 가진 AWS 계정 루트 사용자는 예외이다
- IAM User/Role에 연결된 정책(Identity-based)이나 S3 버킷 정책(Resource-based) 등에 `Effect: Allow` 구문이 하나라도 있으면 요청이 허용된다
- 아무리 IAM 정책에서 `Allow`를 해줬더라도 SCP나 Permission Boundary 에서 `Allow` 해두지 않았다면 거부된다
- 얼마나 많은 정책에서 `Effect: Allow`를 주었든 간에, 단 하나의 정책에서라도 `Effect: Deny` 조건이 맞으면 최종 거부된다
- 동일한 AWS 계정 내에서는 IAM 정책이나 리소스 정책 중 한곳에만 Allow 가 있어도 접근이 가능 (합집합)
- 요청 경로에 VPC Endpoint Policy 나 Deny 조건이 포함되면 모든 정책의 조건을 동시에 만족해야 한다 (IAM Policy + VPC Endpoint Policy + Resource Policy)

<br>

### Cross-Account Access Policy Evaluation Logic
- 교차 계정 접근이 성공하려면 계정 A(요청자)와 계정 B(리소스 측) 양 쪽 모두에서 명시적 Allow 가 있어야 한다 
- 계정 A의 요청자에게 자격 증명 기반 정책(Identity-based policy)이 연결되어 있어야 한다
- 해당 자격 증명 기반 정책 내에 계정 B에 있는 특정 리소스를 대상으로 요청을 보낼 수 있도록 Allow 하는 구문이 포함되어 있어야 한다 
- 계정 B의 리소스에 첨부된 리소스 기반 정책에서도 계정 A의 요청자(Principal)에게 접근을 Allow 해야한다

<br>

## IAM Roles vs Resource-Based Policies
### Cross-Account 환경
- Resource-Based : 계정 B의 리소스에 정책을 직접 붙여 계정 A의 보안 주체를 principal로 지정해 직접 허용한다
- IAM Roles : 계정 B에 IAM Role 을 하나 만들어 두고 계정 A의 사용자에게 이 역할을 수임(`sts:AssumeRole`)할 수 있는 권한을 준다 

<br>

### 사용 방식
- IAM Role 수임 시 기존 권한은 포기하고 새로 수임한 권한만 갖게 된다 
- 리소스 기반 정책 사용 시 본인의 원래 권한을 포기할 필요 없이 유지된다 
- 리소스 기반 정책은 일부 AWS 서비스에서만 지원된다 (Amazon S3 버킷, Amazon SNS 토픽, Amazon SQS 큐, AWS KMS 키 등)

<br>

### 비교
#### IAM Roles
- STS 를 통해 임시 자격 증명을 발급받으며 지정된 시간이 지나면 권한이 만료된다 (Temporary)
- 특정 작업 수행을 위해 사용자/AP에 임시 권한을 부여할 때 유용하다 (Principal-centric)
- 타 계정에 가서 다양한 작업을 임시로 처리해야 할 때 활용한다
#### Resource-based Policies
- 리소스 정책을 수정하거나 삭제하지 않는 한 권한이 유지된다 (Permanent)
- 특정 AWS 리소스(S3,SQS 등) 자체에 대한 접근 제어에 중점을 둔다 (Resource-centric)
- 특정 리소스 하나를 타 계정이 계속 읽고 쓸 수 있도록 개방할 때 활용한다

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)