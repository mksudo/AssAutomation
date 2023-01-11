package org.sudo.tools.models.scene;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
public class SceneTextSection {
    private SceneTextType type;
    private String text;
    private String characterName;
    @Setter
    private boolean isShaking;

    public SceneTextSection(SceneTextType type, String text, String characterName) {
        this.type = type;
        this.text = text;
        this.characterName = characterName;
        this.isShaking = false;
    }
}
