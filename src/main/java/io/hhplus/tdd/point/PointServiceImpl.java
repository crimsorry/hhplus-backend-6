package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    @Override
    public UserPoint chargePoints(long userId, long amount) {
        UserPoint userPoint = userPointRepository.findById(userId);
        long updatedPoint = userPoint.point() + amount;

        userPoint = userPointRepository.insertOrUpdate(userId, updatedPoint);
        pointHistoryRepository.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());

        return userPoint;
    }


}