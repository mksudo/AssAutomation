package org.sudo.tools.models.eventData.snippet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
@Getter
public class Snippet {
    @JsonProperty("Index")
    int index;
    @JsonProperty("Action")
    SnippetActionType action;
    @JsonProperty("ProgressBehavior")
    SnippetProgressBehavior progressBehavior;
    @JsonProperty("ReferenceIndex")
    int referenceIndex;
    @JsonProperty("Delay")
    double delay;
}
