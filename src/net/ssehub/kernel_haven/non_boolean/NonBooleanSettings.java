package net.ssehub.kernel_haven.non_boolean;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.config.Setting;
import net.ssehub.kernel_haven.config.Setting.Type;

/**
 * Settings for the non boolean utilities.
 *
 * @author Adam
 */
public class NonBooleanSettings {
    
    public static final Setting<File> DESTINATION_DIR
        = new Setting<>("prepare_non_boolean.destination", Type.DIRECTORY, true, null, "TODO");
    
    public static final Setting<Pattern> VARIABLE_REGEX
        = new Setting<>("code.extractor.variable_regex", Type.REGEX, true, null, "TODO");

    /**
     * Holds all declared setting constants.
     */
    private static final Set<Setting<?>> SETTINGS = new HashSet<>();
    
    static {
        for (Field field : NonBooleanSettings.class.getFields()) {
            if (Setting.class.isAssignableFrom(field.getType())
                    && Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
                try {
                    SETTINGS.add((Setting<?>) field.get(null));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Don't allow instance of this class.
     */
    private NonBooleanSettings() {
    }
    
    /**
     * Registers all settings declared in this class to the given configuration object.
     * 
     * @param config The configuration to register the settings to.
     * 
     * @throws SetUpException If any setting restrictions are violated.
     */
    public static void registerAllSettings(Configuration config) throws SetUpException {
        for (Setting<?> setting : SETTINGS) {
            config.registerSetting(setting);
        }
    }
    
}
