## (sql) MySQL 실행 계획
> 실행 계획 조회 및 분석, type 종류 (MySQL 8.4 기준)

<br>

## 실행 계획(EXPLAIN)
- 실행 계획은 옵티마이저가 SQL 문을 처리하기 위해 선택한 실행 방식을 보여준다. 

<br>

### 실행 계획 조회
![image](https://github.com/user-attachments/assets/b7a76952-3558-402c-8e34-7654f40b1433)
- `type` : table 을 어떤 방식으로 조회하는 지 출력
- `possible keys` : 사용할 수 있는 인덱스 목록 
- `key` : 데이터 조회 시 실제로 사용한 인덱스 
- `ref` : 테이블 조인 시 어떤 값을 기준으로 조인했는 지 출력
- `rows` : SQL 문을 실행하기 위해 접근할 것으로 예상되는 행 수 
- `filtered` : 필터 조건에 따라 어느 정도의 비율로 데이터를 제거했는 지를 의미
- 실행 계획 조회 방법
```sql
EXPLAIN <SQL문>;
```

<br>

### 실행 계획 분석
> -> Filter: (users.age = 23)  (cost=0.95 rows=1) (actual time=0.0334..0.0382 rows=2 loops=1)     
> -> Table scan on users  (cost=0.95 rows=7) (actual time=0.0313..0.0366 rows=7 loops=1)|

![image](https://github.com/user-attachments/assets/6b923ef2-5061-492a-b58b-969186ec6411)

- `->` 기준 밑에서 위로 실행한다.
- `Table scan on users` : users 테이블을 풀 스캔
- `rows` : 접근한 행의 수
- `actual time` : 첫 번째 데이터에 접근하기까지의 시간 ... 마지막 데이터까지 접근한 시간 ( ms 초 단위로, 밑에서 소요된 시간이 위로 올라갈 때 포함된다. )
- `Filter` : 필터링을 통해 데이터 추출
- 실행 계획 자세히 조회하기
```sql
EXPLAIN ANALYZE <SQL문>;
```

<br>

### 실행 계획 type 종류
- `ALL` : 풀 테이블 스캔, 인덱스를 활용하지 않고 테이블을 처음부터 끝까지 전부 다 뒤져서 데이터를 찾는 방식 → 비효율적
- `index` : 풀 인덱스 스캔, 인덱스 테이블을 처음부터 끝까지 전부 다 뒤져서 데이터를 찾는 방식 → ALL 보다는 효율적이나 아주 효율적은 X
- `const` : 1 건의 데이터를 바로 찾을 수 있는 경우로, 중복 값이 없는 고유 인덱스(UNIQUE) 또는 기본키를 사용해서 조회한 경우 → 효율적
- `range` : 인덱스 레인지 스캔, 인덱스를 활용해 between, 부등호, in, like 와 같은 범위 형태의 데이터를 조회한 경우 → 효율적이나 범위가 큰 경우 성능 저하의 원인이 될 수 있다.
- `ref` : UNIQUE 가 아닌 비고유 인덱스를 사용한 경우 


<br>

## 참고
[비전공자도 이해할 수 있는 MySQL 성능 최적화 입문/실전 (SQL 튜닝편)](https://inf.run/DzjSq)