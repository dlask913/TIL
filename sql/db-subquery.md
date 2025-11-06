## (sql) 서브쿼리
> 하나의 SQL 쿼리 문 안에 포함된 또 다른 Select 쿼리

<br>

## WHERE 절
### 스칼라 서브쿼리 
- 그 결과가 오직 하나의 행으로 나오는 쿼리 
- 결과가 두 개 이상이면 에러가 발생한다 
- ` =`, `>`, `<` , `>=`, `<=`, `<>` 

<br>

### 다중행 서브쿼리
- 그 결과가 여러 행으로 반환되는 것이 당연할 때 사용
- `IN` : 목록에 포함된 값과 일치하는지 확인
```sql
WHERE product_id IN (
	SELECT product_id
	FROM products
	WHERE category = '전자기기'
) 
```
- `ANY` , `ALL` : 목록의 모든/일부 값과 비교
```sql
WHERE price > ANY (100, 200, 300) -- 100 보다 크면 참
WHERE price > ALL (100, 200, 300) -- 300 보다 크면 참
-- *MIN, MAX 를 더 많이 사용
```

<br>

### 다중 컬럼 서브쿼리
- Select 절에 두 개 이상의 컬럼이 포함되는 경우
```sql
WHERE (user_id, status) = ( -- 스칼라 연산('=') 이기 때문에 결과가 하나여야
	SELECT user_id, status
	FROM orders
	WHERE order_is = 3
)
-- 결과가 여러개인 경우 IN 절 활용
```

<br>

### 상관 서브쿼리 
- 서브쿼리가 메인쿼리에서 현재 처리중인 행의 특정 값을 알아야만 계산을 수행할 수 있는 경우
- 서브쿼리가 독립적으로 실행될 수 없고 메인쿼리의 값을 참조하여 실행
- 상관 쿼리는 서브쿼리가 메인쿼리의 행수만큼 반복 실행될 수 있다
- 복잡한 로직을 직관적으로 표현할 수 있지만 메인쿼리가 다루는 데이터 양이 많아지면 쿼리 전체 성능이 급격히 저하될 수 있다.
```sql
WHERE price >= (
	SELECT AVG(price) FROM products p2 WHERE p2.category = p1.category     
)
```
#### 동작 방식
- ↔ 비상관 쿼리는 서브쿼리 실행 후 메인쿼리가 그 결과를 사용한다
1. 메인 쿼리가 먼저 한 행을 읽는다
2. 읽어진 값을 서브쿼리에 전달하여 서브쿼리가 실행
3. 서브쿼리 결과를 이용해 메인쿼리의 WHERE 조건을 판단
4. 메인쿼리의 다른 행을 읽고 2-3번 반복

<br>

### IN 절과 EXISTS 절
#### IN 절
- 서브 쿼리에서 반환하는 목록 전체를 메모리에 저장한 뒤 메인 쿼리의 각 행과 비교한다.
- 서브 쿼리로 조회하는 데이터 양이 클 경우 성능 문제가 발생할 수 있다.
```sql
WHERE product_id IN (SELECT DISTINCT product_id FROM orders);
```

#### EXISTS 절
- 서브 쿼리가 반환하는 결과값에는 관심이 없고 오직 서브 쿼리의 결과로 행이 하나라도 존재하는 지 여부 체크
- 데이터베이스는 조건을 만족하는 첫번째 행을 찾으면 더이상 테이블을 탐색하지 않고 True 를 반환한다
- 관례적으로 SELECT 1 과 같이 상수를 사용하여 불필요한 데이터 조회 회피 
- 특정 조건의 데이터가 존재하지 않는 것을 확인하고 싶을 때는 NOT EXISTS 를 사용한다. 
```sql
WHERE EXISTS ( 
	SELECT 1 FROM orders o WHERE o.product_id = p.product_id 
);
```

<br>

## SELECT 절
### 스칼라 서브쿼리
- SELECT 에서 사용 가능한 서브쿼리는 그 결과수가 단 하나인 스칼라 서브쿼리만 가능하다.
#### 비상관 서브쿼리
```sql
SELECT
    name,
    price,
    (SELECT AVG(price) FROM products) AS avg_price
-- FROM ..
```
#### 상관 서브쿼리 
- 각 행에 따라 계산 결과 값이 다른 경우 사용한다. 
- 가장 큰 단점은 성능 저하
- JOIN 이 너무 복잡하거나 완전히 다른 테이블에서 간단한 정보 하나 만을 조회해올 때 훨씬 간단 명료하다
```sql
SELECT
    p.product_id,
    p.name,
    p.price,
    (SELECT COUNT(*) FROM orders o WHERE o.product_id = p.product_id) AS order_count
-- FROM ..
```

<br>

## FROM 절
### 인라인 뷰 (Inline View)
- FROM 절에 위치한 서브 쿼리는 그 실행 결과가 마치 하나의 독립된 가상 테이블처럼 사용되어 인라인 뷰라고 부른다. 
- 복잡한 데이터를 단계적으로 가공해야 할 때, 특히 집계된 결과를 가지고 다시 한 번 조인이나 필터링을 수행할 때 유용하다.
```sql
-- SELECT .. 
FROM products p
	JOIN (
		SELECT category, MAX(price) AS max_price
		FROM products
		GROUP BY category) AS cmp
	ON p.category = cmp.category AND p.price = cmp.max_price
```

<br>

## 서브쿼리 VS JOIN
### 성능
- JOIN 의 경우, 옵티마이저는 인덱스를 어떻게 활용하고 어떤 테이블을 먼저 읽을 지 등 가장 효율적인 실행 계획을 선택할 수 있는 더 많은 선택지를 제공한다. 
- 일반적으로 JOIN 이 서브쿼리보다 성능이 더 좋거나 동일한 경우가 많다. 
### 가독성
- 서브쿼리는 쿼리의 논리적 단계를 명확하게 구분하여 복잡한 로직을 이해하기 쉽게 만들어주는 경우가 많다. 
- JOIN 은 쿼리에 필요한 모든 소스를 한 눈에 보여주고 여러 테이블의 컬럼을 함께 조회해야 할 때 구조적으로 더 깔끔하다.
### 결론
1. **JOIN** 을 우선적으로 고려한다
2. JOIN 이 복잡하거나 서브쿼리의 가독성이 훨씬 좋은 경우 **서브쿼리**를 사용한다.
3. IN 의 대안으로 **EXISTS** 를 활용한다
4. 성능이 의심될 때는 반드시 측정한다 ( **EXPLAIN** )

<br>

## 참고
[김영한의 실전 데이터베이스 - 기본편](https://inf.run/2aFFu)