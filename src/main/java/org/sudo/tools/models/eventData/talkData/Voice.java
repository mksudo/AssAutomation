package org.sudo.tools.models.eventData.talkData;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
@Getter
public class Voice {
    @JsonProperty("Character2dId")
    int character2DID;
    @JsonProperty("VoiceId")
    String voiceId;
    @JsonProperty("Volume")
    double volume;
}
