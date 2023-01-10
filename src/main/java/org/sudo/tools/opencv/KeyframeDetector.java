package org.sudo.tools.opencv;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.sudo.tools.models.VideoSection;
import org.sudo.tools.models.scene.SceneTextSection;

import java.util.List;
import java.util.logging.Logger;

public class KeyframeDetector extends Detector {
    private static final Logger LOGGER = Logger.getLogger(KeyframeDetector.class.getName());
    private final List<SceneTextSection> textSections;
    private int currentTextSectionIndex;
    private int skipToFrame;

    public KeyframeDetector(String videoFileSrc, Mat templateImageGray, Rect templateRoi, Rect bannerRoi, List<SceneTextSection> textSections) {
        super(videoFileSrc, templateImageGray, templateRoi, bannerRoi);
        this.videoSectionIndex = 0;
        this.skipToFrame = 0;
        this.currentTextSectionIndex = 0;
        this.textSections = textSections;
        LOGGER.info(String.format("text section size %d", this.textSections.size()));
    }

    public List<VideoSection> call() throws Exception {
        VideoCapture stream = new VideoCapture(
                this.videoFileSrc
        );

        this.taskProgress.setTaskName("Key Frame Detector");

        if (!stream.isOpened()) {
            LOGGER.info("Cannot open video file");
            this.taskProgress.setError("Cannot open video file");
            stream.release();
            return this.videoSections;
        }

        double totalFrames = stream.get(Videoio.CAP_PROP_FRAME_COUNT);
        int fps = (int) Math.round(stream.get(Videoio.CAP_PROP_FPS));

        this.taskProgress.setTotalSteps((int) totalFrames);

        Mat frameImage = new Mat();

        this.videoSections.add(new VideoSection());

        for (int frameCounter = 0; stream.grab(); frameCounter++) {
            this.taskProgress.setCurrentStep(frameCounter + 1);
            this.frameDetector.setFrameCount(frameCounter);

            if (frameCounter < this.skipToFrame) continue;

            stream.retrieve(frameImage);

            this.frameDetector.detectFrame(frameImage);

            if (this.frameDetector.getTemplateArea() == null) {
                // Case 1
                // Case 1.1: hasBanner = true, lastHasBanner = true
                // banner keeps displaying, do nothing
                // Case 1.2: hasBanner = true, lastHashBanner = false
                // banner appears, set section begin
                // Case 1.3: hasBanner = false, lastHasBanner = true
                // banner disappears, set section end
                // Case 1.4: hasBanner = false, lastHasBanner = false
                // nothing is on screen, if last section does not end, end it here
                if (this.frameDetector.hasBanner()) {
                    if (!this.frameDetector.lastHasBanner()) {
                        LOGGER.info("case 1.2");

                        this.setVideoSectionStart(frameCounter);
                    }
                } else {
                    if (this.frameDetector.lastHasBanner()) {
                        LOGGER.info("case 1.3");

                        this.setVideoSectionEnd(frameCounter);
                        this.proceedToNextTextSection();
                    } else if (this.isLastVideoStartedButNotEnded()) {
                        LOGGER.info("case 1.4");

                        this.setVideoSectionEnd(frameCounter);
                        this.proceedToNextTextSection();
                    }
                }
            } else {
                // Case 2: hasTemplate = true, hasFirstCharacter = false, hasSecondCharacter = false
                // if last section does not end, end it here
                // Case 3: hasTemplate = true, hasFirstCharacter = true, hasSecondCharacter = false
                // start of sentence, set section begin, skip frames
                // Case 4: hasTemplate = true, hasFirstCharacter = true, hasSecondCharacter = true
                // sentence keeps displaying, do nothing
                if (this.frameDetector.hasFirstCharacter()) {
                    if (!this.frameDetector.hasSecondCharacter()) {
                        LOGGER.info("case 3");

                        if (this.currentTextSectionIndex < this.textSections.size()) {
                            if (this.isLastVideoStartedButNotEnded()) {
                                this.setVideoSectionEnd(frameCounter);
                                this.proceedToNextTextSection();
                            }

                            this.setVideoSectionStart(frameCounter);

                            if (this.currentTextSectionIndex > this.textSections.size() - 1)
                                continue;

                            this.skipToFrame = frameCounter + calculateTextToFrames(
                                    this.textSections
                                            .get(this.currentTextSectionIndex)
                                            .getText(),
                                    fps
                            );

                            LOGGER.info(
                                    String.format(
                                            "skipping from frame %d to frame %d, skipping %d frames, %d skips left",
                                            frameCounter,
                                            this.skipToFrame,
                                            this.skipToFrame - frameCounter,
                                            this.textSections.size() - this.currentTextSectionIndex
                                    )
                            );
                        } else {
                            LOGGER.warning("unmatched current text section index, possible alignment error");
                        }
                    }
                } else if (this.isLastVideoStartedButNotEnded()) {
                    LOGGER.info("case 2");

                    this.setVideoSectionEnd(frameCounter);
                    this.proceedToNextTextSection();
                }
            }
        }

        LOGGER.info("Task finished!");

        stream.release();

        return this.videoSections;
    }

    private static int calculateTextToFrames(String text, int fps) {
        String processedText = text.replace("\n", "");
        long textLength = processedText.codePoints().count();

        LOGGER.info(String.format("text length is %d", textLength));

        return (int) (Math.max(textLength - 2, textLength - 1) * (fps / 15.0));
    }

    private void proceedToNextTextSection() {
        this.currentTextSectionIndex++;

        LOGGER.info(String.format("proceed to text section %d", this.currentTextSectionIndex));
    }
}
