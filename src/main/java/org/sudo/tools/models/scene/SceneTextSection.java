package org.sudo.tools.models.scene;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
public class SceneTextSection {
    private SceneTextType type;
    private String text;
    private String characterName;
}
