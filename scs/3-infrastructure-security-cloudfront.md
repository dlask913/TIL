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

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)