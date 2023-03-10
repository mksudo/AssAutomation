package org.sudo.tools.opencv;

import lombok.Getter;
import lombok.Setter;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.sudo.tools.models.VideoSection;
import org.sudo.tools.models.scene.SceneTextSection;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.logging.Logger;

public class OpenCVDetector extends Thread {

    private static final Logger LOGGER = Logger.getLogger(OpenCVDetector.class.getName());
    private static final String TEMPLATE_FILE_SRC = "template.png";
    private static final int TEMPLATE_WIDTH = 1920;
    private static final int TEMPLATE_HEIGHT = 1440;
    private static final int THREAD_NUMBER = 6;
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(THREAD_NUMBER);
    @Getter
    private final List<Detector> detectors;
    @Getter
    private final List<VideoSection> videoSections;
    @Setter
    private Runnable taskFinishCallback;
    @Getter
    private double videoWidth;
    @Getter
    private double videoHeight;
    @Getter
    private double videoFps;
    @Setter
    @Getter
    private boolean useKeyframeDetector;
    @Setter
    private List<SceneTextSection> textSections;

    public OpenCVDetector() {
        this.detectors = new ArrayList<>();
        this.useKeyframeDetector = true;
        this.videoSections = new ArrayList<>();
    }

    public void prepareTasks(File videoFile) {
        this.detectors.clear();

        VideoCapture videoCapture = new VideoCapture(videoFile.getAbsolutePath());

        LOGGER.fine("video capture loaded for video file at " + videoFile.getAbsolutePath());

        double videoFrameCount = videoCapture.get(Videoio.CAP_PROP_FRAME_COUNT);
        this.videoWidth = videoCapture.get(Videoio.CAP_PROP_FRAME_WIDTH);
        this.videoHeight = videoCapture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
        this.videoFps = videoCapture.get(Videoio.CAP_PROP_FPS);

        LOGGER.fine(String.format("video information loaded, resolution: %,.2f x %,.2f, fps: %,.2f", this.videoWidth, this.videoHeight, this.videoFps));

        int videoSectionFrameCount = (int) (videoFrameCount / THREAD_NUMBER);

        double templateResizeRatio = videoWidth / TEMPLATE_WIDTH;

        LOGGER.fine(String.format("template resized by ratio %,.2f", templateResizeRatio));

        Mat templateImageGray = Imgcodecs.imread(TEMPLATE_FILE_SRC, Imgcodecs.IMREAD_GRAYSCALE);
        Size templateScaledSize = new Size(
                templateImageGray.size().width * templateResizeRatio,
                templateImageGray.size().height * templateResizeRatio
        );

        LOGGER.fine("template resize to size " + templateScaledSize);

        Mat templateImageGrayResized = new Mat();

        Imgproc.resize(templateImageGray, templateImageGrayResized, templateScaledSize);

        Rect templateRoi = new Rect(
                new Point(
                        this.videoWidth / 20,
                        this.videoHeight * 5 / 8
                ),
                new Size(
                        this.videoWidth / 4,
                        this.videoHeight / 4
                )
        );

        LOGGER.fine("template roi for current video is " + templateRoi);

        double bannerRoiWidth = this.videoWidth * 25.0 / 64.0;
        double bannerRoiHeight = this.videoHeight / 10.0;

        double bannerStartPointX = this.videoWidth * 39.0 / 128.0;
        double bannerStartPointY = this.videoHeight * 9.0 / 20.0;

        Rect bannerRoi = new Rect(
                new Point(bannerStartPointX, bannerStartPointY),
                new Size(bannerRoiWidth, bannerRoiHeight)
        );

        LOGGER.fine("transition roi for current video is " + bannerRoi);

        videoCapture.release();

        if (this.useKeyframeDetector) {
            this.detectors.add(
                    new KeyframeDetector(
                            videoFile.getAbsolutePath(),
                            templateImageGrayResized,
                            templateRoi, bannerRoi,
                            this.textSections
                    )
            );
        } else {
            for (int taskIndex = 0; taskIndex < THREAD_NUMBER; taskIndex++) {
                this.detectors.add(
                        new MultithreadingDetector(
                                taskIndex,
                                videoSectionFrameCount,
                                videoFile.getAbsolutePath(),
                                templateImageGrayResized.clone(),
                                templateRoi,
                                bannerRoi
                        )
                );
            }
        }
    }

    public void run() {
        try {
            LOGGER.info("running " + this.detectors.size() + " detectors");

            // this blocks until all futures are resolved
            var results = EXECUTOR.invokeAll(this.detectors);

            for (var result : results) {
                if (!result.isCancelled()) {
                    try {
                        var sections = result.get()
                                .stream()
                                .filter(Predicate.not(VideoSection::isPlaceHolder).and(Predicate.not(VideoSection::tooShort)))
                                .toList();
                        sections.forEach(section -> section.calculateTimeStamp(this.videoFps));
                        this.videoSections.addAll(sections);
                    } catch (ExecutionException exception) {
                        LOGGER.warning("ExecutionException while running task: " + exception);
                    } catch (InterruptedException exception) {
                        LOGGER.warning("InterruptedException error while running task" + exception);
                    }

                }
            }

            if (this.taskFinishCallback != null) {
                this.taskFinishCallback.run();
            }

        } catch (InterruptedException ignored) {
            LOGGER.warning("InterruptedException error while invoking tasks");
        }
    }
}
