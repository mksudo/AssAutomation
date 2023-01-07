package org.sudo.tools.models.eventData.layoutData;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
@Getter
public class LayoutData {
    @JsonProperty("Type")
    int type;
    @JsonProperty("SideFrom")
    int sideFrom;
    @JsonProperty("SideFromOffsetX")
    double sideFromOffsetX;
    @JsonProperty("SideTo")
    int sideTo;
    @JsonProperty("SideToOffsetX")
    double sideToOffsetX;
    @JsonProperty("DepthType")
    int depthType;
    @JsonProperty("Character2dId")
    int character2DID;
    @JsonProperty("CostumeType")
    String costumeType;
    @JsonProperty("MotionName")
    String motionName;
    @JsonProperty("FacialName")
    String facialName;
    @JsonProperty("MoveSpeedType")
    int moveSpeedType;
}
