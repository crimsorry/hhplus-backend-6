package io.hhplus.tdd.point;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

public interface PointService {
    UserPoint chargePoints(long userId, long amount);
    UserPoint getPoints(long userId);
    UserPoint usePoints(long userId, long amount);
    List<PointHistory> chargeUsePoints(long userId);
}
