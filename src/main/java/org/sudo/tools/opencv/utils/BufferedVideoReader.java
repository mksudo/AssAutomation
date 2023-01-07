package org.sudo.tools.opencv.utils;

import lombok.Getter;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.util.concurrent.LinkedBlockingQueue;

public class BufferedVideoReader extends Thread {
    private final String videoFileSrc;
    private final LinkedBlockingQueue<Mat> frames;
    @Getter
    private VideoCapture stream;
    private final int frameStart;
    private final int frameCount;

    public BufferedVideoReader(String videoFileSrc, LinkedBlockingQueue<Mat> frames) {
        this.frames = frames;
        this.videoFileSrc = videoFileSrc;
        this.frameStart = 0;
        this.frameCount = Integer.MAX_VALUE;
    }

    public BufferedVideoReader(String videoFileSrc, LinkedBlockingQueue<Mat> frames, int frameStart, int frameCount) {
        this.frames = frames;
        this.videoFileSrc = videoFileSrc;
        this.frameStart = frameStart;
        this.frameCount = frameCount;
        this.stream = new VideoCapture(
                videoFileSrc,
                Videoio.CAP_FFMPEG,
                new MatOfInt(Videoio.CAP_PROP_HW_ACCELERATION, Videoio.VIDEO_ACCELERATION_ANY)
        );
        this.stream.set(Videoio.CAP_PROP_POS_FRAMES, this.frameStart);
    }

    @Override
    public void run() {
        Mat frame = new Mat();

        int frameCounter = 0;

        try {
            while (
                    frameCounter < this.frameCount &&
                            this.stream.isOpened() &&
                            this.stream.read(frame)
            ) {
                this.frames.put(frame);
                frameCounter++;
            }
            this.frames.put(new Mat());
            this.stream.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
