package io.hhplus.tdd.point;

import java.util.List;

public interface PointHistoryRepository {
    void insert(long userId, long amount, TransactionType type, long timestamp);
    List<PointHistory> selectAllByUserId(long userId);
}