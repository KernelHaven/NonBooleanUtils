/*
 * Copyright 2017-2019 University of Hildesheim, Software Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
        = new Setting<>("prepare_non_boolean.destination", Type.DIRECTORY, true, null, "The destination directory "
                + "where a temporary copy of the source tree with the non boolean replacements should be placed. "
                + "All contents of this will be overwritten.");
    
    public static final Setting<Pattern> VARIABLE_REGEX
        = new Setting<>("code.extractor.variable_regex", Type.REGEX, true, null, "A regular expression to define what "
                + "the variables that require non-boolean replacements look like. This regex should also cover the "
                + "names of the constant variables, that should be replaced by their value.");

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
