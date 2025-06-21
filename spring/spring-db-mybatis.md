# (spring) MyBatis
> MyBatis 장단점, MyBatis 설정 및 적용, MyBatis 기능 정리

<br>

## MyBatis
> SQL 을 MXL 에 편리하게 작성할 수 있고 동적 쿼리를 매우 편리하게 작성할 수 있다.

### 장단점
- JdbcTemplate 은 스프링에 내장된 기능이라 별도 설정없이 사용할 수 있었지만 MyBatis 는 추가 설정이 필요하다. 
- 라인이 길어져도 문자 더하기에 대한 불편함이 없어, 동적 쿼리르 매우 편리하게 작성할 수 있다. 
- 동적 쿼리와 복잡한 쿼리가 많다면 MyBatis 를 사용하고 단순한 쿼리들이 많으면 JdbcTemplate 을 선택해서 사용할 수 있다. 

<br>

## MyBatis 설정
#### 라이브러리 추가
- MyBatis 를 사용하기 위해 build.gradle 에 아래 의존 관계를 추가한다. 
```groovy
// spring boot 3.0 이상부터 3.0.3 사용
implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3'
```
#### properties 설정
1. `mybatis.type-aliases-package` 
- MyBatis 에서 타입 정보를 사용할 때 패키지 이름을 적어야 하는데, 여기에 명시하면 패키지 이름을 생략할 수 있다. 
- 지정한 패키지와 그 하위 패키지가 자동으로 인식된다. 
- 여러 위치를 지정하려면 `,` 나 `;` 로 구분한다. 
2. `mybatis.configuration.map-underscore-to-camel-case` 
- 언더바를 카멜로 자동 변경해주는 기능을 활성화 한다. 
3. `logging.level.hello.itemservice.repository.mybatis`
- MyBatis 에서 실행되는 쿼리 로그를 확인할 수 있다. 
```properties
# application.properites
mybatis.type-aliases-package={패키지명}
mybatis.configuration.map-underscore-to-camel-case=true  
logging.level.hello.itemservice.repository.mybatis=trace
```

<br>

## MyBatis 적용
#### 1. Mapper 인터페이스 생성
- MyBatis 매핑 XML 을 호출해주는 Mapper 인터페이스를 생성하여 MyBatis 가 인식할 수 있게 @Mapper 를 붙여준다.
- 이 인터페이스의 메서드를 호출하면 매핑되는 xml 의 해당 SQL 을 실행하고 결과를 돌려준다. 
```java
@Mapper  
public interface ItemMapper {  
    void save(Item item);  
    void update(@Param("id") Long id, @Param("updateParam") ItemUpdateDto updateParam);
    Optional<Item> findById(Long id);  
    List<Item> findAll(ItemSearchCond itemSearch);  
}
```

#### 2. src/main/resources 하위에 XML 매핑 파일 생성
- XML 파일을 원하는 위치에 두고 싶으면 application.properties 에 아래 설정을 추가한다. 
```properties
mybatis.mapper-locations=classpath:mapper/**/*.xml
```
- 기본적으로는 패키지 위치를 맞추어 주어야 하고, namespace 에는 앞서 만든 매퍼 인터페이스를 지정한다. ( src/main/resources/hello/itemservice/repository/mybatis/ItemMapper.xml )
```xml
<?xml version="1.0" encoding="UTF-8"?>  
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">  
<mapper namespace="hello.itemservice.repository.mybatis.ItemMapper">  
    <insert id="save" useGeneratedKeys="true" keyProperty="id">  
        insert into item (item_name, price, quantity)  
        values (#{itemName}, #{price}, #{quantity})  
    </insert>  
  
    <update id="update">  
        update item  
        set item_name=#{updateParam.itemName},  
            price=#{updateParam.price},  
            quantity=#{updateParam.quantity}  
        where id = #{id}  
    </update>  
  
    <select id="findById" resultType="Item">  
        select id, item_name, price, quantity  
        from item  
        where id = #{id}  
    </select>  
  
    <select id="findAll" resultType="Item">  
        select id, item_name, price, quantity  
        from item  
        <where>  
            <if test="itemName != null and itemName != ''">  
                and item_name like concat('%', #{itemName}, '%')  
            </if>  
            <if test="maxPrice != null">  
                and price &lt;= #{maxPrice}  
            </if>  
        </where>    
	</select>
</mapper>
```

#### XML 특수문자
- XML 에서는 데이터 영역에 <, > 같은 특수 문자를 사용할 수 없기 때문에 아래와 같이 치환하여 사용하거나 CDATA 구문 문법을 적용할 수 있다. 
```console
< : &lt; 
> : &gt;
> & : &amp;
```
- `&lt;` 적용 예시 
```xml
<if test="maxPrice != null">  
	and price &lt;= #{maxPrice}  
</if>  
```
- `CDATA` 구문 적용 예시 
```xml
<if test="maxPrice != null"> 
	<![CDATA[ and price <= #{maxPrice} ]]>
</if>
```

#### MyBatis 분석
1. 애플리케이션 로딩 시점에 MyBatis 스프링 연동 모듈은 `@Mapper` 가 붙어있는 인터페이스를 조사한다. 
2. 해당 인터페이스가 발견되면 동적 프록시 기술을 사용해서 `ItemMapper` 인터페이스의 구현체를 만든다. 
3. 생성된 구현체를 스프링 빈으로 등록한다. 

<br>

## MyBatis 기능 정리 
### 동적 SQL
- `if` : 해당 조건에 따라 값을 추가할지 말지 판단한다.
```xml
WHERE state = ‘ACTIVE’   
	<if test="title != null">     
	AND title like #{title}   
	</if>
```
- `choose`, `when`, `otherwise`
```xml
WHERE state = ‘ACTIVE’
<choose>
	<when test="title != null">
	  AND title like #{title}
	</when>
	<when test="author != null and author.name != null">       
	AND author_name like #{author.name}
	</when>
	<otherwise>
	  AND featured = 1     
	</otherwise>
</choose>
```
- `where` : 상황에 따라 동적으로 달라져 발생하는 문제를 해결 할 수 있다.  문장이 없으면 where 를 추가하지 않고 문장이 있을 때만 추가한다.
```xml
<!-- 문제 상황
SELECT * FROM BLOG
WHERE
AND title like someTitle
-->
SELECT * FROM BLOG
<where>
    <if test="state != null">
         state = #{state}
    </if>
    <if test="title != null">
        AND title like #{title}
    </if>
</where>
```
- `foreach` : 컬렉션을 반복 처리할 때 사용한다. 파라미터로 `List` 를 전달한다.
```xml
<where>
    <foreach item="item" index="index" collection="list" open="ID in (" separator="," close=")" nullable="true">
	#{item}
    </foreach>   
</where>
```

<br>

### 애노테이션 SQL 작성
- XML 대신 애노테이션에 SQL 을 작성할 수 있다. 
- `@Insert`, `@Update`, `@Delete`, `@Select` 기능이 제공되고 동적 SQL 이 해결되지 않으므로 간단한 경우에만 사용한다. 
```java
@Select("select id, item_name, price, quantity from item where id=#{id}") Optional<Item> findById(Long id);
```

<br>

### 문자열 대체 
- 파라미터 바인딩이 아니라 문자 그대로를 처리하고 싶은 경우 ${} 를 사용할 수 있다. 
- ${) 를 사용하면 SQL 인젝션 공격을 당할 수 있어 가급적 사용하지 않는다. 
```java
@Select("select * from user where ${column} = #{value}")
```

<br>

### Result Maps
- 결과를 매핑할 때 컬럼명과 객체의 프로퍼티 명이 다른 경우 별칭(`as`) 를 사용하거나 아래와 같이 `resultMap` 을 선언해서 사용할 수 있다. 
```xml
<resultMap id="userResultMap" type="User">
	<id property="id" column="user_id" />
	<result property="username" column="user_name"/>
	<result property="password" column="hashed_password"/> 
</resultMap>
<select id="selectUsers" resultMap="userResultMap">   
	select user_id, user_name, hashed_password
	from some_table   
	where id = #{id} 
</select>
```

<br>

## 참고 
[인프런 - 스프링 DB 2편 - 데이터 접근 활용 기술](https://inf.run/NMpER) 