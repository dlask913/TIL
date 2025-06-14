# (spring) JdbcTemplate
> JdbcTemplate 장단점, JdbcTemplate 적용 (순서 기반, 이름 기반, SimpleJdbcInsert), 문제점

<br>

## JdbcTemplate
> SQL 을 직접 사용하는 경우 JdbcTemplate 은 JDBC 를 편리하게 사용할 수 있게 도와준다.

### 장단점
- JdbcTemplate 은 spring-jdbc 라이브러리에 포함되어 있고 별도 복잡한 설정 없이 바로 사용할 수 있어 편리하다. 
- 템플릿 콜백 패턴을 사용하기 때문에 JDBC 를 직접 사용할 때 발생하는 대부분의 반복 작업을 대신 처리해준다. ( 커넥션 획득, statement 준비 및 실행, 커넥션&statement&resultset 종료, 트랜잭션을 다루기 위한 커넥션 동기화 등 )
- 동적 SQL 을 해결하기 어렵다는 단점이 있다.

<br>

## JdbcTemplate 적용
### 라이브러리 추가
```gradle
implementation 'org.springframework.boot:spring-boot-starter-jdbc'
```

### JdbcTemplate 기본 사용 예제 ( 순서 기반 )
- `JdbcTemplate` 은 dataSource 가 필요하여 dataSource 를 의존 관계 주입받고 생성자 내부에서 `JdbcTemplate` 을 생성한다. 
- `save()` : 데이터를 저장할 때 PK 는 auto increment 방식을 사용하기 때문에 직접 지정하지 않고 빈 상태로 저장하면 데이터베이스가 생성한다. ( INSERT 이후 확인 가능 )
- `update()` : ? 에 바인딩할 파라미터를 순서대로 전달한다.
- `findById()` : `queryForObject()` 는 결과가 하나일 때 사용한다. 결과가 없으면 `EmptyResultDataAccessException` 예외가 발생하고 결과가 둘 이상이면 `IncorrectResultSizeDataAccessException` 예외가 발생한다.
- `findAll()` : `query()` 는 결과가 하나 이상일 때 사용하고 결과가 없으면 빈 컬렉션을 반환한다.
```java
@Slf4j  
public class JdbcTemplateItemRepository implements ItemRepository {  
  
    private final JdbcTemplate template;  
  
    public JdbcTemplateItemRepository(DataSource dataSource) {  
        this.template = new JdbcTemplate(dataSource); 
    }  
  
    @Override  
    public Item save(Item item) {  
        String sql = "insert into item(item_name, price, quantity) values (?,?,?)";  
        KeyHolder keyHolder = new GeneratedKeyHolder();  
        template.update(connection -> {  
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"}); // 자동 증가 키  
            ps.setString(1, item.getItemName());  
            ps.setInt(2, item.getPrice());  
            ps.setInt(3, item.getQuantity());  
            return ps;  
        }, keyHolder);  
  
        long key = keyHolder.getKey().longValue();
        item.setId(key);  
        return item;  
    }  
  
    @Override  
    public void update(Long itemId, ItemUpdateDto updateParam) {  
        String sql = "update item set item_name=?, price=?, quantity=? where id=?";
        template.update(sql,  
                updateParam.getItemName(),  
                updateParam.getPrice(),  
                updateParam.getQuantity(),  
                itemId); // 영향을 받은 row 수 반환
    }  
  
    @Override  
    public Optional<Item> findById(Long id) {  
        String sql = "select id, item_name, price, quantity from item where id = ?";  
        try {  
            Item item = template.queryForObject(sql, itemRowMapper(), id);  
            return Optional.of(item);  
        } catch (EmptyResultDataAccessException e) { 
	        // 결과가 없는 경우 예외 처리
            return Optional.empty();  
        }  
    }  
  
    @Override  
    public List<Item> findAll(ItemSearchCond cond) {  
        String itemName = cond.getItemName();  
        Integer maxPrice = cond.getMaxPrice();  
  
        /* 동적 쿼리 문제
	        - 요구사항에 따라 고려해야할 상황들이 많다.
	        - 코드가 복잡해진다.
	    */
        String sql = "select id, item_name, price, quantity from item";  
        if (StringUtils.hasText(itemName) || maxPrice != null) {  
            sql += " where";  
        }
  
        boolean andFlag = false;  
        List<Object> param = new ArrayList<>();  
        if (StringUtils.hasText(itemName)) {  
            sql += " item_name like concat('%',?,'%')";  
            param.add(itemName);  
            andFlag = true;  
        }  

		if (maxPrice != null) {  
            if (andFlag) {  
                sql += " and";  
            }  
            sql += " price <= ?";  
            param.add(maxPrice);  
        }  
        log.info("sql={}", sql);  
        return template.query(sql, itemRowMapper(), param.toArray());  
    }  
  
    // 반환 결과인 ResultSet 을 객체로 변환
    private RowMapper<Item> itemRowMapper() {
        return ((rs, rowNum) -> {  
            Item item = new Item();  
            item.setId(rs.getLong("id"));  
            item.setItemName(rs.getString("item_name"));  
            item.setPrice(rs.getInt("price"));  
            item.setQuantity(rs.getInt("quantity"));  
            return item;  
        });  
    }  
}
```

<br>

### JdbcTemplate 이름 지정 파라미터 ( 이름 기반 )
- `JdbcTemplate` 을 기본으로 사용하면 ? 순서대로 바인딩 하는데 개발자의 실수로 파라미터 순서가 바뀌는 경우 데이터가 잘못 들어가는 버그가 생길 위험이 있다.
- 위 문제를 보완하기 위해 `NamedParameterJdbcTemplate` 를 사용하여 이름을 지정해서 파라미터 바인딩을 할 수 있도록 한다.
#### NamedParameterJdbcTemplate 사용 예제 
- 아래와 같이 `?` 대신에 `:파라미터이름` 을 받는 것을 확인할 수 있다.
- 이름 지정 바인딩에서 자주 사용하는 파라미터의 종류는 크게 3가지가 있다. 
  -  Map 을 사용한 키 조회 - `findById()` 에서 사용
  - `SqlParameterSource` 의 구현체인 `MapSqlParameterSource` 을 사용한 SQL 타입 지정 - `update()` 에서 사용
  - `SqlParameterSource` 의 구현체인 `BeanPropertySqlParameterSource` : 자바빈 프로퍼티 규약을 통해서 자동으로 파라미터 객체 생성 ( ex> getItemName -> itemName ), `update()` 처럼 `:id` 를 바인딩 해야 하는데 DTO 에 `itemId` 가 없는 경우에는 사용할 수 없다. - `save()` 에서 사용
- `BeanPropertyRowMapper` 는 ResultSet 의 결과를 받아 자바빈 규약에 맞추어 데이터를 변환한다. 
```java
@Slf4j  
public class JdbcTemplateItemRepository implements ItemRepository {  
  
    private final NamedParameterJdbcTemplate template;  
  
    public JdbcTemplateItemRepository(DataSource dataSource) {
        this.template = new NamedParameterJdbcTemplate(dataSource);  
    }  
  
    @Override  
    public Item save(Item item) {  
        String sql = "insert into item(item_name, price, quantity) " +  
                "values (:itemName, :price, :quantity)";  
  
        SqlParameterSource param = new BeanPropertySqlParameterSource(item);
  
        KeyHolder keyHolder = new GeneratedKeyHolder();  
        template.update(sql, param, keyHolder);  
  
        long key = keyHolder.getKey().longValue();  
        item.setId(key);  
        return item;  
    }  
  
    @Override  
    public void update(Long itemId, ItemUpdateDto updateParam) {  
        String sql = "update item " +  
                "set item_name=:itemName, price=:price, quantity=:quantity " +  
                "where id=:id";  
  
        SqlParameterSource param = new MapSqlParameterSource()  
                .addValue("itemName", updateParam.getItemName())  
                .addValue("price", updateParam.getPrice())  
                .addValue("quantity", updateParam.getQuantity())  
                .addValue("id", itemId);
  
        template.update(sql, param);  
    }  
  
    @Override  
    public Optional<Item> findById(Long id) {  
        String sql = "select id, item_name, price, quantity from item where id = :id";  
        try {  
            Map<String, Object> param = Map.of("id", id); // 키 조회
            Item item = template.queryForObject(sql, param, itemRowMapper());  
            return Optional.of(item);  
        } catch (EmptyResultDataAccessException e) {  
            return Optional.empty();  
        }  
    }  
  
    private RowMapper<Item> itemRowMapper() {  
        return BeanPropertyRowMapper.newInstance(Item.class); //camel 변환 지원  
    }  
}
```

<br>

### JdbcTemplate - SimpleJdbcInsert
- `SimpleJdbcInsert` 는 INSERT SQL 을 직접 작성하지 않을 수 있는 편리한 기능을 제공한다.
- `SimpleJdbcInsert` 는 생성 시점에 데이터베이스 테이블의 메타 데이터를 조회하여 어떤 컬럼이 있는 지 확인 할 수 있기 때문에 `usingColumns` 를 생략할 수 있다. 
- 만약 특정 컬럼만 지정해서 저장하고 싶을 땐 `usingColumns` 을 사용할 수 있다. 
```java
@Slf4j  
public class JdbcTemplateItemRepository implements ItemRepository {  
  
    private final NamedParameterJdbcTemplate template;  
    private final SimpleJdbcInsert jdbcInsert;  
  
    public JdbcTemplateItemRepository(DataSource dataSource) {  
        this.template = new NamedParameterJdbcTemplate(dataSource);  
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)  
                .withTableName("item") // 데이터를 저장할 테이블명 지정
                .usingGeneratedKeyColumns("id");   // PK 컬럼명 지정
//                .usingColumns("item_name", "price", "quantity"); // 생략 가능  
    }
	
	@Override  
	public Item save(Item item) {  
	    SqlParameterSource param = new BeanPropertySqlParameterSource(item);  
	    Number key = jdbcInsert.executeAndReturnKey(param);  
	    item.setId(key.longValue());  
	    return item;  
	}
	..
}
```

<br>

### 문제점
- 동적 쿼리 문제를 해결하지 못하고 SQL 을 자바 코드로 작성하기 때문에 SQL 라인이 코드를 넘어갈 때 마다 문자 더하기를 해주어야 한다는 단점이 있다. 
- 이를 해결하기 위해 MyBatis 를 사용할 수 있다. 

<br>

## 참고 
[인프런 - 스프링 DB 2편 - 데이터 접근 활용 기술](https://inf.run/NMpER) 