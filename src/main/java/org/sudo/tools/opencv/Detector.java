package org.sudo.tools.opencv;

import lombok.Getter;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.sudo.tools.models.VideoSection;
import org.sudo.tools.opencv.utils.FrameDetector;
import org.sudo.tools.utils.TaskProgress;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public abstract class Detector implements Callable<List<VideoSection>> {
    @Getter
    protected int videoSectionIndex;
    protected final String videoFileSrc;
    protected final FrameDetector frameDetector;
    @Getter
    protected final TaskProgress taskProgress;
    @Getter
    protected final List<VideoSection> videoSections;

    public Detector(String videoFileSrc, Mat templateImageGray, Rect templateRoi, Rect bannerRoi) {
        this.videoFileSrc = videoFileSrc;
        this.frameDetector = new FrameDetector(templateImageGray, templateRoi, bannerRoi);
        this.taskProgress = new TaskProgress();
        this.videoSections = new ArrayList<>();
    }

    protected VideoSection getLastVideoSection() {
        return this.videoSections.get(this.videoSections.size() - 1);
    }

    protected boolean setVideoSectionStart(int frame) {
        VideoSection lastVideoSection = this.getLastVideoSection();
        boolean hasStartFrame = lastVideoSection.hasStart();
        lastVideoSection.setStartFrame(frame);
        return hasStartFrame;
    }

    protected boolean setVideoSectionEnd(int frame) {
        VideoSection lastVideoSection = this.getLastVideoSection();

        if (this.isLastVideoStartedButNotEnded()) {
            lastVideoSection.setEndFrame(frame);
            if (lastVideoSection.tooShort()) {
                // false detection
                lastVideoSection.setStartFrame(-1);
                lastVideoSection.setEndFrame(-1);
                return false;
            } else {
                this.videoSections.add(new VideoSection());
                return true;
            }
        }

        return false;
    }

    protected boolean isLastVideoStartedButNotEnded() {
        VideoSection lastVideoSection = this.getLastVideoSection();
        return lastVideoSection.hasStart() && !lastVideoSection.hasEnd();
    }
}
