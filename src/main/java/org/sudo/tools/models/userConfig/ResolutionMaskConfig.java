package org.sudo.tools.models.userConfig;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
public record ResolutionMaskConfig(
        int resolutionX,
        int resolutionY,
        String textMask,
        String locationTextStyle,
        String locationTextMask
) {
}
