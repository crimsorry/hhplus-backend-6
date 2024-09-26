package io.hhplus.tdd.point.usecase;

import io.hhplus.tdd.point.interfaces.repository.PointHistoryRepository;
import io.hhplus.tdd.point.entities.TransactionType;
import io.hhplus.tdd.point.interfaces.repository.UserPointRepository;
import io.hhplus.tdd.point.entities.PointHistory;
import io.hhplus.tdd.point.entities.UserPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.locks.Lock;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final ConcurrencyManager concurrencyManager;

    /* 유저 포인트 충전 */
    @Override
    public UserPoint chargePoints(long userId, long amount) {
        Lock lock = concurrencyManager.getUserLock(userId); // 유저 id 별 동시성 제어
        lock.lock();
        try{
            UserPoint userPoint = userPointRepository.findById(userId);
            long updatedPoint = userPoint.point() + amount;
            if(updatedPoint>999999L){
                throw new IllegalArgumentException("999,999 포인트 보유 한도 초과입니다.");
            }
            userPoint = userPointRepository.insertOrUpdate(userId, updatedPoint);
            pointHistoryRepository.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());
            return userPoint;
        }finally {
            lock.unlock();
        }
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
        Lock lock = concurrencyManager.getUserLock(userId);
        lock.lock();
        try{
            UserPoint userPoint = userPointRepository.findById(userId);
            if(amount>500000L){
                throw new IllegalArgumentException("500,000 포인트 사용 한도 초과입니다.");
            }
            long updatedPoint = userPoint.point() - amount;
            if(updatedPoint<0){
                throw new IllegalArgumentException("포인트가 부족합니다.");
            }else
                userPoint = userPointRepository.insertOrUpdate(userId, updatedPoint);
            pointHistoryRepository.insert(userId, amount, TransactionType.USE, System.currentTimeMillis());
            return userPoint;
        }finally {
            lock.unlock();
        }
    }

    /* 유저 포인트 충전/이용 조회 */
    @Override
    public List<PointHistory> chargeUsePoints(long userId) {
        return pointHistoryRepository.selectAllByUserId(userId);
    }


}