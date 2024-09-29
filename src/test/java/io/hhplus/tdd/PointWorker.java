package io.hhplus.tdd;

import io.hhplus.tdd.point.usecase.PointService;

import java.util.concurrent.CountDownLatch;

public class PointWorker implements Runnable {

    private final PointService pointService;
    private final long userId;
    private final long amount;
    private final boolean isCharge;
    private final CountDownLatch latch;

    public PointWorker(PointService pointService, long userId, long amount, boolean isCharge, CountDownLatch latch) {
        this.pointService = pointService;
        this.userId = userId;
        this.amount = amount;
        this.isCharge = isCharge;
        this.latch = latch;
    }

    @Override
    public void run() {
        try {
            if (isCharge) {
                pointService.chargePoints(userId, amount);
            } else {
                pointService.usePoints(userId, amount);
            }
        } finally {
            latch.countDown();
        }
    }
}