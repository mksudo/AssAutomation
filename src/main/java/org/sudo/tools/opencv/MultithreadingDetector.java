package org.sudo.tools.opencv;


import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.sudo.tools.models.VideoSection;
import org.sudo.tools.opencv.utils.BufferedVideoReader;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class MultithreadingDetector extends Detector {
    private int videoSectionFrameCount;
    private final LinkedBlockingQueue<Mat> frames;

    public MultithreadingDetector(
            int videoSectionIndex,
            int videoSectionFrameCount,
            String videoFileSrc,
            Mat templateImageGray,
            Rect templateRoi,
            Rect bannerRoi
    ) {
        super(videoFileSrc, templateImageGray, templateRoi, bannerRoi);
        this.frames = new LinkedBlockingQueue<>(256);
        this.videoSectionIndex = videoSectionIndex;
        this.videoSectionFrameCount = videoSectionFrameCount;
    }

    @Override
    public List<VideoSection> call() throws Exception {
        int startFrame = this.videoSectionIndex * this.videoSectionFrameCount;

        this.taskProgress.setTaskName("Multithreading Detector " + this.videoSectionIndex);

        BufferedVideoReader stream = new BufferedVideoReader(
                this.videoFileSrc,
                this.frames,
                startFrame,
                this.videoSectionFrameCount
        );

        stream.start();

        if (!stream.getStream().isOpened()) {
            System.out.println("Cannot open video file");
            this.taskProgress.setError("Cannot open video file");
            return this.videoSections;
        }

        this.taskProgress.setTotalSteps(this.videoSectionFrameCount);

        Rect templateArea = null;

        this.videoSections.add(new VideoSection());

        Mat frameImage;

        for (int frameCounter = 0; frameCounter < this.videoSectionFrameCount; frameCounter++) {
            frameImage = this.frames.take();

            if (frameImage.empty()) {
                break;
            }

            this.frameDetector.detectFrame(frameImage);

            if (this.frameDetector.getTemplateArea() == null) {
                if (this.frameDetector.hasBanner()) {
                    this.setVideoSectionStart(startFrame + frameCounter);
                } else {
                    VideoSection lastSection = this.videoSections.get(this.videoSections.size() - 1);
                    if (lastSection.hasStart() && !lastSection.hasEnd()) {
                        lastSection.setEndFrame(startFrame + frameCounter);
                    }
                }
            } else {
                if (this.frameDetector.hasFirstCharacter()) {
                    if (!this.frameDetector.hasSecondCharacter()) {
                        this.setVideoSectionStart(startFrame + frameCounter);
                    }
                } else {
                    this.setVideoSectionEnd(frameCounter);
                }
            }

            if (frameCounter % 50 == 0) {
                this.taskProgress.setCurrentStep(frameCounter + 1);
            }
        }

        System.out.printf("Task %d => finished\n", this.videoSectionIndex);

        this.taskProgress.setCurrentStep(this.videoSectionFrameCount);

        VideoSection lastSection = this.videoSections.get(this.videoSections.size() - 1);

        if (lastSection.hasStart() && !lastSection.hasEnd()) {
            lastSection.setEndFrame((this.videoSectionIndex + 1) * this.videoSectionFrameCount);
        }

        return this.videoSections;
    }
}
