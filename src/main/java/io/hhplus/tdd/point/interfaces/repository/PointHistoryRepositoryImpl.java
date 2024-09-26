package io.hhplus.tdd.point.interfaces.repository;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.point.entities.TransactionType;
import io.hhplus.tdd.point.entities.PointHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PointHistoryRepositoryImpl implements PointHistoryRepository {

    private final PointHistoryTable pointHistoryTable;

    @Override
    public void insert(long userId, long amount, TransactionType type, long timestamp) {
        pointHistoryTable.insert(userId, amount, type, timestamp);
    }

    @Override
    public List<PointHistory> selectAllByUserId(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }
}
