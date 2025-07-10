# (spring) 스프링 트랜잭션 전파
> 스프링 트랜잭션 전파 REQUIRED 옵션

<br>

## 스프링 트랜잭션 전파
: 트랜잭션을 각각 사용하는 것이 아니라 이미 트랜잭션이 진행중일 때 추가로 트랜잭션을 수행하면 어떻게 동작할 지 결정하는 것을 트랜잭션 전파(propagation) 라 한다. <br>

#### 트랜잭션 관련 로그 설정 
```properties
logging.level.org.springframework.transaction.interceptor=TRACE
logging.level.org.springframework.jdbc.datasource.DataSourceTransactionManager=DEBUG
# JPA log
logging.level.org.springframework.orm.jpa.JpaTransactionManager=DEBUG logging.level.org.hibernate.resource.transaction=DEBUG
# JPA SQL
logging.level.org.hibernate.SQL=DEBUG
```

<br>

### Case1. 트랜잭션 각각 2번 사용

![image](https://github.com/user-attachments/assets/72314b13-51dd-4ed4-826e-f9704969b927)

- 트랜잭션이 각각 수행되며 사용하는 DB 커넥션도 다르다.
- 별도 커넥션을 사용하기 때문에 어느 트랜잭션이 롤백하더라도 다른 트랜잭션에 영향을 주지 않는다. 

<br>

### Case2. 트랜잭션이 이미 진행중인데 추가 트랜잭션 수행 ( REQUIRED 옵션 )

![image](https://github.com/user-attachments/assets/6d97735b-51e1-4817-b750-4af4a97d8d56)

- 스프링에서 외부 트랜잭션과 내부 트랜잭션을 묶어서 하나의 트랜잭션을 만들어 준다. 
- 내부 트랜잭션이 외부 트랜잭션에 참여한다. ( 기존 트랜잭션을 이어받아 수행 )
- 논리 트랜잭션들을 하나의 물리 트랜잭션으로 묶어, 처음 트랜잭션을 싲가한 외부 트랜잭션이 실제 물리 트랜잭션을 관리하도록 한다. → 트랜잭션 중복 문제 해결
- 내부 트랜잭션을 시작해서 코드 상 커밋하더라도 `Particiating in existing transaction` 로그와 함께 실제 DB 커넥션 로그에 커밋하지 않는다. 
- 논리 트랜잭션 : 트랜잭션 매니저를 통해 사용하는 단위
- 물리 트랜잭션 : 실제 커넥션을 통해 커밋, 롤백하는 단위

#### 원칙
- 모든 논리 트랜잭션이 커밋되어야 물리 트랜잭션이 커밋된다.
- 하나의 논리 트랜잭션이라도 롤백되면 물리 트랜잭션은 롤백된다. 

<br>

### Case3. 내부 트랜잭션 커밋, 외부 트랜잭션 롤백되는 경우 ( REQUIRED 옵션 )
- 논리 트랜잭션이 하나라도 롤백되면 전체 물리 트랜잭션은 롤백된다. ( 데이터도 전체 롤백 )
- 내부 트랜잭션이 롤백하더라도 외부 트랜잭션이 물리 트랜잭션을 관리하기 때문에 커밋을 할 것 같지만, 스프링은 이 경우 `UnexpectedRollbackException` 예외를 던진다.
#### 동작 방식

![image](https://github.com/user-attachments/assets/18d4f81d-a634-4363-b87c-04084c790ae9)


- 내부 트랜잭션이 롤백하면 트랜잭션 매니저에 롤백 요청을 하여 커넥션에 **rollbackOnly=true** 를 설정한다. 
- 외부 트랜잭션 코드에서 트랜잭션 매니저에 커밋 요청을 하면 커넥션의 rollbackOnly 설정을 확인하고 `UndexpectedRollbackException` 이 발생한다.

#### 정리
- 논리 트랜잭션이 하나라도 롤백되면 물리 트랜잭션은 롤백된다.
- 내부 논리 트랜잭션이 롤백되면 롤백 전용 마크를 표시한다.
- 외부 트랜잭션을 커밋할 때 롤백 전용 마크를 확인한 후, 표시되어 있으면 물리 트랜잭션을 롤백하고 예외를 던진다. 

<br>

## 참고 
[인프런 - 스프링 DB 2편 - 데이터 접근 활용 기술](https://inf.run/NMpER) 