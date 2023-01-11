package org.sudo.tools.scene;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.sudo.tools.models.eventData.EventData;
import org.sudo.tools.models.eventData.snippet.Snippet;
import org.sudo.tools.models.eventData.snippet.SnippetActionType;
import org.sudo.tools.models.eventData.specialEffectData.SpecialEffectData;
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

    public SceneReader() {
        MAPPER.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);
    }

    public void read(File jsonFile) throws IOException {
        this.eventData = MAPPER.readValue(jsonFile, EventData.class);
        this.parseTextSections();
    }

    public void parseTextSections() {
        this.textSections = new ArrayList<>();

        for (int snippetIndex = 0; snippetIndex < this.eventData.getSnippets().size(); snippetIndex++) {
            Snippet snippet = this.eventData.getSnippets().get(snippetIndex);
            Snippet nextSnippet = null;
            if (snippetIndex + 1 < this.eventData.getSnippets().size()) {
                nextSnippet = this.eventData.getSnippets().get(snippetIndex + 1);
            }
            SpecialEffectData nextSpecialEffectData = null;
            if (nextSnippet != null && nextSnippet.getAction() == SnippetActionType.SPECIAL_EFFECT) {
                nextSpecialEffectData = this.eventData.getSpecialEffectData().get(nextSnippet.getReferenceIndex());
            }

            switch (snippet.getAction()) {
                case TALK -> {
                    var talkData = this.eventData
                            .getTalkData()
                            .get(snippet.getReferenceIndex());

                    SceneTextSection textSection = new SceneTextSection(
                            SceneTextType.CONVERSATION,
                            talkData.getBody(),
                            talkData.getWindowDisplayName()
                    );

                    if (nextSpecialEffectData != null) {
                        if (
                                nextSpecialEffectData.getEffectType() == SpecialEffectType.SHAKE_WINDOW ||
                                        nextSpecialEffectData.getEffectType() == SpecialEffectType.SHAKE_SCREEN
                        ) {
                            textSection.setShaking(true);
                        }
                    }

                    this.textSections.add(textSection);
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
        }
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
