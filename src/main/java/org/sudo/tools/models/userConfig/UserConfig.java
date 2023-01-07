package org.sudo.tools.models.userConfig;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.ArrayList;

@Jacksonized
@Builder
public record UserConfig(
        String resolutionMaskConfigFilePath,
        ArrayList<String> resolutionMaskConfigFileNames,
        String sampleAssFilePath,
        boolean debug
) {
}
