package io.hhplus.tdd.point;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

public interface PointService {
    UserPoint chargePoints(long userId, long amount);
}
