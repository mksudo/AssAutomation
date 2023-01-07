package org.sudo.tools.opencv.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class FrameDetector {
    private static final Logger LOGGER = Logger.getLogger(FrameDetector.class.getName());
    private final Mat templateImageGray;
    private final Rect templateRoi;
    private final Rect bannerRoi;
    @Getter
    private Rect templateArea;
    @Accessors(fluent = true)
    @Getter
    private boolean hasBanner;
    @Accessors(fluent = true)
    @Getter
    private boolean lastHasBanner;
    @Accessors(fluent = true)
    @Getter
    private boolean hasFirstCharacter;
    @Accessors(fluent = true)
    @Getter
    private boolean hasSecondCharacter;
    @Getter
    @Setter
    private int frameCount;

    public FrameDetector(Mat templateImageGray, Rect templateRoi, Rect bannerRoi) {
        this.templateImageGray = templateImageGray;
        this.templateRoi = templateRoi;
        this.bannerRoi = bannerRoi;
        this.templateArea = null;
        this.hasBanner = false;
        this.lastHasBanner = false;
        this.hasFirstCharacter = false;
        this.hasSecondCharacter = false;
        this.frameCount = 0;
    }

    public void detectFrame(Mat frameImage) throws Exception {
        // from last detection
        if (this.templateArea != null) {
            this.templateArea = detectTemplate(frameImage, this.templateImageGray, new Rect(
                    templateArea.x - 20, templateArea.y - 20,
                    this.templateImageGray.width() + 20, this.templateImageGray.height() + 20
            ));
        }

        // fast match failed, use full match
        if (this.templateArea == null) {
            this.templateArea = detectTemplate(frameImage, this.templateImageGray, this.templateRoi);
        }

        // full match failed, template not on screen
        if (this.templateArea == null) {
            // try to find banner
            this.lastHasBanner = this.hasBanner;
            this.hasBanner = detectHasBanner(frameImage, this.bannerRoi);
            this.hasFirstCharacter = false;
            this.hasSecondCharacter = false;
        } else {
            this.lastHasBanner = this.hasBanner;
            this.hasBanner = false;
            this.hasFirstCharacter = detectHasCharacter(frameImage, new Rect(
                    new Point(
                            this.templateArea.x,
                            this.templateArea.y + templateArea.height * 2
                    ),
                    this.templateArea.size()
            ));
            this.hasSecondCharacter = detectHasCharacter(frameImage, new Rect(
                    new Point(
                            this.templateArea.x + this.templateArea.width,
                            this.templateArea.y + this.templateArea.height * 2
                    ),
                    new Size(
                            this.templateArea.size().width * 2.1,
                            this.templateArea.size().height
                    )
            ));
        }


        LOGGER.fine(
                String.format(
                        "Frame %d => lastHasBanner: %b, hasBanner: %b, hasFirstCharacter: %b, hasSecondCharacter: %b\n",
                        frameCount,
                        lastHasBanner,
                        hasBanner,
                        hasFirstCharacter,
                        hasSecondCharacter
                )
        );
    }

    public static boolean detectHasBanner(Mat frame, Rect roi) {
        Mat frameRoi = frame.submat(roi);
        Mat frameRoiHSV = new Mat();

        Imgproc.cvtColor(frameRoi, frameRoiHSV, Imgproc.COLOR_BGR2HSV);

        int whiteSensitivity = 15;

        Mat mask = new Mat();
        Mat maskedRoi = new Mat();
        // extract mask from roi
        Core.inRange(
                frameRoiHSV,
                new Scalar(0, 0, 255 - whiteSensitivity),
                new Scalar(255, whiteSensitivity, 255),
                mask
        );

        Core.bitwise_and(frameRoiHSV, frameRoiHSV, maskedRoi, mask);

        ArrayList<Mat> hsv = new ArrayList<>(3);
        // split out v channel, use that as a grayscale image
        Core.split(maskedRoi, hsv);
        // hopefully this kernel is big enough to eliminate spaces between text segments
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(20, 2));
        Mat dilatedRoi = new Mat();
        Imgproc.dilate(hsv.get(2), dilatedRoi, kernel, new Point(-1, -1), 5);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(dilatedRoi, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        if (contours.size() != 1) {
            return false;
        }

        MatOfPoint contour = contours.get(0);
        Rect boundingRect = Imgproc.boundingRect(contour);

        Rect bannerTextArea = new Rect(
                new Point(0, roi.height / 4.0),
                new Size(roi.width, roi.height / 2.0)
        );

        if (boundingRect.height < bannerTextArea.height / 2.0) {
            return false;
        }

        double bannerTextAreaMidpointX = bannerTextArea.width / 2.0;
        double boundingRectMidpointX = boundingRect.x + boundingRect.width / 2.0;

        if (Math.sqrt(Math.pow(bannerTextAreaMidpointX - boundingRectMidpointX, 2.0)) > 20.0) {
            return false;
        }

        return bannerTextArea.contains(boundingRect.tl()) && bannerTextArea.contains(boundingRect.br());
    }

    public boolean detectHasCharacter(Mat frame, Rect roi) throws Exception {
        Mat frameRoi = frame.submat(roi);
        Mat frameImageGray = new Mat();
        Imgproc.cvtColor(frameRoi, frameImageGray, Imgproc.COLOR_BGR2GRAY);

        MatOfDouble mean = new MatOfDouble();
        MatOfDouble standardDeviation = new MatOfDouble();

        Core.meanStdDev(frameImageGray, mean, standardDeviation);

        double standardDeviationValue = standardDeviation.get(0, 0)[0] / 255.0;

        if (standardDeviationValue < 0.11) return false;

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2));
        Mat dilatedRoi = new Mat();

        Imgproc.dilate(frameImageGray, dilatedRoi, kernel, new Point(-1, -1), 5);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(dilatedRoi, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        return contours.size() > 0;
    }

    public static Rect detectTemplate(Mat frame, Mat templateGray, Rect roi) {
        Rect frameRect = null;
        Mat frameRoi = frame.submat(roi);
        Mat frameRoiGray = new Mat();

        Imgproc.cvtColor(frameRoi, frameRoiGray, Imgproc.COLOR_BGR2GRAY);

        Mat result = new Mat();

        Imgproc.matchTemplate(frameRoiGray, templateGray, result, Imgproc.TM_CCOEFF_NORMED);

        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);

        double matchVal = mmr.maxVal;
        Point matchLoc = mmr.maxLoc;

        if (matchVal > 0.9) {
            Point frameLoc = new Point(
                    matchLoc.x + roi.x,
                    matchLoc.y + roi.y
            );

            frameRect = new Rect(frameLoc, templateGray.size());
        }

        frameRoiGray.release();
        result.release();

        return frameRect;
    }
}
