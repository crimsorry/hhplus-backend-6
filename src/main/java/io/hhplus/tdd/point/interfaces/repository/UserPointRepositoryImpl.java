package io.hhplus.tdd.point.interfaces.repository;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.entities.UserPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserPointRepositoryImpl implements UserPointRepository {

    private final UserPointTable userPointTable;

    @Override
    public UserPoint findById(long userId) {
        return userPointTable.selectById(userId);
    }

    @Override
    public UserPoint insertOrUpdate(long userId, long amount) {
        return userPointTable.insertOrUpdate(userId, amount);
    }
}