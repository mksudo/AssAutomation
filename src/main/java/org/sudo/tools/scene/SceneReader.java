package org.sudo.tools.scene;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.sudo.tools.models.eventData.EventData;
import org.sudo.tools.models.eventData.specialEffectData.SpecialEffectType;
import org.sudo.tools.models.scene.SceneTextSection;
import org.sudo.tools.models.scene.SceneTextType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SceneReader {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    @Getter
    private EventData eventData;
    @Getter
    private List<SceneTextSection> textSections;

    public void read(File jsonFile) throws IOException {
        this.eventData = MAPPER.readValue(jsonFile, EventData.class);
        this.parseTextSections();
    }

    public void parseTextSections() {
        this.textSections = new ArrayList<>();
        this.eventData.getSnippets().forEach(snippet -> {
            switch (snippet.getAction()) {
                case TALK -> {
                    var talkData = this.eventData
                            .getTalkData()
                            .get(snippet.getReferenceIndex());

                    this.textSections.add(
                            new SceneTextSection(
                                    SceneTextType.CONVERSATION,
                                    talkData.getBody(),
                                    talkData.getWindowDisplayName()
                            )
                    );
                }
                case SPECIAL_EFFECT -> {
                    var specialEffectData = this.eventData
                            .getSpecialEffectData()
                            .get(snippet.getReferenceIndex());

                    if (specialEffectData.getEffectType() == SpecialEffectType.TELOP) {
                        this.textSections.add(
                                new SceneTextSection(
                                        SceneTextType.TRANSITION,
                                        specialEffectData.getStringVal(),
                                        ""
                                )
                        );
                    }
                }
            }
        });
    }

    public List<SceneTextSection> getNoNewLineTextSections() {
        return this.textSections
                .stream()
                .map(section ->
                        new SceneTextSection(
                                section.getType(),
                                section.getText()
                                        .replaceAll("\n", ""),
                                section.getCharacterName()
                        )
                )
                .toList();
    }
}
