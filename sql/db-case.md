## (sql) CASE 문
> CASE 문 종류, 실행 순서 및 위치, 그룹핑, 조건부 집계

<br>

## CASE 문
- 특정 조건에 따라 다른 값을 출력하게 만들 수 있다.
### 종류
#### 1. 단순 CASE 문
- 특정 하나의 컬럼이나 표현식의 값에 따라 결과를 다르게 하고싶을 때 사용
- 사용 방법
```mysql
CASE 비교대상_컬럼_또는_표현식 
	WHEN 값1 THEN 결과1 
	WHEN 값2 THEN 결과2
    ...
	ELSE 그_외의_경우_결과 
END
```

#### 2. 검색 CASE 문
- 단순 CASE 문처럼 하나의 특정 값을 비교하는 대신, 각 WHEN 절에 독립적인 조건식을 사용하여 복잡한 논리를 구현할 때 사용
- WHEN 절의 순서를 배치할 때, 더 포괄적인 조건보다는 더 구체적인 조건을 먼저 배치하는 것이 일반적이다.
- 조건식에서 `>=, =`, `AND, OR, NOT` 등 다양한 비교 연산자와 논리 연산자를 포함할 수 있다.
- 사용 방법
```mysql
CASE
	WHEN 조건1 THEN 결과1
	WHEN 조건2 THEN 결과2    
	...
	ELSE 그_외의_경우_결과 
END
```

#### 실행 순서 및 위치
- 위에서 순서대로 조건을 평가하고 가장 먼저 일치하는 WHEN 절을 만나면 THEN 의 결과를 반환하고 그 즉시 종료한다
- SELECT 절 외에도 ORDER BY, GROUP BY, WHERE 절 등 다양한 SQL 구문과 함께 사용될 수 있다

<br>

### 그룹핑
- CASE 문을 통해 동적으로 만들어낸 값을 GROUP BY 절과 결합할 수도 있다
- `1990년대, 1980년대, 그 이전 출생으로 분류하고 각 그룹에 총 몇 명이 있는지 출력하라` 라는 요구사항이 있을 때 아래와 같이 작성할 수 있다
```mysql
SELECT
	CASE
		WHEN YEAR(birth_date) >= 1990 THEN '1990년대생' 
		WHEN YEAR(birth_date) >= 1980 THEN '1980년대생' 
		ELSE '그 이전 출생'
	END AS birth_decade,
	COUNT(*) AS customer_count
FROM
    users
GROUP BY
	CASE
		WHEN YEAR(birth_date) >= 1990 THEN '1990년대생' 
		WHEN YEAR(birth_date) >= 1980 THEN '1980년대생' 
		ELSE '그 이전 출생'
	END;
```

- SQL 표준 논리적 순서에 따르면 GROUP BY 절이 SELECT 절보다 먼저 처리되기 때문에 GROUP BY 에서 SELECT 절의 별칭을 사용하지 못하는 것이 맞지만 사용자 편의를 위해 많은 데이터베이스에서 이를 허용한다.
```mysql
SELECT
	CASE
		WHEN YEAR(birth_date) >= 1990 THEN '1990년대생' 
		WHEN YEAR(birth_date) >= 1980 THEN '1980년대생' 
		ELSE '그 이전 출생'
	END AS birth_decade,
	COUNT(*) AS customer_count
FROM
    users
GROUP BY birth_decade;
```

<br>

### 조건부 집계
- CASE 문을 SUM, COUNT 와 같은 집계 함수 안으로 넣어 활용한다
- `전체 주문 건수와 함께 '결제 완료(COMPLETED)', '배송(SHIPPED)', '주문 대기(PENDING)' 상태의 주문이 각각 몇 건인지 별도의 컬럼으로 나누어 보고 싶다` 는 요구사항이 있을 때 아래와 같이 작성할 수 있다
#### 1. COUNT 활용
- COUNT 함수는 NULL 이 아닌 모든 값을 센다 
- status 가 'COMPLETED' 인 경우에 1을 반환하고 아닌 경우 NULL 을 반환하기 때문에 COMPLETED 인 행의 개수를 세개 된다
```mysql
COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END)
```

#### 2. SUM 활용
- SUM 함수는 숫자들의 합계를 구한다
```mysql
SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END)
```

<br>

## 참고
[김영한의 실전 데이터베이스 - 기본편](https://inf.run/2aFFu)