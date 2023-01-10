package org.sudo.tools.ass;

import org.sudo.tools.ConfigProvider;
import org.sudo.tools.models.AssEvent;
import org.sudo.tools.models.VideoSection;
import org.sudo.tools.models.eventData.EventData;
import org.sudo.tools.models.scene.SceneTextSection;
import org.sudo.tools.models.userConfig.ResolutionMaskConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class AssWriter {
    private static final Logger LOGGER = Logger.getLogger(AssWriter.class.getName());
    private EventData eventData;
    private List<VideoSection> videoSections;
    private List<SceneTextSection> textSections;
    private List<AssEvent> assEvents;
    private File videoFile;
    private int videoWidth;
    private int videoHeight;

    public AssWriter(
            File videoFile,
            EventData eventData,
            List<SceneTextSection> textSections,
            List<VideoSection> videoSections,
            double videoWidth,
            double videoHeight
    ) {
        this.eventData = eventData;
        this.videoSections = videoSections;
        this.textSections = textSections;
        this.assEvents = new ArrayList<>();
        this.videoFile = videoFile;
        this.videoWidth = (int) videoWidth;
        this.videoHeight = (int) videoHeight;
    }

    public void analyse() {
        if (this.textSections.size() != this.videoSections.size()) {
            LOGGER.warning(
                    String.format(
                            "mismatching section size, scene section size %d, video section size %d",
                            this.textSections.size(),
                            this.videoSections.size()
                    )
            );

            if (this.textSections.size() < this.videoSections.size()) {
                LOGGER.warning(
                        "extra video sections: " +
                                Arrays.toString(
                                        this.videoSections
                                                .subList(
                                                        this.textSections.size(),
                                                        this.videoSections.size()
                                                )
                                                .toArray()
                                )
                );
            } else {
                LOGGER.warning(
                        "extra text sections: " +
                                Arrays.toString(
                                        this.textSections
                                                .subList(
                                                        this.videoSections.size(),
                                                        this.textSections.size()
                                                )
                                                .toArray()
                                )
                );
            }
        }

        ResolutionMaskConfig resolutionMaskConfig = this.getResolutionMaskConfig();

        String textMask = "";
        String locationTextMask = "";
        String locationTextStyle = "";

        if (resolutionMaskConfig != null) {
            textMask = resolutionMaskConfig.textMask();
            locationTextMask = resolutionMaskConfig.locationTextMask();
            locationTextStyle = resolutionMaskConfig.locationTextStyle();
        }

        int alignedSize = Math.min(this.textSections.size(), this.videoSections.size());

        this.parseCommentAssEvents(alignedSize, textMask, locationTextMask);

        this.parseDialogueAssEvents(alignedSize, locationTextStyle);


    }

    private void parseDialogueAssEvents(int alignedSize, String locationTextStyle) {
        for (int alignedIndex = 0; alignedIndex < alignedSize; alignedIndex++) {
            VideoSection videoSection = this.videoSections.get(alignedIndex);
            SceneTextSection textSection = this.textSections.get(alignedIndex);

            switch (textSection.getType()) {
                case TRANSITION -> {
                    AssEvent dialogue = AssEvent.builder()
                            .labelName("Dialogue")
                            .startTimeStamp(videoSection.getStartTimeStamp())
                            .endTimeStamp(videoSection.getEndTimeStamp())
                            .style("screen")
                            .name("transition")
                            .text(locationTextStyle + textSection.getText())
                            .build();
                    this.assEvents.add(dialogue);
                }
                case CONVERSATION -> {
                    String characterStyle = getCharacterStyle(textSection.getCharacterName());
                    AssEvent dialogue = AssEvent.builder()
                            .labelName("Dialogue")
                            .startTimeStamp(videoSection.getStartTimeStamp())
                            .endTimeStamp(videoSection.getEndTimeStamp())
                            .style(characterStyle)
                            .name(textSection.getCharacterName())
                            .text(textSection.getText().replaceAll("\n", "\\\\n"))
                            .build();
                    this.assEvents.add(dialogue);
                }
            }
        }
    }

    private void parseCommentAssEvents(int alignedSize, String textMask, String locationTextMask) {
        VideoSection maskStartSection = null, maskEndSection = null;
        SceneTextSection maskStartTextSection = null;

        for (int alignedIndex = 0; alignedIndex < alignedSize; alignedIndex++) {
            VideoSection videoSection = this.videoSections.get(alignedIndex);
            SceneTextSection textSection = this.textSections.get(alignedIndex);

            if (maskStartSection == null) {
                maskStartSection = videoSection;
                maskEndSection = videoSection;
                maskStartTextSection = textSection;
                continue;
            }

            if (
                    videoSection.getStartFrame() - maskEndSection.getEndFrame() < 5 &&
                            textSection.getType() == maskStartTextSection.getType()
            ) {
                maskEndSection = videoSection;
                if (alignedIndex < alignedSize - 1) {
                    continue;
                }
            }

            switch (maskStartTextSection.getType()) {
                case TRANSITION -> {
                    AssEvent comment = AssEvent.builder()
                            .labelName("Comment")
                            .startTimeStamp(maskStartSection.getStartTimeStamp())
                            .endTimeStamp(maskEndSection.getEndTimeStamp())
                            .name("screen")
                            .style("screen")
                            .text(locationTextMask.isEmpty() ? "TODO: fill location text mask" : locationTextMask)
                            .build();
                    this.assEvents.add(comment);
                }
                case CONVERSATION -> {
                    AssEvent comment = AssEvent.builder()
                            .labelName("Comment")
                            .startTimeStamp(maskStartSection.getStartTimeStamp())
                            .endTimeStamp(maskEndSection.getEndTimeStamp())
                            .name("screen")
                            .style("screen")
                            .text(textMask.isEmpty() ? "TODO: fill conversation text mask" : textMask)
                            .build();
                    this.assEvents.add(comment);
                }
            }

            maskStartSection = videoSection;
            maskEndSection = videoSection;
            maskStartTextSection = textSection;
        }
    }

    public void write() throws IOException {
        Path videoFileParent = Path.of(this.videoFile.getAbsolutePath()).getParent();

        String videoFileName = this.videoFile.getName();
        int extensionIndex = videoFileName.lastIndexOf('.');

        if (extensionIndex == -1) {
            LOGGER.warning("cannot get filename for video file, please check if video filename has proper format!");
            return;
        }

        String videoFileNameWithoutExtension = videoFileName.substring(0, extensionIndex);

        Path assFilePath = videoFileParent == null ?
                Path.of(videoFileNameWithoutExtension + ".ass") :
                Path.of(videoFileParent.toString(), videoFileNameWithoutExtension + ".ass");

        Files.writeString(
                assFilePath,
                getSampleAssFileContent() +
                        this.assEvents
                                .stream()
                                .map(AssEvent::toString)
                                .collect(Collectors.joining("\n")),
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    private static String getSampleAssFileContent() {
        var userConfig = ConfigProvider.getInstance().getUserConfig();

        if (userConfig == null) return "";

        String sampleAssSrc = userConfig.sampleAssFilePath();

        if (sampleAssSrc == null || sampleAssSrc.isBlank()) return "";

        try {
            return Files.readString(Path.of(sampleAssSrc));
        } catch (IOException exception) {
            LOGGER.warning("IOException when reading sample ass file: " + exception.getMessage());
            return "";
        }
    }

    private ResolutionMaskConfig getResolutionMaskConfig() {
        var resolutionMaskConfigs = ConfigProvider.getInstance().getResolutionMaskConfigs();

        if (resolutionMaskConfigs == null) return null;

        return resolutionMaskConfigs
                .stream()
                .filter(
                        resolutionMaskConfig ->
                                resolutionMaskConfig.resolutionX() == this.videoWidth &&
                                        resolutionMaskConfig.resolutionY() == this.videoHeight
                )
                .findFirst()
                .orElse(null);
    }

    private static String getCharacterStyle(String characterName) {
        String defaultName = "screen";

        var characterStyleConfig = ConfigProvider.getInstance().getCharacterStyleConfig();

        if (characterStyleConfig == null) {
            return defaultName;
        }

        defaultName = characterStyleConfig.getOrDefault("default", defaultName);

        return characterStyleConfig.getOrDefault(characterName, defaultName);
    }
}
