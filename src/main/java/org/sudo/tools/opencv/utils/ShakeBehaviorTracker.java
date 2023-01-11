package org.sudo.tools.opencv.utils;

import lombok.Getter;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

public class ShakeBehaviorTracker {
    private List<KeyPoint> keyPoints;

    public ShakeBehaviorTracker() {
        this.keyPoints = new ArrayList<>();
    }

    @Getter
    private static class KeyPoint {
        private final Point point;
        private final int frameCount;
        private double timestamp;

        public KeyPoint(Point point, int frameCount) {
            this.point = point;
            this.frameCount = frameCount;
        }

        public void calculateTimestamp(double fps) {
            this.timestamp = this.frameCount / fps;
        }
    }

    public void track(Point point, int frameCount) {
        if (this.keyPoints.size() == 0) {
            this.keyPoints.add(new KeyPoint(point, frameCount));
            return;
        }

        KeyPoint lastKeyPoint = this.keyPoints.get(this.keyPoints.size() - 1);

        if (isSameKeyPoint(point, lastKeyPoint.getPoint())) {
            return;
        }

        this.keyPoints.add(new KeyPoint(point, frameCount));
    }

    private static boolean isSameKeyPoint(Point p1, Point p2) {
        return Math.abs(p1.x - p2.x) < 2 && Math.abs(p1.y - p2.y) < 2;
    }

    public void calculateTimestamps(double fps) {
        for (var keyPoint : this.keyPoints) {
            keyPoint.calculateTimestamp(fps);
        }
    }

    public boolean isBehaviorTracked() {
        return this.keyPoints.size() > 1;
    }
}
