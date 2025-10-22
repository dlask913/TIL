## (sql) 내부 조인과 외부 조인
> 내부 조인, 외부 조인과 기타 조인(셀프 조인, 크로스 조인)

<br>

## 내부 조인 ( INNER JOIN )
- 양쪽 테이블에 모두 공통으로 존재하는 데이터만 조회 → 교집합 
- 정의하는 순서 상관없이 그 결과는 항상 동일하나 가독성을 위해 중심이 되는 테이블을 왼쪽에 정의한다. 
- 논리적인 순서 : FROM / JOIN (테이블 결합) → WHERE (조건 필터링) → SELECT (컬럼 선택)
- 사용 방법
```sql
SELECT 컬럼1, 컬럼2, ...
FROM 테이블A
INNER JOIN 테이블B -- INNER 는 생략 가능
ON 테이블A.연결컬럼 = 테이블B.연결컬럼;
```

<br>

## 외부 조인 ( OUTER JOIN )
- 한 쪽 테이블에만 존재하는 데이터를 결과에 포함할 수 있다 
- 보통 분석의 기준이 되는 테이블을 먼저 쓰고 필요한 정보가 있는 테이블들을 하나씩 붙이는 게 더 직관적이어서 LEFT JOIN 이 많이 사용된다. 
- 종류 : LEFT OUTER JOIN, RIGHT OUTER JOIN, FULL OUTER JOIN 
<img width="800" height="200" alt="Image" src="https://github.com/user-attachments/assets/224ef202-1e00-4cbf-a12e-f42d3c9be9a7" />

- 사용 방법
```sql
SELECT 컬럼1, 컬럼2, ...
FROM 테이블A
[LEFT/RIGHT/FULL] JOIN 테이블B -- OUTER 는 생략 가능
ON 테이블A.연결컬럼 = 테이블B.연결컬럼;
```

<br>

### 언제 행이 늘어나고 언제 그대로일까? 
- 행 개수 유지 : 자식에서 부모로 조인할 때 (to-one) → FROM 자식 JOIN 부모
- 행 개수 증가 : 부모에서 자식으로 조인할 때 (to-many) → FROM 부모 JOIN 자식

<br>

## 기타 조인
### 셀프 조인
- Alias 를 활용하여 자기 자신과 조인하는 기법
- 사용 방법
```sql
SELECT
    e.name AS employee_name,
    m.name AS manager_name
FROM
    employees e
JOIN
    employees m ON e.manager_id = m.employee_id;
```

<br>

### 크로스 조인
- 짝이나 관계가 없는 두 테이블로 가능한 모든 조합을 만들어낼 때 사용
- 그 결과를 Cartesion Product 이라 부르고 수가 급격히 늘어날 수 있기 때문에 신중하게 사용한다. 
- A 테이블에 m 개의 행이 있고 B 테이블에 n 개의 행이 있다면, 두 테이블을 CROSS JOIN 한 결과는 m * n 개이다.
- 사용 방법
```sql
SELECT 컬럼1, 컬럼2, ...
FROM 테이블A
CROSS JOIN 테이블B -- OUTER 는 생략 가능
ON 테이블A.연결컬럼 = 테이블B.연결컬럼;
```

<br>

### INSERT INTO ... SELECT
- 데이터를 한 번에 대량으로 삽입할 때 유용하다. 
- 사용 방법
```sql
INSERT INTO product_options(product_name, size, color) 
SELECT
    CONCAT('기본티셔츠-', c.color, '-', s.size) AS product_name, 
    s.size, c.color
FROM
    sizes AS s CROSS JOIN
    colors AS c;
```

<br>

## Alias 
- 필드 혹은 테이블 뒤에 AS 를 사용하거나 생략하고 한 칸 띄어서 별칭을 붙일 수 있다. 
- 보통 필드는 AS 를 사용하고 테이블은 AS 를 생략하고 별칭을 붙인다.

<br>

## 참고
[김영한의 실전 데이터베이스 - 기본편](https://inf.run/2aFFu)