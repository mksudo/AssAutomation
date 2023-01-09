package org.sudo.tools.models.eventData.snippet;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

/**
 * Reference: <a href="https://github.com/Sekai-World/sekai-viewer/blob/dev/src/types.d.ts">...</a>
 * Reference Time: 2023-01-04
 */

public enum SnippetActionType {
    NONE,
    TALK,
    CHARACTER_LAYOUT,
    INPUT_NAME,
    CHARACTER_MOTION,
    SELECTABLE,
    SPECIAL_EFFECT,
    SOUND,
    @JsonEnumDefaultValue
    UNKNOWN
}
