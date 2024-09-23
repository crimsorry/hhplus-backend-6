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

    /* 유저 포인트 충전 */
    @Override
    public UserPoint chargePoints(long userId, long amount) {
        UserPoint userPoint = userPointRepository.findById(userId);

        long updatedPoint = userPoint.point() + amount;

        userPoint = userPointRepository.insertOrUpdate(userId, updatedPoint);
        pointHistoryRepository.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());
        return userPoint;
    }

    /* 유저 포인트 조회 */
    @Override
    public UserPoint getPoints(long userId) {
        UserPoint userPoint = userPointRepository.findById(userId);
        return userPoint;
    }

    /* 유저 포인트 이용 */
    @Override
    public UserPoint usePoints(long userId, long amount) {
        UserPoint userPoint = userPointRepository.findById(userId);
        long updatedPoint = userPoint.point() - amount;
        if(updatedPoint<0){
            throw new RuntimeException("포인트가 부족합니다.");
        }
        userPoint = userPointRepository.insertOrUpdate(userId, updatedPoint);
        pointHistoryRepository.insert(userId, amount, TransactionType.USE, System.currentTimeMillis());
        return userPoint;
    }


}