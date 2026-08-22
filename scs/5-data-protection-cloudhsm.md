## (aws) Data Protection - CloudHSM
> Encryption, CloudHSM, KMS vs. CloudHSM, Integration with AWS and 3rd Party Services, Sharing Cluster Across-Accounts

<br>

## Encryption
### Server-side Encryption
- 데이터가 서버에 수신된 후 암호화되며 클라이언트로 전송되기 직전에 서버에서 복호화된다 
- 저장소(S3, EBS 등)에는 Data Key 를 통해 암호화된 상태로 저장된다 
- 암/복호화 키는 AWS KMS 등 어딘가에서 관리되어야 하며, 서버가 해당 키에 접근할 수 있는 권한을 갖고 있어야 한다 
### Client-side Encryption
- 데이터가 서버로 보내지기 전 클라이언트 앱에서 미리 암호화되며, 서버는 이 데이터를 절대 복호화할 수 없다
- 데이터를 수신하는 최종 클라이언트 측에서 복호화를 수행한다 
- 성능 및 키 관리를 위해 Envelope Encryption 방식을 활용할 수 있다

<br>

## CloudHSM
- AWS 가 하드웨어를 제공하지만 키 관리 및 제어 권한은 100% 고객이 갖는 전용 하드웨어 보안 모듈(Hardware Security Module) 서비스 
- **전용 암호화 하드웨어 장비를 제공**하는 단독 점유(Dedicated) 방식
- 물리적/논리적 침입 시도 시 데이터가 즉시 파기되는 변조 방지(Tamper resistant) 하드웨어이며, 높은 수준의 보안 표준(FIPS 140-2 Level 3)을 준수한다
- AWS 도 키에 접근할 수 없고 오직 고객만이 완전한 키 제어권을 갖는다 
- 대칭/비대칭 암호화 지원 : 일반 데이터 암호화(대칭키)와 SSL/TLS 인증서 키(비대칭기) 처리를 모두 지원한다
- Amazon Redshift 의 데이터베이스 암호화 키 관리나 고객 제공 키를 사용하는 서버 측 암호화(SSE-C) 옵션과 조합하여 활용하기 좋다 
- CloudHSM 클러스터는 다중 가용 영역(Multi-AZ)에 걸쳐 분산 배치되어 가용성과 내구성이 뛰어나다 

#### 권한 분리 (IAM vs. CloudHSM Software)
- IAM 은 오직 HSM 클러스터 하드웨어를 CRUD 하는 인프라 관리 권한만 제어하고 IAM 권한으로는 암호화 키를 볼 수 없다 
- 실제 암호화 키를 생성/사용하고 HSM 장비 내부의 암호화 작업 사용자를 관리하는 것은 오직 전용 CloudHSM Client SW를 통해서만 가능하다

<br>

### KMS vs. CloudHSM 
#### KMS
- 테넌시: 여러 고객이 인프라를 공유하는 멀티테넌트(Multi-Tenant) 방식
- 보안 표준: FIPS 140-2 Level 3 규격을 준수
- 마스터 키: AWS Owned Keys, AWS Managed Keys, Customer Managed Keys 지원
- 지원 기능: 대칭키, 비대칭키, 전자서명(Digital Signing) 지원
- 접근성: KMS Key Replication을 통해 다중 리전 간 접근 및 복제가 가능하다
- 암호화 가속: 하드웨어 가속 기능을 제공하지 않는다
- 인증 및 권한: AWS IAM 정책을 통해 권한을 제어한다
- 고가용성: AWS가 인프라 차원에서 고가용성을 완전 관리
- 감사 및 로깅: CloudTrail 및 CloudWatch와 통합되어 동작
#### CloudHSM
- 테넌시: 단일 고객 전용 단독 점유(Single-Tenant) 방식
- 보안 표준: FIPS 140-2 Level 3 규격을 준수
- 마스터 키: 고객이 직접 생성 및 관리하는 마스터 키(Customer Managed CMK)만 사용 가능
- 지원 기능: 대칭키, 비대칭키, 전자서명 외에 해싱(Hashing) 추가 지원
- 접근성: 특정 VPC 내에 배포되며, 다른 VPC에서 접근하려면 VPC 피어링이 필요하다
- 암호화 가속: SSL/TLS 가속 및 Oracle TDE 가속 기능을 제공한다
- 인증 및 권한: IAM과 별개로 CloudHSM 내부에서 사용자를 직접 생성하고 권한을 관리한다
- 고가용성: 사용자가 직접 여러 가용 영역(Multi-AZ)에 HSM을 추가 배치해야 고가용성이 확보된다
- 감사 및 로깅: CloudTrail, CloudWatch 통합 및 MFA 지원

<br>

### Integration with AWS and 3rd Party Services
#### with AWS
- AWS KMS 의 Custom Key Store 기능으로 연결(Connector)하여 사용할 수 있다 
- KMS 의 편리한 사용성을 유지하며 실제 키 생성 및 저장만 CloudHSM에서 처리하도록 구성한다
- EBS, S3, RDS 등 다양한 AWS 리소스가 KMS Custom Key Store를 통해 CloudHSM의 암호화 키를 활용한다
- RDS Oracle의 TDE(투명적 데이터 암호화) 기능도 KMS 연동을 통해 지원한다 
#### with 3rd Party Services
- 외부 소프트웨어나 온프레미스 앱의 암호화 키를 CloudHSM 내부에서 직접 생성하고 안전하게 저장한다
- SSL/TLS Offload: 웹 서버의 SSL/TLS 암/복호화 부하를 CloudHSM 하드웨어로 분산 처리
- Windows Server Certificate Authority (CA): 인증서 발급 기관의 루트 키/서명 키 보호
- 기타 소프트웨어 키 관리: Oracle TDE, Microsoft SignTool, Java Keytool 등 다양한 개발/운영 도구와 연동

<br>

### Sharing Cluster Across-Accounts
- CloudHSM 클러스터를 여러 AWS 계정 간 공유할 때, 장비 자체를 직접 공유하는 대신 AWS RAM(Resource Access Manager)을 활용해 서브넷을 공유할 수 있다
- CloudHSM 클러스터가 위치한 Private Subnet 을 AWS RAM 으로 다른 계정에 공유한다
- 서브넷 공유 대상은 AWS Organization 전체, 특정 조직 단위(OU), 또는 개별 AWS 계정 단위로 지정할 수 있다
- 리소스/클러스터 자체를 직접 타 계정으로 공유할 수는 없다 (반드시 서브넷 공유 방식을 거쳐야 함)
- 서브넷을 공유받은 타 계정의 클라이언트(EC2 등)가 CloudHSM에 정상 접근할 수 있도록 CloudHSM의 Security Group에서 해당 클라이언트 대역의 트래픽을 허용하도록 설정해야 한다 

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)