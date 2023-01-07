package org.sudo.tools.models.eventData.talkData;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
@Getter
public class Motion {
    @JsonProperty("Character2dId")
    int character2DID;
    @JsonProperty("MotionName")
    String motionName;
    @JsonProperty("FacialName")
    String facialName;
    @JsonProperty("TimingSyncValue")
    double timingSyncValue;
}
