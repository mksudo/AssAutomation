package org.sudo.tools.models.eventData.snippet;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum SnippetProgressBehavior {
    NOW,
    WAIT_UNTIL_FINISHED,
    @JsonEnumDefaultValue
    UNKNOWN
}
