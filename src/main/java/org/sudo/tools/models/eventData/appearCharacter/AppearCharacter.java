package org.sudo.tools.models.eventData.appearCharacter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
@Getter
public class AppearCharacter {
    @JsonProperty("Character2dId")
    int character2DID;
    @JsonProperty("CostumeType")
    String costumeType;
}
