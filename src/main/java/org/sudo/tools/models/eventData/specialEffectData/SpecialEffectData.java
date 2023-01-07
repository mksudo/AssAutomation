package org.sudo.tools.models.eventData.specialEffectData;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
@Getter
public class SpecialEffectData {
    @JsonProperty("EffectType")
    SpecialEffectType effectType;
    @JsonProperty("StringVal")
    String stringVal;
    @JsonProperty("StringValSub")
    String stringValSub;
    @JsonProperty("Duration")
    double duration;
    @JsonProperty("IntVal")
    int intVal;
}
