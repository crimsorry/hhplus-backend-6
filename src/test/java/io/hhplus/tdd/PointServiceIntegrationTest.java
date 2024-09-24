package io.hhplus.tdd;

import io.hhplus.tdd.point.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    public void 유저_포인트_충전_성공_통합테스트() {
        // Given
        long userId = 1L;
        long pointCharge = 400L;

        // When
        UserPoint result = pointService.chargePoints(userId, pointCharge);

        // Then
        assertEquals(pointCharge, result.point());
        List<PointHistory> histories = pointHistoryRepository.selectAllByUserId(userId);
        assertEquals(1, histories.size());
        assertEquals(TransactionType.CHARGE, histories.get(0).type());
        assertEquals(pointCharge, histories.get(0).amount());
    }



}
