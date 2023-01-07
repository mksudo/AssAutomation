package org.sudo.tools.models.eventData;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.sudo.tools.models.eventData.appearCharacter.AppearCharacter;
import org.sudo.tools.models.eventData.layoutData.LayoutData;
import org.sudo.tools.models.eventData.snippet.Snippet;
import org.sudo.tools.models.eventData.soundData.SoundData;
import org.sudo.tools.models.eventData.specialEffectData.SpecialEffectData;
import org.sudo.tools.models.eventData.talkData.TalkData;

import java.util.List;

@Jacksonized
@Builder
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventData {
    // m_GameObject
    // m_Enabled
    // m_Script
    // m_Name
    @JsonProperty("ScenarioId")
    String scenarioId;
    @JsonProperty("AppearCharacters")
    List<AppearCharacter> appearCharacters;
    // FirstLayout
    @JsonProperty("FirstBgm")
    String firstBgm;
    @JsonProperty("EpisodeMusicVideoId")
    String episodeMusicVideoId;
    @JsonProperty("FirstBackground")
    String firstBackground;
    @JsonProperty("FirstCharacterLayoutMode")
    int firstCharacterLayoutMode;
    @JsonProperty("Snippets")
    List<Snippet> snippets;
    @JsonProperty("TalkData")
    List<TalkData> talkData;
    @JsonProperty("LayoutData")
    List<LayoutData> layoutData;
    @JsonProperty("SpecialEffectData")
    List<SpecialEffectData> specialEffectData;
    @JsonProperty("SoundData")
    List<SoundData> soundData;
    @JsonProperty("NeedBundleNames")
    List<String> needBundleNames;
    // IncludeSoundDataBundleNames
    // ScenarioSnippetCharacterLayoutModes
}
