package org.sudo.tools.models.eventData.talkData;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Jacksonized
@Builder
@Getter
public class TalkData {
    @JsonProperty("TalkCharacters")
    List<TalkCharacter> talkCharacters;
    @JsonProperty("WindowDisplayName")
    String windowDisplayName;
    @JsonProperty("Body")
    String body;
    @JsonProperty("TalkTention")
    int talkTension;
    @JsonProperty("LipSync")
    int lipSync;
    @JsonProperty("MotionChangeFrom")
    int motionChangeFrom;
    @JsonProperty("Motions")
    List<Motion> motions;
    @JsonProperty("Voices")
    List<Voice> voices;
    @JsonProperty("Speed")
    double speed;
    @JsonProperty("FontSize")
    int fontSize;
    @JsonProperty("WhenFinishCloseWindow")
    int whenFinishCloseWindow;
    @JsonProperty("RequirePlayEffect")
    int requirePlayEffect;
    @JsonProperty("EffectReferenceIdx")
    int effectReferenceIdx;
    @JsonProperty("RequirePlaySound")
    int requirePlaySound;
    @JsonProperty("SoundReferenceIdx")
    int soundReferenceIdx;
    @JsonProperty("TargetValueScale")
    double targetValueScale;
}
