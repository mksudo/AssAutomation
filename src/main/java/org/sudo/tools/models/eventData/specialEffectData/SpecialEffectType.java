package org.sudo.tools.models.eventData.specialEffectData;

/**
 * Reference: <a href="https://github.com/Sekai-World/sekai-viewer/blob/dev/src/types.d.ts">...</a>
 * Reference Time: 2023-01-04
 */

public enum SpecialEffectType {
    NONE,
    BLACK_IN,
    BLACK_OUT,
    WHITE_IN,
    WHITE_OUT,
    SHAKE_SCREEN,
    SHAKE_WINDOW,
    CHANGE_BACKGROUND,
    // television opaque projector
    // used for location transition between scenes
    TELOP,
    FLASH_BACK_INT,
    FLASH_BACK_OUT,
    CHANGE_CARD_STILL,
    AMBIENT_COLOR_NORMAL,
    AMBIENT_COLOR_EVENING,
    AMBIENT_COLOR_NIGHT,
    PLAY_SCENARIO_EFFECT,
    STOP_SCENARIO_EFFECT,
    CHANGE_BACKGROUND_STILL,
    PLACE_INFO,
    MOVIE,
    SEKAI_IN,
    SEKAI_OUT,
    ATTACH_CHARACTER_SHADER,
    SIMPLE_SELECTABLE,
    FULL_SCREEN_TEXT,
    STOP_SHAKE_SCREEN,
    STOP_SHAKE_WINDOW
}
