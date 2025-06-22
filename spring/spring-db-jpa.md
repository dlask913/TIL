# (spring) JPA
> JPA 장단점, JPA 설정 및 적용 ( 설정, 적용, 예외 변환 )

<br>

## JPA
> SQL 중심적인 개발에서 객체 중심으로 개발할 수 있도록 도와준다.

- JPA (Java Persistence API) 는 인터페이스의 모음으로, 자바 진영의 ORM 기술 표준이다.
- JPA 2.1 표준 명세를 구현한 Hibernate, EclipseLink, DataNucleus 3가지 구현체가 있다. 
- ORM (Object-relational mapping) 은 객체는 객체대로 설계하고 관계형 데이터베이스는 관계형 데이터베이스대로 설계하고 ORM 프레임워크가 중간에서 매핑한다. 
- 대중적인 언어에는 대부분 ORM 기술이 존재한다.

### JPA 사용 이유
- SQL 중심적인 개발에서 객체 중심으로 개발한다.
- 유지보수 : 필드 변경 시 모든 SQL 을 수정하지 않고 객체에 필드만 추가하면 된다. 
- 객체와 관계형 DB 간 패러다임의 불일치 해결
- 성능 최적화 : 1차 캐시와 동일성 보장, 트랜잭션을 지원하는 쓰기 지연, 지연 로딩

### JPA 동작
> MemberDAO ↔ JPA (JDBC API) ↔ DB
- SQL 생성
- JDBC API 사용
- ResultSet 매핑
- 패러다임 불일치 해결

<br>

## JPA 적용
### 라이브러리 추가
- 아래 의존 관계를 추가하면 `hibernate-core`, `jakarta.persistence-api`, `spring-data-jpa` 와 같은 라이브러리가 추가된다.
```groovy
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
```

#### properties 설정
1. `org.hibernate.SQL`
- 하이버네이트가 생성하고 실행하는 SQL 을 확인할 수 있다. ( logger 를 통해 출력 )
2. `org.hibernate.type.descriptor.sql.BasicBinder`
- SQL 에 바인딩 되는 파라미터를 확인할 수 있다.
3. `spring.jpa.show-sql=true`
- System.out 콘솔을 통해 SQL 이 출력되어 권장하지 않는다.
```properties
logging.level.org.hibernate.SQL=DEBUG  
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

<br>

### JPA 적용
#### 1. ORM 매핑
- `@Entity` : JPA 가 사용하는 객체라는 뜻으로, JPA 가 인식할 수 있도록 한다. 
- `@Id` : 테이블의 PK 와 해당 필드를 매핑한다. 
- `@GeneratedValue(strategy = GenerationType.IDENTITY)`  : PK 생성 값을 데이터베이스에서 생성하는 IDENTITY 방식을 사용한다 ( auto inrement )
- `@Column` : 객체의 필드를 테이블의 컬럼과 매핑한다.
- JPA 는 `public` 또는 `protected` 의 기본 생성자가 필수이다. 
```java
@Data @Entity  
@AllArgsConstructor @NoArgsConstructor  
public class Item {  
  
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)  
    private Long id;  
  
    @Column(name = "item_name", length = 10)  
    private String itemName;  
    private Integer price;  
    private Integer quantity;  
}
```

#### 2. Repository 코드 작성
- JPA 의 모든 동작은 `EntityManager` 를 통해 이루어지고 `EntityManager` 는 내부에 데이터 소스를 가지고 있으며 데이터베이스에 접근할 수 있다. 
- JPA 의 모든 데이터 변경은 트랜잭션 안에서 이루어져야 하기 때문에 `@Transactional` 을 사용한다. 
- JPA 에서는 데이터 변경 시 트랜잭션이 필수이고 일반적으로는 비즈니스 로직을 시작하는 서비스 계층에 트랜잭션을 걸어주는 것이 맞다. 
- `update()` : update 관련 메서드를 실행하지 않았음에도 JPA 는 트랜잭션이 커밋되는 시점에 변경된 엔티티 객체가 있는 지 확인하여 특정 엔티티 객체가 변경된 경우에 UPDATE SQL 을 실행한다. 
- 테스트의 경우 마지막에 트랜잭션이 롤백되기 때문에 UPDATE SQL 을 실행하지 않는다. ( UPDATE SQL 을 확인하려면 `@Commit` 사용 )
```java
@Repository  
@Transactional  
public class JpaItemRepository implements ItemRepository {  
  
    private final EntityManager em;  
  
    public JpaItemRepository(EntityManager em) {  
        this.em = em; 
    }  
  
    @Override  
    public Item save(Item item) {  
        em.persist(item); // 저장
        return item;  
    }  
  
    @Override  
    public void update(Long itemId, ItemUpdateDto updateParam) {  
        Item findItem = em.find(Item.class, itemId);
        findItem.setItemName(updateParam.getItemName()); // 수정
        findItem.setPrice(updateParam.getPrice()); // 수정
        findItem.setQuantity(updateParam.getQuantity()); // 수정
    }  
  
    @Override  
    public Optional<Item> findById(Long id) {  
        Item item = em.find(Item.class, id); // 조회타입과 PK 로 조회
        return Optional.ofNullable(item);  
    }
}
```

<br>

### JPQL ( Java Persistence Query Language )
- JPA 는 JPQL 이라는 객체지향 쿼리 언어를 제공한다. 주로 여러 데이터를 복잡한 조건으로 조회할 때 사용한다. 
- SQL 이 테이블을 대상으로 한다면 JPQL 은 엔티티 객체를 대상으로 SQL 을 실행한다. 
- 엔티티 객체를 대상으로 하기 때문에 `from` 다음에 엔티티 객체 이름이 들어간다. 
- 그럼에도 동적 쿼리가 깔끔하지 못하다는 문제가 있다. 
```java
public List<Item> findAll(ItemSearchCond cond) {
	String jpql = "select i from Item i"; // 엔티티 객체 대상
	Integer maxPrice = cond.getMaxPrice();
	String itemName = cond.getItemName();
	if (StringUtils.hasText(itemName) || maxPrice != null) {             
		jpql += " where";
	}
	boolean andFlag = false;
	if (StringUtils.hasText(itemName)) {
		jpql += " i.itemName like concat('%',:itemName,'%')";             
		andFlag = true;
	}
	if (maxPrice != null) {
		if (andFlag) {
			jpql += " and";
		}
		jpql += " i.price <= :maxPrice";
	}
	TypedQuery<Item> query = em.createQuery(jpql, Item.class); 
	if (StringUtils.hasText(itemName)) {
		query.setParameter("itemName", itemName);         
	}
	if (maxPrice != null) {
		query.setParameter("maxPrice", maxPrice);         
	}
	return query.getResultList();     
}
```

<br>

### JPA 예외 변환
- EntityManager 는 스프링과 관계 없는 순수 JPA 기술로, 예외가 발생하면 JPA 관련 예외를 발생시킨다. ( `PersistenceException` 과 그 하위 예외 )
- `@Repository` 를 통해 JPA 예외를 스프링 예외 추상화로 변환할 수 있다. 
- `@Repository` 가 붙은 클래스는 컴포넌트 스캔의 대상이 되고 **예외 변환 AOP 적용 대상**이 된다.
- 스프링과 JPA 를 함께 사용하는 경우 스프링은 JPA 예외 변환기 ( `PersistenceExceptionTranslator` ) 를 등록하고 예외 변환 AOP 프록시는 JPA 관련 예외가 발생하면 예외 변환기를 통해 스프링 데이터 접근 예외로 변환한다.
- 결과적으로 리포지토리에 **Repository 만 있으면 스프링이 예외 변환을 처리하는 AOP 를 만들어준다.**
![image](https://github.com/user-attachments/assets/77baf5d6-dbca-44eb-922b-8ba5ffca111a)


<br>

## 참고 
[인프런 - 스프링 DB 2편 - 데이터 접근 활용 기술](https://inf.run/NMpER) 