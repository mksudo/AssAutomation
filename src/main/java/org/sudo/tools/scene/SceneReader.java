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

                    Snippet nextSnippet;
                    SpecialEffectData nextSpecialEffectData;

                    for (
                            int nextSnippetIndex = snippetIndex + 1;
                            nextSnippetIndex < this.eventData.getSnippets().size() && !textSection.isShaking();
                            nextSnippetIndex++
                    ) {
                        nextSnippet = this.eventData.getSnippets().get(nextSnippetIndex);

                        if (nextSnippet.getAction() == SnippetActionType.TALK) {
                            break;
                        }

                        if (nextSnippet.getAction() != SnippetActionType.SPECIAL_EFFECT) {
                            continue;
                        }

                        nextSpecialEffectData = this.eventData
                                .getSpecialEffectData()
                                .get(nextSnippet.getReferenceIndex());

                        if (
                                nextSpecialEffectData.getEffectType() == SpecialEffectType.SHAKE_WINDOW
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
