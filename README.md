# 프로젝트 개요 + 동시성 분석 보고서 (하단)

항해99 벡엔드 플러스 과제

![img](https://teamsparta.notion.site/image/https%3A%2F%2Fprod-files-secure.s3.us-west-2.amazonaws.com%2F83c75a39-3aba-4ba4-a792-7aefe4b07895%2Ff9277d4b-c85f-47d0-8284-17c7f64a258a%2Fimg_summary.png?table=block&id=4ef44073-8cc9-4fce-a63b-c01a93a3aca8&spaceId=83c75a39-3aba-4ba4-a792-7aefe4b07895&width=1420&userId=&cache=v2)

* 1주차: TDD 로 개발하기



## 과제 

- 매주 금요일 10시 이전까지 제출



## git branch 관리

- 개발 : main 브랜치에서 새 브랜치 생성하여 작업

  >  예시: [feat] pointCharge

- PR : 새 브랜치에서 작업 종료되면 main 브랜치로 remote push, 새브랜치 -> main 브랜치 PR

- 업무 종류 : 

  > - [feat] : 새로운 기능 추가, 기존의 기능을 요구 사항에 맞추어 수정
  > - [fix] : 기능에 대한 버그 수정
  > - [refactor] : 기능의 변화가 아닌 코드 리팩터링 ex) 변수 이름 변경
  > - [test] : 테스트 코드 추가/수정
  > - [build] : 빌드 관련 수정
  > - [chore] : 패키지 매니저 수정, 그 외 기타 수정 ex) .gitignore
  > - [docs] : 문서(주석) 수정
  > - [style] : 코드 스타일, 포맷팅에 대한 수정
  > - [delete] : 파일 삭제



## 동시성 제어 분석 및 보고서

### :zap: 개요

* 사용자 포인트 시스템에서 발생 가능한 동시성 이슈를 해결하기 위해 학습한 동시성 제어 방식에 대해 다룹니다.
* 동시성 이슈는 여러 스레드가 동일한 자원에 동시에 접근할 때 발생할 수 있으며, 이러한 문제를 해결하기 위해 다양한 동시성 제어 기술을 사용하였습니다.



### :zap: 동시성 문제 발생

1. **포인트 충전/사용**: 여러 사용자가 동시에 포인트를 충전하거나 사용하는 경우, **포인트가 잘못 계산**되거나 **데이터 무결성**이 깨질 위험이 있습니다. 

2. **동시성 충돌**: 동일한 사용자가 여러 스레드에서 동시에 포인트를 충전하거나 사용할 경우, **정확한 포인트 합산**이 이뤄지지 않을 수 있습니다.

> 이를 해결하기 위해 `ReentrantLock`, `ConcurrentHashMap`, `CountDownLatch` 를 이용한 동시성 제어가 적용되었습니다.



### :zap: 동시성 제어 방식

### 1. Lock :white_check_mark:

* **특징**: 동시에 **여러 쓰레드**가 같은 자원에 접근하는 것을 제한
* **장점**: 데이터의 일관성과 무결성을 유지
* **채택 사유** : 여러 클래스 (`ReentrantLock`,  `StampedLock` 등) 를 사용해 동시성을 구현해보고자 채택했습니다. 



### 1.1 Syncronized

*  **특징**: 단일 스레드
*  **장점**: 간단하고 사용이 직관적입니다.
*  **단점**: 제어권이 제한적이며, 시간이 오래 걸리면 스레드가 기다리게 됩니다.
*  **비 채택 사유** : 포인트 충전/사용이 다량 발생한 경우 마지막 요청은 자기 차례가 올때까지 대기애햐 하기 때문에 사용하지 않았습니다.



### 2.1 ReentrantLock :white_check_mark:

* **특징**: 

  * Lock 인터페이스를 구현한 클래스로 **재진입이 가능**한 Lock
  * 한 쓰레드가 이미 Lock을 보유하고 있는 경우, 해당 쓰레드는 Lock을 다시 획득

* **장점**: 

  * 락 획득과 해제를 더 세밀하게 제어, **락 명시적으로 해제**.
  * `tryLock()` 같은 비차단 락 획득 방법과 **공정성 설정**(스레드 순서 보장) 가능

* **단점**: `unlock()`을 반드시 명시적으로 호출해야 하며, 코드 복잡

* **채택 사유:** 

  * 포인트 충전/사용이 다량 발생한 경우 스레드 순서 보장이 가능하며 스레드 순서 보장이 가능하며 동시성 보장이 가능하기 때문에 선택했습니다.
  * 그리고 다수의 스레드가 포인트 데이터를 경쟁할 때 **스레드 기아 문제**를 해결할 수 있습니다.

* **적용 코드**: 

  ```java
  @Component
  public class ConcurrencyManager {
      private final ConcurrentHashMap<Long, Lock> userLocks = new ConcurrentHashMap<>();
  
      // 특정 유저 Lock 가져오거나 없으면 생성
      public Lock getUserLock(long userId) {
          return userLocks.computeIfAbsent(userId, id -> new ReentrantLock());
      }
  }
  ```



###  2.2 StampedLock

* **특징**: 
  * **낙관적 락**: 여러 스레드가 동시에 읽기 작업 할 수 있도록 허용하며 쓰기 작업 발생 시 락 다시 검증
  * **비관적 락**: 읽기와 쓰기 모두에 대해 동시 접근을 제어하는 락
* **장점**: 여러 스레드가 동시에 데이터를 읽음
* **단점**: `ReentrantLock`처럼 재진입이 불가능
* **비 채택 사유**: 
  * 포인트 사용/충전 기능은 write 작업이 빈번에 read 성능을 높이는 낙관적 락의 이점이 크지 않다고 생각했습니다. 
  * 비관적 락 기능이 <code>ReentrantLock</code>과 유사하여 사용하지 않았습니다.



### 2.3 Semaphore

* **특징**: 동시에 접근할 수 있는 **스레드 수를 제한**
* **장점**: 스레드 수를 제어. 특정 작업에 대한 **동시 접근을 제한**
* **단점**: 락과 달리 복잡한 자원 관리가 필요한 경우 제어가 어려움
* **비 채택 사유** : 포인트 서비스에서는 스레드 수를 제한하는 상황이 발생하지 않았기 때문에 사용하지 않았습니다.



### 3.1 ConcurrentHashMap :white_check_mark:

* **특징**: 

  * **멀티스레드 환경**에서 안전하며, **동시 읽기 및 쓰기**에 최적화
  * **멀티스레드 환경**에 최적화되어 있기 때문에, **고성능 멀티스레드 애플리케이션**에서 적합
  * **부분 동기화**: 전체 맵에 락을 거는 것이 아니라 **버킷(bucket)** 단위로 나누어 **부분적으로 락**을 걸기 때문에 성능이 뛰어남

* **장점**: 높은 **동시성**을 보장하면서도 빠른 성능을 유지

* **단점**: 단순한 동기화만 필요할 경우, 복잡도가 높을 수 있음

* **채택 사유:**

  * 포인트 서비스 **사용자마다 다른 락**을 사용하기 때문에 **다른 사용자에 대한 요청**은 **동시에 처리**할 수 있어 채택하게 되었습니다.

* **적용 코드**

  ```java
  @Component
  public class ConcurrencyManager {
      private final ConcurrentHashMap<Long, Lock> userLocks = new ConcurrentHashMap<>();
  
      // 특정 유저 Lock 가져오거나 없으면 생성
      public Lock getUserLock(long userId) {
          return userLocks.computeIfAbsent(userId, id -> new ReentrantLock());
      }
  }
  ```



### 3.2 HashMap

* **특징**: 
  * 비동기적인 Map, **단일 스레드**에 유리
  * 동시성 제어가 없으며, 여러 스레드 동시 접근 시 **데이터 일관성 깨짐**
  * <code>synchronizedMap</code> 을 이용한 동기화 가능.
* **장점**: 빠른 속도와 쉬운 사용.
* **단점**: 멀티스레드 환경에서 **메모리 누수**나 **데이터 경합** 문제가 발생
* **비 채택 사유** : 포인트 서비스에서는 동기화가 보장된 자료 구조가 필요하기 때문에 사용하지 않았습니다.

> :heavy_exclamation_mark: 동기화가 보장된 자료구조란?
>
> **여러 스레드가 동시에 접근**할 때도 **일관성**과 **정확성**을 보장하는 자료 구조



### 3.3 HashTable

* **특징**: **동기화**된 Map.
* **장점**: **스레드 안전**을 기본적으로 제공하며, **단순한 멀티스레드 환경**에서는 적합
* **단점**: 모든 메서드에서 **전체 맵에 락을 걸기 때문에 성능 저하**가 발생할 수 있으며, 멀티스레드 환경에서 성능이 `ConcurrentHashMap`에 비해 떨어짐.
* **비 채택 사유** : <code>ConcurrentHashMap</code> 이 성능 면에서 효율적이기 때문에 사용하지 않았습니다. 그리고 Null 처리가 불가능하기 때문에 사용하기 까다로운 문제가 있습니다.



### 4.1 ContDownlatch :white_check_mark:

* **특징**: 

  * **동기화 보조 장치**로, 하나 이상의 스레드가 다른 스레드에서 수행 중인 작업이 완료될 때까지 기다림
  * 한번 사용된 CountDownLatch 재사용 불가

* **장점**: **여러 작업의 동시 완료 여부를 확인**할 때 사용하기 적합

* **단점**: 재사용이 불가능하기 때문에 동인한 카운트 여러번 사용 불가

* **채택 사유** : 테스트 코드에서 사용 시 여러 스레드가 동시에 작업을 수행하고 동시성을 제어하여 처리하기 때문에 채택하게 되었습니다.

* **적용 코드**

  ```java
  	@Test
      public void 동시성제어_유저_포인트_충전사용_통합테스트() throws InterruptedException {
          // given
          long userId = 1L;
          long point = 1500L;
          long pointCharge = 100L;
          long pointCharge2 = 600L;
          long pointUse = 50L;
          long pointUse2 = 250L;
          long pointUse3 = 300L;
          int totalTasks = 5; // 작업 thread
  
          CountDownLatch latch = new CountDownLatch(totalTasks);
          ExecutorService executorService = Executors.newFixedThreadPool(totalTasks);
  
          pointService.chargePoints(userId, point);
  
          // when
          executorService.submit(new PointWorker(pointService, userId, pointCharge, true, latch));
          executorService.submit(new PointWorker(pointService, userId, pointUse, false, latch));
          executorService.submit(new PointWorker(pointService, userId, pointCharge2, true, latch));
          executorService.submit(new PointWorker(pointService, userId, pointUse2, false, latch));
          executorService.submit(new PointWorker(pointService, userId, pointUse3, false, latch));
  
          latch.await(); // 0 될때까지 대기
  
          // then
          List<PointHistory> result = pointService.chargeUsePoints(userId);
          assertEquals(6, result.size());
          assertEquals(point + pointCharge + pointCharge2 + pointUse + pointUse2 + pointUse3, result.get(0).amount() + result.get(1).amount() + result.get(2).amount() + result.get(3).amount() + result.get(4).amount() + result.get(5).amount());
  
          // 스레드 풀 종료
          executorService.shutdown();
      }
  ```



### 4.2 Future

*  **특징**: 
   *  **비동기 작업의 결과를 나타내는 인터페이스**. 비동기적으로 실행되는 작업의 **결과나 상태**를 관리
   *  **단일 작업**에 주로 사용
*  **장점**: **비동기 작업에서 결과를 추적**하거나, **완료 후 추가적인 작업을 수행**할 때 효과적
*  **단점**: 여러 Future 작업 동시에 처리하는 경우 **작업 완료 순서 보장 어려움**
*  **비 채택 사유** : 여러 작업을 동시에 관리하거나 동시성을 보장하는데 적합하지 않다고하여 사용하지 않았습니다.



### :zap: 결론

프로젝트를 통해 TDD, 동시성 제어를 학습 하며 포인트 충전/사용을 통해 발생할 수 있는 정책(예외처리) 를 설정하고 데이터 충돌과 순차성에 대해 고민하면서 해결 방안에 대해 모색할 수 있었습니다. 



### :zap: 추가 고려 사항

* **성능 부하 테스트**: JMeter 과 같은 툴을 이용한 부하 테스트 진행을 진행해 성능 문제를 분석할 필요가 있을 것 같습니다.