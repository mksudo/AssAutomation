package org.sudo.tools.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoSection {
    int startFrame;
    int endFrame;
    double startTimeStamp;
    double endTimeStamp;

    public VideoSection() {
        this.startFrame = -1;
        this.endFrame = -1;
        this.startTimeStamp = 0;
        this.endTimeStamp = 0;
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
    }

    @Override
    public String toString() {
        return String.format("{ startFrame = %d, endFrame = %d, startTimeStamp = %,.2f, endTimeStamp = %,.2f }", this.startFrame, this.endFrame, this.startTimeStamp, this.endTimeStamp);
    }
}