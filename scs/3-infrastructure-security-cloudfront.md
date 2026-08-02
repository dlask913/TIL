## (aws) Infrastructure Security - CloudFront
> CloudFront, CloudFront vs S3 Cross Region Replication

<br>

## CloudFront
- 데이터, 동영상, 애플리케이션 및 API 를 전 세계 고객에게 안전하게, Low Latency, 고속으로 전송하는 글로벌 콘텐츠 전송 네트워크(CDN) 서비스
- 사용자가 콘텐츠를 요청할 때 CloudFront 는 오리진 서버로 매번 보내지 않고 사용자에게 물리적으로 가까운 Edge Location 에서 캐시된 복사본 반환
- 서비스 로딩 기간을 단축시켜 UX 향상
- 전 세계 수백 개 이상의 PoP(Points of Presence) 인프라망을 기반으로 작동하며 edge locations, regional edge caches 로 구별됨
- AWS Shield Standard 가 기본 제공되며 WAF 와 통합 가능 -> DDoS protection

<br>

### Origins
#### S3
- S3 에 저장된 정적 파일을 대상으로 함
- 다운로드 뿐 아니라 클라이언트가 Cloudfront 로 파일을 업로드(PUT/POST)하면 S3 로 전달해 저장할 수 있다
- 사용자가 S3 URL 로 직접 접근하는 것을 막고 CloudFront 를 통해서만 S3 에 접근할 수 있도록 제한 → **OAC** (Origin Access Control)
#### VPC Origin
- AWS 가 제공하는 퍼블릭 인터넷을 거치지 않는 private 백엔드 연동 방식
- VPC 내 private subnet 에 위치한 리소스를 대상으로 함
- public ip 가 없는 Private ALB, Private NLB, EC2 인스턴스에 CloudFront 가 직접 트래픽을 전달
#### Custom Origin (HTTP)
- S3 일반 REST 엔드포인트가 아닌 HTTP/HTTPS 스펙을 따르는 모든 웹서버 및 엔드포인트 대상
- S3 버킷에서 '정적 웹 사이트 호스팅' 기능 활성화하면 생성되는 URL 을 오리진으로 사용하는 경우 (OAC/OCI 설정 불가)
- AWS 내부에 있는 Public ALB, Public EC2, API Gateway 는 물론 AWS 외부 타사 온프레미스 웹서버까지 모두 연결 가능
- 동적 애플리케이션 트래픽을 라우팅할 때 주로 사용

<br>

### 동작 방식
> 사용자가 특정 파일을 요청했을 때 

![image](https://docs.aws.amazon.com/ko_kr/AmazonCloudFront/latest/DeveloperGuide/images/how-cloudfront-delivers-content.png)

1. Client → CloudFront Edge Location (요청 전달)
2. Local Cache 확인 (캐시 미스 발생)
3. Forward Request to your Origin (오리진 요청)
4. 캐싱 및 응답

<br>

### Geo Restriction
- 특정 국가 사용자의 접근을 허용하거나 차단하는 Access Control 기능
- Allowlist (허용 목록) : 지정한 국가 목록에 속한 IP의 접근만 승인하고, 목록에 없는 다른 모든 국가의 접근은 기본적으로 차단
- Blocklist (차단 목록) : 지정한 국가 목록에 속한 IP의 접근을 차단하고, 목록에 없는 다른 모든 국가의 접근은 허용
- 배포당 Allowlist 또는 Blocklist 중 하나만 선택하여 활성화할 수 있다
- CloudFront는 클라이언트 요청의 IP 주소를 제3자 지리 위치 데이터베이스(Geo-IP Database)와 매핑하여 요청이 어느 국가에서 들어왔는지 판별한다
- Use case: Copyright Laws to control access to content

<br>


### Field-Level Encryption, FLE
```
[ Client (Browser) ]
       │  (1) POST 요청 (신용카드번호 평문 전송 - HTTPS)
       ▼
[ CloudFront Edge Location ] ──► (2) Edge에서 Public Key로 카드번호 필드만 암호화
       │  (HTTPS + 필드 자체 암호화 상태)
       ▼
[ Nginx / Web Server / App ] ──► (3) 백엔드 내부망 통과 (카드번호 암호화 상태)
       │
       ▼
[ Payment Processing Server ] ─► (4) Private Key로 카드번호 복호화 및 처리
```
- HTTPS(TLS) 를 사용하면 클라이언트와 CloudFront 사이 네트워크 구간은 암호화되지만 Edge 를 통과해 내부 웹 서버나 백엔드 내부에서는 데이터가 평문으로 노출될 수 있다 
- FLE 를 적용하면 백엔드 내부에서도 특정 필드가 암호화된 상태를 유지한다 
- 사용자가 form 을 제출하면 요청이 Edge Location 에 도착하는 즉시 지정된 민감 필드만 암호화된다 
- 비대칭 암호화 사용 (RSA)
- HTTP POST 요청의 Body 에 포함된 필드 중 최대 10개의 특정 필드를 지정하여 암호화할 수 있다 
- CloudFront 콘솔이나 API 에 비대칭 키의 Public Key 를 업로드하고 어떤 필드 패턴에 이 공개키를 적용할지 매핑해둔다

<br>

### Authorization Header
- 클라이언트가 API 요청을 보낼 때 `Authorization: Bearer <JWT_TOKEN>` 헤더가 포함된 경우 CloudFront 는 default 로 헤더를 제거하고 origin 으로 전달
- CloudFront Cache Policy 또는 요청 정책 설정에 **Authorization 헤더를 Forward** 하도록 허용해주어야만 한다
- S3 가 origin 인 경우, S3 는 Authorization 헤더를 S3 자체의 REST API 인증(AWS SigV4 서명) 용도로 사용하기 때문에 Authorization 헤더 전달이 아닌 OAC, Signed URL / Cookies 를 통해야 한다 

<br>

### Restrict to ALB
- CloudFront 을 우회(Bypass)해서 ALB 로 직접 요청을 쏘게 되면 CloudFront 의 WAF 및 보안 정책이 무용지물이 되기 때문에 반드시 모든 클라이언트 트래픽이 Edge Location 을 거쳐서만 ALB 로 오도록 강제로 제한해야 한다 
#### Custom HTTP Header 방식
1. CloudFront 설정 : CloudFront 가 ALB 로 요청을 전달할 때 커스텀 헤더를 자동으로 붙여 전송하도록 설정
2. ALB Listener Rule 설정 : ALB Listener Rule 에서 HTTP 헤더 조건 추가
3. Secret Value : 이 커스텀 헤더의 이름과 Secret Token 은 외부 유저에게 노출되선 안되며 정기적으로 교체하는 것이 좋다 
```
[ Client ]
   │
   ├─► (1) 정상 요청 (보안 헤더 없음)
   │    │
   │    ▼
   │  [ CloudFront Edge ]
   │    │
   │    │ (2) 비밀 Custom Header 자동 추가
   │    │     Header: X-Custom-Header
   │    │     Value : Secret123
   │    ▼
   ├─► [ ALB (Load Balancer) ]
   │    │
   │    ├─► (4) Header = Secret123 일치
   │    │    │
   │    │    ▼
   │    │  [ EC2 / Target Group ] (200 OK)
   │    │
   │    └─► ❌ Header 불일치 / 없음
   │         │
   │         ▼
   │       [ 403 Forbidden ] (즉시 차단)
   │
   └─► (3) 우회 요청 (ALB IP/DNS로 직접 요청 - 헤더 없음)
        │
        ▼
      [ ALB (Load Balancer) ]
        │
        ▼
      [ 403 Forbidden ] (즉시 차단)
```

<br>

## CloudFront vs S3 Cross Region Replication
### CloudFront
- 사용자에게 가까운 Edge Location 에서 데이터를 캐싱하여 전송 속도(읽기 성능)를 극대화하는 CDN 서비스
- 전 세계 400개 이상의 PoP(Points of Presence) Edge Location 사용
- S3 에서 원본 파일을 가져온 뒤 설정된 TTL 동안 Edge 에 보관 (기본 24시간)
- 전 세계 불특정 다수 사용자가 흩어져 있는 웹/앱 서비스에 최적화
- 특히 이미지, CSS, JS, 동영상 스트리밍 등 자주 업데이트되지 않고 전 세계 어디서나 빠른 읽기가 필요한 정적 파일 전송에 유리

<br>

### S3 CRR, 리전 간 비동기 복제
- 캐싱 서비스가 아닌 한 AWS 리전의 S3 버킷에 들어오는 객체를 다른 리전의 S3 버킷으로 자동/비동기 복제하는 데이터 관리 기능
- 복제하려는 Source Region 과 Destination Region 을 1:1 Rule 로 직접 지정해주어야 한다
- 비동기 방식으로 원본 버킷에 파일이 들어오면 몇 초~ 몇 분 내 동일하게 복제된다 
- Destionation Region 버킷은 보통 읽기전용으로 취급되며 1way 단방향 복제가 기본
- 특정 몇몇 리전에서 자주 변경되는 데이터를 원본과 동일하게 낮은 지연시간으로 동기화해야 할 때 사용

<br>

## CloudFront Signed URL / Signed Cookies
- 유료 사용자들에게 유료 콘텐츠만 배포하고 싶은 비즈니스 요구사항이 존재할 경우 사용자의 접근을 제한하기 위해 CloudFront Signed URL / Cookie 를 생성할 수 있다 
- 기본적으로 CloudFront URL 은 public 상태이며, 배포 설정에서 Restrict Viewer Access 옵션을 Yes 로 한다 
- 서명을 생성할 때 백엔드 코드에서 Epock timestamp 형태로 만료 시간을 반드시 명시해야 한다
- Shared content (movie, music) 은 무단 공유될 위험이 높아 유효 기간을 수 분정도로 짧게 한다
- Private conent (private to the user) 와 같은 개인 전용 콘텐츠(프로필 이미지, 문서등)은 유효 기간을 수 년정도로 길 게 해도 된다
- Signed URL = 단일 파일에 대한 접근 권한 (파일 1개당 1개의 서명된 URL 필요)
- Signed Cookies = 여러 파일에 대한 접근 권한 (하나의 서명된 쿠키로 수많은 파일 접근 가능)
#### LifeCycle
```
1. Client → Application : 클라이언트가 백엔드 서버에 로그인하고 유료 컨텐츠 접근을 요청
2. Application → Edge Location / AWS SDK : APP 은 AWS SDK 를 이용해 비대칭 키로 서명된 CloudFront Signed URL 생성 및 반환
3. Client → CloudFront Edge Location : 클라이언트는 전달받은 Signed URL 로 CloudFront 에 파일 요청
4. CloudFront → Amazon S3 : Edge 에서 서명을 검증한 뒤 유효한 경우 OAC 보안 통로를 통해 S3 버킷에서 안전하게 파일을 가져와 응답
```

<br>

### CloudFront Signed URL vs S3 Pre-Signed URL
#### CloudFront Signed URL
- Origin 이 S3든 EC2, ALB 든 상관없이 특정 경로에 대한 접근 허용 
- CloudFront Key Group 을 통해 관리
- IP, 경로, 날짜, 만료시간 등 정교한 필터링 가능
- Edge Caching 성능 이점을 그대로 활용 가능
#### S3 Pre-signed URL
- Pre-Signed URL 을 전달받은 클라이언트는 해당 URL 을 생성해 준 IAM User 또는 Role 이 가진 S3 접근 권한을 그대로 상속받아 동작한다 
- 서명 주체의 IAM 키 사용
- 유효시간이 제한된다 
- 업로드에 적합하다

<br>

### Signed URL Process
```
[ 백엔드 애플리케이션 (EC2, Lambda, Spring 등) ]
       │
       ├─ 비대칭 키 생성 (RSA 2048-bit)
       │    ├─ Private Key (비밀키) ──► 백엔드 서버가 보유 (서명 생성용)
       │    └─ Public Key  (공개키) ──► CloudFront에 업로드 (Trusted Key Group 등록)
       │
       ▼
[ 클라이언트 요청 시 ]
1. 백엔드가 Private Key로 Signed URL 생성해서 클라이언트에 전달
2. 클라이언트가 CloudFront Edge Location으로 Signed URL 요청
3. CloudFront가 등록된 Public Key로 서명 변조 여부 검증 (Verify)
```
#### Trusted key group (recommendation)
- public key 들을 묶어서 관리하는 CloudFront 키 그룹 기능
- Root 계정 없이 IAM 권한을 받은 관리자가 AWS CLI 나 SDK(API)를 통해 키를 업로드하고 교체할 수 있다
- 보안을 위해 주기적으로 키를 교체해야 할 때, API 를 통해 손쉽게 키 순환 자동화 로직을 구축할 수 있다 
#### CloudFront Key Pair
- AWS 계정 자체에 등록하는 구형 Key Pair 방식
- 반드시 AWS Root 계정으로 로그인해서 콘솔을 통해 직접 관리
- AWS Best Practice 에서는 일상적인 작업에 Root 계정을 사용하는 것을 강력히 금지하므로 현재는 권장하지 않음

<br>

## CloudFront OAC vs OAI
### OAC + SSE-KMS (recommendation)
- OAC 는 CloudFront 가 S3 origin 으로 요청을 보낼 때 SigV4 로 서명하여 전송
- AWS SigV4 서명이 적용되어 있으면 OAC 는 별도의 변수 처리나 우회 방식 없이 SSE-KMS 로 암호화된 S3 객체를 기본적으로 (natively) 가져올 수 있다
- S3 객체를 복호화할 때 쓰이는 AWS KMS Key Policy 에도 CloudFront OAC 서비스 보안 주체를 승인하는 구문 추가 필요 (`kms:Decrypt` 권한을 `cloudfront.amazonaws.com`에 허용)
### OAI + SSE-KMS (legacy)
- OAI 는 SigV4 서명을 지원하지 않기 때문에 SSE-S3 만 지원하며, SSE-KMS 로 암호화된 객체는 복호화하지 못하고 403 에러를 뱉어낸다 
- SSE-KMS 를 읽기 위해서는 CloudFront Origin Request 단계에 Lambda@Edge 를 붙여서 S3 로 향하는 요청에 SigV4 서명을 강제로 넣어준다 
- 위 작업을 위해서는 OAI 를 비활성화해야 한다 

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)