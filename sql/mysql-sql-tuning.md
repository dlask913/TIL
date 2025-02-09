## (sql) SQL문 튜닝
> WHERE 문이 사용된 SQL문 튜닝, 인덱스가 작동하지 않는 경우, ORDER BY 문이 사용된 SQL문 튜닝

<br>

### WHERE 문이 사용된 SQL문 튜닝
#### WHERE 문 단일 조건
- WHERE 문의 부등호(>, <, <=, >=, =), IN, BETWEEN, LIKE 와 같은 곳에서 사용되는 컬럼은 인덱스를 사용했을 때 성능이 향상될 가능성이 높다. 
- 예시
```sql
-- SQL 문 - created_at 은 UNIQUE 
SELECT * FROM users
WHERE created_at >= DATE_SUB(NOW(), INERVAL 3 DAY); 

-- INDEX 생성
CREATE INDEX idx_created_at ON users (created_at);
```

#### WHERE 문 다중 조건
- `WHERE` 문에 조건이 여러 개 있을 때, `NOT UNIQUE` 칼럼보다 `UNIQUE` 조건이 있는 컬럼으로 인덱스를 사용하는 것이 효율적이다.
- 모든 조건에 인덱스를 생성하더라도 `EXPLAIN` 을 통해 확인해보면 가장 효율적인 인덱스만 사용한 것을 볼 수 있다. → 쓰기 작업까지 고려
- 데이터 액세스(rows) 를 크게 줄일 수 있는 컬럼은 중복 정도가 낮은 컬럼이기 때문에 중복 정도가 낮은 컬럼을 골라 인덱스를 생성한다. 
- Index 와 Multi-Column Index 간 성능 차이가 크지 않을 때는 Index 를 최소한으로 유지하기 위해 단일 컬럼에만 설정하는 일반 Index 를 활용한다.
- 예시
```sql
-- SQL 문
SELECT * FROM users
WHERE department = 'Sales' 
AND created_at >= DATE_SUB(NOW(), INTERVAL 3 DAY);

-- 1안)INDEX 생성
-- CREATE INDEX idx_department ON users (department); → type: ref
CREATE INDEX idx_created_at ON users (created_at); -- → type: range

-- 2안)Multi-Column INDEX 생성
CREATE INDEX idx_created_at_department ON users (department, created_at);
CREATE INDEX idx_created_at_department ON users (created_at, department);
```

<br>

### 인덱스가 작동하지 않는 경우
#### 넓은 범위 데이터 조회
- 넓은 범위의 데이터를 조회할 때는 옵티마이저가 인덱스를 활용하는 것보다 풀 테이블 스캔으로 데이터를 찾는 게 효율적이라고 판단할 수 있다.
- 인덱스를 활용하면 인덱스을 거쳤다가 원래 테이블의 데이터를 일일이 하나씩 찾는 과정을 거친다.
- 예시
```sql
-- SQL 문 ( 넓은 범위의 데이터 조회 )
SELECT * FROM users
ORDER BY name DESC;

-- INDEX 생성
CREATE INDEX idx_name ON users (name);

--> EXPLAIN : 인덱스를 사용하는 것보다 풀 테이블 스캔이 효과적이라 판단
```

#### 인덱스 컬럼을 가공한 경우
- 함수나 연산을 사용해 인덱스 컬럼을 가공하면 MySQL 에서는 활용하지 못하는 경우가 많다.
- 인덱스를 적극 활용하기 위해 인덱스 컬럼 자체를 최대한 가공하지 않도록 한다. 
- 예시
```sql
-- (문제) SQL 문
SELECT * FROM users 
WHERE SUBSTRING(name, 1, 10) = 'User0000';

SELECT * FROM users
WHERE salary * 2 < 1000
ORDER BY salary;

-- INDEX 생성
CREATE INDEX idx_name ON users (name);
CREATE INDEX idx_salary ON users (salary);

--> EXPLAIN : 인덱스 컬럼이 가공되어 활용하지 못하고 풀 테이블 스캔

-- (해결) SQL 문 수정
SELECT * FROM users
WHERE name LIKE 'User0000%';

SELECT * FROM users
WHERE salary < 1000 / 2
ORDER BY salary;
```

<br>

### ORDER BY 문이 사용된 SQL문 튜닝
- `ORDER BY` 는 시간이 오래걸리는 작업이므로 최대한 피해주는 것이 좋다.
- `LIMIT` 없이 큰 범위 데이터를 조회하면 풀 테이블 스캔을 해버릴 수 있기 때문에 `LIMIT` 으로 작은 데이터의 범위를 조회해오도록 신경쓴다. 
- 예시
```sql
-- SQL 문
SELECT * FROM users
ORDER BY salary
LIMIT 100; -- 넓은 범위를 조회하면 type: ALL 이 되기 때문에 LIMIT 으로 제한 걸기

-- INDEX 생성
CREATE INDEX idx_salary ON users (salary);

--> EXPLAIN type: index
```

<br>

### WHERE 문에 인덱스 걸기 vs ORDER BY 문에 인덱스 걸기
- `ORDER BY` 특징 상 모든 데이터를 바탕으로 정렬해야 하기 때문에 인덱스 풀 스캔 또는 테이블 풀 스캔을 활용할 수 밖에 없다.
- `ORDER BY` 보다 `WHERE` 에 있는 컬럼에 인덱스를 걸었을 때 성능이 향상되는 경우가 많다. 
- 실행 계획과 SQL 실행 시간을 보면서 어떻게 인덱스를 거는 지 찾는 것이 정확하다.
- 예시
```sql
-- SQL 문
SELECT * FROM users
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 3 DAY)
AND department = 'Sales'
ORDER BY salary
LIMIT 100;

-- INDEX 생성
-- CREATE INDEX idx_salary ON users (salary);
CREATE INDEX idx_created_at ON users (created_at);
```

<br>

## 참고
[비전공자도 이해할 수 있는 MySQL 성능 최적화 입문/실전 (SQL 튜닝편)](https://inf.run/DzjSq)