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

import net.ssehub.kernel_haven.util.null_checks.NonNull;
import net.ssehub.kernel_haven.variability_model.VariabilityVariable;

/**
 * An Integer-based variability variable with a infinite (not restricted) domain.
 * 
 * @author Adam
 */
public class InfiniteIntegerVariable extends VariabilityVariable {

    /**
     * Creates this {@link InfiniteIntegerVariable}.
     * 
     * @param name The name of this variable.
     * @param type The type of this variable.
     */
    public InfiniteIntegerVariable(@NonNull String name, @NonNull String type) {
        super(name, type);
    }

}
