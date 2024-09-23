package io.hhplus.tdd.point;

public interface PointHistoryRepository {
    void insert(long userId, long amount, TransactionType type, long timestamp);
}