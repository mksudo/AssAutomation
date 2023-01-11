package org.sudo.tools.models;

import lombok.Getter;
import lombok.Setter;
import org.opencv.core.Point;
import org.sudo.tools.opencv.utils.ShakeBehaviorTracker;

import java.util.ArrayList;

@Getter
@Setter
public class VideoSection {
    private int startFrame;
    private int endFrame;
    private double startTimeStamp;
    private double endTimeStamp;
    private ShakeBehaviorTracker shakeBehaviorTracker;

    public VideoSection() {
        this.startFrame = -1;
        this.endFrame = -1;
        this.startTimeStamp = 0;
        this.endTimeStamp = 0;
        this.shakeBehaviorTracker = new ShakeBehaviorTracker();
    }

    public boolean hasStart() {
        return this.startFrame != -1;
    }

    public boolean hasEnd() {
        return this.endFrame != -1;
    }

    public boolean isPlaceHolder() {
        return this.startFrame == -1 && this.endFrame == -1;
    }

    public void calculateTimeStamp(double fps) {
        this.startTimeStamp = this.startFrame / fps;
        this.endTimeStamp = this.endFrame / fps;

        this.shakeBehaviorTracker.calculateTimestamps(fps);
    }

    public boolean tooShort() {
        return this.endFrame - this.startFrame < 10;
    }

    @Override
    public String toString() {
        return String.format("{ startFrame = %d, endFrame = %d, startTimeStamp = %,.2f, endTimeStamp = %,.2f }", this.startFrame, this.endFrame, this.startTimeStamp, this.endTimeStamp);
    }
}