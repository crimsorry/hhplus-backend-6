package io.hhplus.tdd.point.interfaces.repository;

import io.hhplus.tdd.point.entities.TransactionType;
import io.hhplus.tdd.point.entities.PointHistory;

import java.util.List;

public interface PointHistoryRepository {
    void insert(long userId, long amount, TransactionType type, long timestamp);
    List<PointHistory> selectAllByUserId(long userId);
}