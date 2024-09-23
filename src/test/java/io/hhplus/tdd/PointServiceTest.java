package io.hhplus.tdd;

import io.hhplus.tdd.point.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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


}
