package io.hhplus.tdd;

import io.hhplus.tdd.point.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

// 통합테스트: spring context 까지 로드
// 단위테스트와 차이점: 실제 서비스, 리포지토리, DB 사용
@SpringBootTest(classes = TddApplication.class)
@ExtendWith(SpringExtension.class)
public class PointServiceIntegrationTest {

    @Autowired
    private PointServiceImpl pointService;

    @Autowired
    private UserPointRepository userPointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Test
    public void 유저_포인트_조회_통합테스트(){
        // given
        long userId = 1L;

        // when
        UserPoint result = pointService.getPoints(userId);

        // then
        assertEquals(0, result.point());
    }

//    @Test
//    public void 유저_포인트_사용_통합테스트(){
//        // given
//        long userId = 1L;
//        long pointCharge = 600L;
//        long pointUse = 400L;
//        pointService.chargePoints(userId, pointCharge); // 포인트 충전
//
//        // when
//        UserPoint result = pointService.usePoints(userId, pointUse);
//
//        // then
//        assertEquals(pointCharge - pointUse, result.point());
//        List<PointHistory> histories = pointHistoryRepository.selectAllByUserId(userId);
//        assertEquals(2, histories.size());
//        assertEquals(TransactionType.USE, histories.get(1).type());
//        assertEquals(pointUse, histories.get(1).amount());
//    }
//
//    @Test
//    public void 유저_포인트_충전_성공_통합테스트() {
//        // Given
//        long userId = 1L;
//        long pointCharge = 400L;
//
//        // When
//        UserPoint result = pointService.chargePoints(userId, pointCharge);
//
//        // Then
//        assertEquals(pointCharge, result.point());
//        List<PointHistory> histories = pointHistoryRepository.selectAllByUserId(userId);
//        assertEquals(1, histories.size());
//        assertEquals(TransactionType.CHARGE, histories.get(0).type());
//        assertEquals(pointCharge, histories.get(0).amount());
//    }

    // https://www.baeldung.com/java-countdown-latch
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

        // TODO: 테스트 100개 들어오면 마지막은 오래 기다려야됨. 진짜 그러는지 확인 필요. 아니면 그 redsy, start, end 써야됨.
        // 아래 코드 이용하면 순서 보장 안됨. 먼저 끝나는거 먼저 실행.
        // 근데 우리는 user id가 다른건 병렬로 처리되고 같으면 순서성을 보장해야하잖아?
        // 아냐 ReentrantLock 로 순서 보장 했을니까 괜찮을듯.

        // when
        executorService.submit(() -> { // 포인트 충전 1
            try {
                pointService.chargePoints(userId, pointCharge);
            } finally {
                latch.countDown();  // 작업 완료 시 latch 카운트 감소
            }
        });
        executorService.submit(() -> { // 포인트 사용 1
            try {
                pointService.usePoints(userId, pointUse);
            } finally {
                latch.countDown();
            }
        });
        executorService.submit(() -> { // 포인트 충전 2
            try {
                pointService.chargePoints(userId, pointCharge2);
            } finally {
                latch.countDown();
            }
        });
        executorService.submit(() -> { // 포인트 사용 2
            try {
                pointService.usePoints(userId, pointUse2);
            } finally {
                latch.countDown();
            }
        });
        executorService.submit(() -> { // 포인트 사용 3
            try {
                pointService.usePoints(userId, pointUse3);
            } finally {
                latch.countDown();
            }
        });

        latch.await(); // 0 될때까지 대기

        // then
        List<PointHistory> result = pointService.chargeUsePoints(userId);
        assertEquals(6, result.size());
        assertEquals(point + pointCharge + pointCharge2 + pointUse + pointUse2 + pointUse3, result.get(0).amount() + result.get(1).amount() + result.get(2).amount() + result.get(3).amount() + result.get(4).amount() + result.get(5).amount());

        for(PointHistory point2 : result){
            System.out.println(point2.amount() + " " + point2.type());
        }

        // 스레드 풀 종료
        executorService.shutdown();
    }

    // 단위 테스트에서 실패 케이스에 대해 테스트 했기 때문에 통합테스트에서는 db에 제대로 저장 되는지 확인!



}
