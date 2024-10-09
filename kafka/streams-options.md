# 카프카 스트림즈DSL 옵션들
> 스트림즈DSL의 필수&선택 옵션들

<br>

## 스트림즈DSL 필수 옵션
> 설정된 default 값이 없는 옵션들을 말한다.

- bootstrap.servers : 프로듀서가 데이터를 전송할 대상 카프카 클러스터에 속한 브로커들의 호스트 이름: 포트를 1개 이상 작성한다. 2개 이상을 입력하여 일부 브로커에 이슈가 발생하더라도 접속에 이슈가 없도록 설정 가능하다.
- application.id : 스트림즈 애플리케이션을 구분하기 위한 고유한 아이디를 설정한다. 다른 로직을 가진 스트림즈 애플리케이션들은 서로 다른 id 값을 가져야 한다.

<br>

## 스트림즈DSL 선택 옵션
> 설정된 default 값이 있는 옵션들을 말한다. 

- default.key.serde : 레코드의 메시지 키를 직렬화, 역직렬화 하는 클래스를 지정한다. 기본값은 `Serdes.ByteArray().getClass().getName()` 이다.
- default.value.serde : 레코드의 메시지 값을 직렬화, 역직렬화 하는 클래스를 지정한다. 기본값은 `Serdes.ByteArray().getClass().getName()` 이다.
- num.stream.threads : 스트림 프로세싱 실행 시 실행될 스레드 개수를 지정한다. 기본값은 1이다. 
- state.dir : 상태기반 데이터 처리를 할 때 데이터를 저장할 디렉토리를 지정한다. 기본값은 `/tmp/kafka-streams` 이다. 

<br>

## 참고
[아파치 카프카 애플리케이션 프로그래밍](https://inf.run/uCwV5)