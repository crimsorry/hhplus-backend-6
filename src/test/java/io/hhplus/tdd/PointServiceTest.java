package io.hhplus.tdd;

import io.hhplus.tdd.point.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

    @InjectMocks
    private PointServiceImpl pointService;

    @Mock
    private UserPointRepository userPointRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @Test
    public void 유저_포인트_충전_성공_검증() {
        // Given
        long userId = 1L;
        long point = 100L;
        long pointCharge = 400L;
        UserPoint userPoint = new UserPoint(userId, point, System.currentTimeMillis());
        UserPoint userPointUpdate = new UserPoint(userId, point + pointCharge, System.currentTimeMillis());

        // when
        when(userPointRepository.findById(userId)).thenReturn(userPoint);
        when(userPointRepository.insertOrUpdate(userId, point + pointCharge)).thenReturn(userPointUpdate);

        // then
        UserPoint result = pointService.chargePoints(userId, pointCharge);

        // 결과 검증
        assertEquals(point + pointCharge, result.point()); // 유저 포인트 합계 검증
        verify(userPointRepository).insertOrUpdate(userId, point + pointCharge); // 메서드 호출 검증
        verify(pointHistoryRepository).insert(eq(userId), eq(pointCharge), eq(TransactionType.CHARGE), anyLong());
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

        // when
        when(userPointRepository.findById(userId)).thenReturn(userPoint);
        when(userPointRepository.insertOrUpdate(userId, point - pointUse)).thenReturn(userPointUse);

        // then
        UserPoint result = pointService.usePoints(userId, pointUse);

        // 결과 검증
        assertEquals(point - pointUse, result.point()); // 유저 포인트 합계 검증
        verify(userPointRepository).insertOrUpdate(userId, point - pointUse); // 메서드 호출 검증
        verify(pointHistoryRepository).insert(eq(userId), eq(pointUse), eq(TransactionType.USE), anyLong());
    }

    @Test
    public void 유저_포인트_충전_조회_검증() {
        // given
        long userId = 1L;
        long point = 600L;
        long pointCharge = 400L;
        UserPoint userPoint = new UserPoint(userId, point, System.currentTimeMillis());
        UserPoint userPointUpdate = new UserPoint(userId, point + pointCharge, System.currentTimeMillis());

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
    }

    @Test
    public void 유저_포인트_사용_조회_검증() {
        // given
        long userId = 1L;
        long point = 1000L;
        long pointUse = 200L;
        UserPoint userPointAfterCharge = new UserPoint(userId, point, System.currentTimeMillis());
        UserPoint userPointAfterUse = new UserPoint(userId, point - pointUse, System.currentTimeMillis());

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
    }


}
