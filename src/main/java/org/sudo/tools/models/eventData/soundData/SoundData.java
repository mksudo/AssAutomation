package org.sudo.tools.models.eventData.soundData;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
@Getter
public class SoundData {
    @JsonProperty("PlayMode")
    int playMode;
    @JsonProperty("Bgm")
    String bgm;
    @JsonProperty("Se")
    String se;
    @JsonProperty("Volume")
    double volume;
    @JsonProperty("SeBundleName")
    String seBundleName;
    @JsonProperty("Duration")
    double duration;
}
