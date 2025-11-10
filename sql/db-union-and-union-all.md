## (sql) UNION 과 UNION ALL 
> UNION 과 UNION ALL 규칙, 성능, 정렬

<br>

## UNION 과 UNION ALL 
- JOIN 은 테이블을 옆으로 붙여 더 많은 정보를 가진 컬럼을 만들고, UNION 은 여러 개의 결과 집합을 아래로 이어 붙여 더 많은 행을 만든다. 
- `UNION` : 두 결과 집합을 합친 뒤 완전히 중복된 행은 자동으로 제거한다.
- `UNION ALL` : 두 결과 집합을 합친 모든 행을 출력한다. 
- 예시
```sql
SELECT name, email FROM users
[UNION / UNION ALL]
SELECT name, email FROM retired_users;
```

### 규칙
- UNION 으로 연결되는 모든 SELECT 문은 컬럼의 개수가 동일해야 한다
- 같은 위치의 컬럼들은 서로 호환 가능한 데이터 타입이어야 한다 
- 최종 결과의 컬럼 이름은 첫번째 SELECCT 문을 따른다

### 성능
- UNION ALL 이 UNION 보다 훨씬 빠르다
- UNION 은 중복을 찾아내는 과정을 거쳐야하기 때문에 중복을 제거해야하는 경우에만 사용하도록 한다

### 정렬
- 최종 결과 집합에 대해 정렬을 적용할 수 있다
- 전체 UNION 연산의 가장 마지막에 한 번만 정의한다. 
- 결과 집합의 컬럼명이 첫번째 SELECT 문을 따르기 때문에 첫번째 SELECT 문의 컬럼명이나 해당 컬럼명의 별칭만 사용한다. 
- 예시
```sql
SELECT name, email FROM users
UNION
SELECT name, email FROM retired_users 
ORDER BY name; -- 최종 결과에 대한 정렬
```

<br>

## 참고
[김영한의 실전 데이터베이스 - 기본편](https://inf.run/2aFFu)