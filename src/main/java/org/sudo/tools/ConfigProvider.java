package org.sudo.tools;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import lombok.Getter;
import org.sudo.tools.models.userConfig.ResolutionMaskConfig;
import org.sudo.tools.models.userConfig.UserConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ConfigProvider {
    private static final Logger LOGGER = Logger.getLogger(ConfigProvider.class.getName());
    private static final ObjectMapper MAPPER = new TomlMapper();
    private static final String USER_CONFIG_PATH = "settings.toml";
    private static final String CHARACTER_STYLE_CONFIG_PATH = "characterStyles.toml";
    private static final ConfigProvider instance = new ConfigProvider();

    public static ConfigProvider getInstance() {
        return instance;
    }

    @Getter
    private UserConfig userConfig;
    @Getter
    private List<ResolutionMaskConfig> resolutionMaskConfigs;
    @Getter
    private Map<String, String> characterStyleConfig;

    private ConfigProvider() {
        this.loadUserConfig();
        this.loadResolutionMaskConfigs();
        this.loadCharacterStyleConfig();
    }

    private void loadUserConfig() {
        this.userConfig = loadTomlFile(new File(USER_CONFIG_PATH), UserConfig.class);
    }


    private void loadResolutionMaskConfigs() {
        if (this.userConfig == null) {
            LOGGER.warning("user config is null, skipping resolution mask config loading");
            this.resolutionMaskConfigs = null;
            return;
        }

        if (this.userConfig.resolutionMaskConfigFilePath() == null) {
            LOGGER.warning("resolution mask config path is null, skipping resolution mask config loading");
            this.resolutionMaskConfigs = null;
            return;
        }

        this.resolutionMaskConfigs = new ArrayList<>();

        for (var resolutionMaskConfigFileName : this.userConfig.resolutionMaskConfigFileNames()) {
            File resolutionMaskConfigFile = new File(
                    this.userConfig.resolutionMaskConfigFilePath() +
                            resolutionMaskConfigFileName
            );

            this.resolutionMaskConfigs.add(loadTomlFile(resolutionMaskConfigFile, ResolutionMaskConfig.class));
        }
    }

    private void loadCharacterStyleConfig() {
        var typeRef = new TypeReference<HashMap<String, String>>() {
        };
        this.characterStyleConfig = loadTomlFile(new File(CHARACTER_STYLE_CONFIG_PATH), typeRef);
    }

    private <T> T loadTomlFile(File tomlFile, Class<T> valueType) {
        try {
            return MAPPER.readValue(tomlFile, valueType);
        } catch (Exception e) {
            LOGGER.warning(
                    String.format(
                            "Exception %s occurred while reading file %s",
                            e.getClass().getCanonicalName(),
                            tomlFile.getAbsolutePath()
                    )
            );
            return null;
        }
    }

    private <T> T loadTomlFile(File tomlFile, TypeReference<T> valueType) {
        try {
            return MAPPER.readValue(tomlFile, valueType);
        } catch (Exception e) {
            LOGGER.warning(
                    String.format(
                            "Exception %s occurred while reading file %s",
                            e.getClass().getCanonicalName(),
                            tomlFile.getAbsolutePath()
                    )
            );
            return null;
        }
    }
}
