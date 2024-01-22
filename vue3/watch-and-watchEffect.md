## (vue3) watch 와 watchEffect

> 반응형 시스템을 활용하여 데이터의 변화를 감지하고 이에 따라 부수 효과를 실행하는 데 사용. ( [공식문서](https://vuejs.org/guide/essentials/watchers.html) )

<br>

## 1. watch
- 지정된 데이터 소스의 변화를 감시하고 해당 데이터가 변경될 때마다 제공된 콜백함수를 실행한다.
- 감시할 반응형 데이터와 데이터 변화에 대응하여 실행될 콜백 함수 를 인자로 받는다.
- 콜백 함수는 이전 값과 현재 값을 인자로 받을 수 있으며 이를 통해 데이터가 어떻게 변했는지 파악할 수 있다.
```javascript
watch(obj, (newValue, oldValue) => {
// 데이터가 변화했을 때 실행될 로직
});
```
<br>

## 2. watchEffect
- 지정된 데이터 소스에 대한 의존성을 자동으로 추적하고, 소스 내에서 사용된 반응형 상태가 변경도리 때마다 실행된다.
- 콜백 함수만을 인자로 받으며, 해당 콜백 내에서 사용된 모든 반응형 상태에 대해 자동으로 감시한다.
- 콜백 내의 반응형 상태가 최초로 실행될 때 즉시 실행되고 의존성 중 하나라도 변경되면 다시 실행된다.
- 주로 컴포넌트가 마운트될 때부터 반응형 상태에 대해 자동으로 의존성을 추적하고 정리한다. (clean-up)
```javascript
watchEffect(() => {
	// 의존성이 있는 반응형 상태가 변경될 때 실행될 로직
});
```
<br>

## 3. 사용 사례
특정 데이터의 변화를 정밀하게 감지하고 복잡한 로직을 처리해야 하는 경우에는 watch 를 반응형 상태의 변화에 대해 자동적으로 의존성을 추적하고 간단한 부수 효과를 처리하는 경우에는 watchEffect 를 사용하는 것이 적합해 보인다.

<br>

## 참고 
[인프런 - 프로젝트로 배우는 Vue.js 3](https://inf.run/XZ5f) 