package io.hhplus.tdd;

import io.hhplus.tdd.point.entities.PointHistory;
import io.hhplus.tdd.point.entities.UserPoint;
import io.hhplus.tdd.point.entities.TransactionType;
import io.hhplus.tdd.point.interfaces.repository.PointHistoryRepository;
import io.hhplus.tdd.point.interfaces.repository.UserPointRepository;
import io.hhplus.tdd.point.usecase.ConcurrencyManager;
import io.hhplus.tdd.point.usecase.PointServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PointServiceUnitTest {

    @InjectMocks
    private PointServiceImpl pointService;

    @Mock
    private UserPointRepository userPointRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @Mock
    private ConcurrencyManager concurrencyManager;

    @Mock
    private Lock lock;

    public void beforeLock(long userId) {
        when(concurrencyManager.getUserLock(userId)).thenReturn(lock);
        doNothing().when(lock).lock();
        doNothing().when(lock).unlock();
    }

    public void afterLock() {
        verify(lock).lock();
        verify(lock).unlock();
    }

    @Test
    public void 유저_포인트_충전_성공_검증() {
        // Given
        long userId = 1L;
        long point = 100L;
        long pointCharge = 400L;
        UserPoint userPoint = new UserPoint(userId, point, System.currentTimeMillis());
        UserPoint userPointUpdate = new UserPoint(userId, point + pointCharge, System.currentTimeMillis());
        beforeLock(userId);

        // when
        when(userPointRepository.findById(userId)).thenReturn(userPoint);
        when(userPointRepository.insertOrUpdate(userId, point + pointCharge)).thenReturn(userPointUpdate);

        // then
        UserPoint result = pointService.chargePoints(userId, pointCharge);

        // 결과 검증
        assertEquals(point + pointCharge, result.point()); // 유저 포인트 합계 검증
        verify(userPointRepository).insertOrUpdate(userId, point + pointCharge); // 메서드 호출 검증
        verify(pointHistoryRepository).insert(eq(userId), eq(pointCharge), eq(TransactionType.CHARGE), anyLong());
        afterLock();
    }

    @Test
    public void 유저_포인트_조회_검증(){
        // given
        long userId = 1L;
        long point = 100L;
        UserPoint userPoint = new UserPoint(userId, point, System.currentTimeMillis());

        // when
        when(userPointRepository.findById(userId)).thenReturn(userPoint);

        // then
        UserPoint result = pointService.getPoints(userId);

        // 결과 검증
        assertEquals(point, result.point());
    }

    @Test
    public void 유저_포인트_이용_검증(){
        // given
        long userId = 1L;
        long point = 600L;
        long pointUse = 400L;
        UserPoint userPoint = new UserPoint(userId, point, System.currentTimeMillis());
        UserPoint userPointUse = new UserPoint(userId, point - pointUse, System.currentTimeMillis());
        beforeLock(userId);

        // when
        when(userPointRepository.findById(userId)).thenReturn(userPoint);
        when(userPointRepository.insertOrUpdate(userId, point - pointUse)).thenReturn(userPointUse);

        // then
        UserPoint result = pointService.usePoints(userId, pointUse);

        // 결과 검증
        assertEquals(point - pointUse, result.point()); // 유저 포인트 합계 검증
        verify(userPointRepository).insertOrUpdate(userId, point - pointUse); // 메서드 호출 검증
        verify(pointHistoryRepository).insert(eq(userId), eq(pointUse), eq(TransactionType.USE), anyLong());
        afterLock();
    }

    @Test
    public void 유저_포인트_충전_조회_검증() {
        // given
        long userId = 1L;
        long point = 600L;
        long pointCharge = 400L;
        UserPoint userPoint = new UserPoint(userId, point, System.currentTimeMillis());
        UserPoint userPointUpdate = new UserPoint(userId, point + pointCharge, System.currentTimeMillis());
        beforeLock(userId);

        List<PointHistory> pointHistoryListExpect = new ArrayList<>();
        pointHistoryListExpect.add(new PointHistory(1L, userId, pointCharge, TransactionType.CHARGE, System.currentTimeMillis()));

        // when
        when(userPointRepository.findById(userId)).thenReturn(userPoint);
        when(userPointRepository.insertOrUpdate(userId, point + pointCharge)).thenReturn(userPointUpdate);
        when(pointHistoryRepository.selectAllByUserId(userId)).thenReturn(pointHistoryListExpect);

        // then
        UserPoint resultCharge = pointService.chargePoints(userId, pointCharge);
        List<PointHistory> pointHistoryList = pointService.chargeUsePoints(userId);

        // 결과 검증
        assertEquals(point + pointCharge, resultCharge.point()); // 포인트 충전 검증
        assertEquals(pointHistoryListExpect, pointHistoryList); // 포인트 내역 검증

        // 메서드 호출 검증
        verify(userPointRepository).insertOrUpdate(userId, point + pointCharge);
        verify(pointHistoryRepository).insert(eq(userId), eq(pointCharge), eq(TransactionType.CHARGE), anyLong());
        verify(pointHistoryRepository).selectAllByUserId(userId);
        afterLock();
    }

    @Test
    public void 유저_포인트_사용_조회_검증() {
        // given
        long userId = 1L;
        long point = 1000L;
        long pointUse = 200L;
        UserPoint userPointAfterCharge = new UserPoint(userId, point, System.currentTimeMillis());
        UserPoint userPointAfterUse = new UserPoint(userId, point - pointUse, System.currentTimeMillis());
        beforeLock(userId);

        List<PointHistory> pointHistoryListExpect = new ArrayList<>();
        pointHistoryListExpect.add(new PointHistory(1L, userId, pointUse, TransactionType.USE, System.currentTimeMillis()));

        // when
        when(userPointRepository.findById(userId)).thenReturn(userPointAfterCharge);
        when(userPointRepository.insertOrUpdate(userId, point - pointUse)).thenReturn(userPointAfterUse);
        when(pointHistoryRepository.selectAllByUserId(userId)).thenReturn(pointHistoryListExpect);

        // then
        UserPoint resultUse = pointService.usePoints(userId, pointUse);
        List<PointHistory> pointHistoryList = pointService.chargeUsePoints(userId);

        // 결과 검증
        assertEquals(point - pointUse, resultUse.point()); // 포인트 사용 검증
        assertEquals(pointHistoryListExpect, pointHistoryList); // 포인트 내역 검증

        // 메서드 호출 검증
        verify(userPointRepository).insertOrUpdate(userId, point - pointUse);
        verify(pointHistoryRepository).insert(eq(userId), eq(pointUse), eq(TransactionType.USE), anyLong());
        verify(pointHistoryRepository).selectAllByUserId(userId);
        afterLock();
    }

    @Test
    public void 유저_포인트_사용_부족_검증(){
        // given
        long userId = 1L;
        long point = 200L;
        long pointUse = 300L;
        UserPoint userPoint = new UserPoint(userId, point, System.currentTimeMillis());
        beforeLock(userId);

        // when
        when(userPointRepository.findById(userId)).thenReturn(userPoint);

        // then
        Exception exception = assertThrows(CustomPointException.class, () -> {
            pointService.usePoints(userId, pointUse);
        });

        // 결과 검증
        assertEquals("포인트가 부족합니다.", exception.getMessage());
        // exception 발생으로 해당 메소드가 호출되지 않았기 때문에 never() 추가.
        verify(userPointRepository, never()).insertOrUpdate(userId, point - pointUse);
        afterLock();
    }

    /*
    * 999,999 포인트 초과 보유 불가
    * */
    @Test
    public void 유저_포인트_충전_한도_초과_검증(){
        // given
        long userId = 1L;
        long point = 900000L;
        long pointCharge = 100000L;
        UserPoint userPoint = new UserPoint(userId, point, System.currentTimeMillis());
        beforeLock(userId);

        // when
        when(userPointRepository.findById(userId)).thenReturn(userPoint);

        // then
        Exception exception = assertThrows(CustomPointException.class, () -> {
            pointService.chargePoints(userId, pointCharge);
        });

        // 결과 검증
        assertEquals("999,999 포인트 보유 한도 초과입니다.", exception.getMessage());
        verify(userPointRepository, never()).insertOrUpdate(userId, point + pointCharge);
        afterLock();
    }

    /*
     * 500,000 포인트 초과 사용 불가
     * */
    @Test
    public void 유저_포인트_사용_한도_초과_검증(){
        // given
        long userId = 1L;
        long point = 900000L;
        long pointUse = 600000L;
        UserPoint userPoint = new UserPoint(userId, point, System.currentTimeMillis());
        beforeLock(userId);

        // when
        when(userPointRepository.findById(userId)).thenReturn(userPoint);

        // then
        Exception exception = assertThrows(CustomPointException.class, () -> {
            pointService.usePoints(userId, pointUse);
        });

        // 결과 검증
        assertEquals("500,000 포인트 사용 한도 초과입니다.", exception.getMessage());
        verify(userPointRepository, never()).insertOrUpdate(userId, point - pointUse);
        afterLock();
    }

    /*
     * 음수 포인트 사용/충전 불가
     * */
    @Test
    public void 유저_포인트_사용_충전_유효성_검증() {
        // given
        long userId = 1L;
        long point = -100L;

        // when & then
        Exception exception = assertThrows(CustomPointException.class, () -> {
            new UserPoint(userId, point, System.currentTimeMillis());
        });

        assertEquals("포인트 금액이 음수 입니다.", exception.getMessage());
    }


    /* 2. 동시성 제어 - 유저_포인트_충전_사용_조회_검증
    *  단위테스트 - mock 결과값과 exception 성공/실패 응답 여부를 확인하게 위해 테스트
    *  통합테스트 - 실제 서비스를 호출하고 성공하는지 파악하기 위해 테스트.
    *    > 따라서 동시성 테스트는 통합테스트에서 실행하려 함.
    * */

    /*
    * 3. 존재하지 않는 유저 검증 -
    * 유저 존재하지 않는 경우 amount 0 추가.
    * 유저가 없어도 list 호출이 되지만 실사용 서비스처럼 개발하기 위해 추가.
    * history 추가 X. > 아니면 회원가입 축하 메세지? 200 포인트 충전 시켜서 history 에 충전으로 남기기... (고민)
    *   > 현재 구조: 포인트 충전 시 유저 insert + update.
    *       > 따라서 존재하지 않는 검증 불필요로 정리.
    * */


}
