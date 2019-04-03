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
package net.ssehub.kernel_haven.non_boolean.replacer;

import net.ssehub.kernel_haven.util.logic.False;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.True;

/**
 * A literal boolean value. Singleton with two instances, {@link #TRUE} and {@link #FALSE}.
 *
 * @author Adam
 */
class LiteralBoolResult extends BoolResult {

    public static final LiteralBoolResult TRUE = new LiteralBoolResult(true);
    
    public static final LiteralBoolResult FALSE = new LiteralBoolResult(false);
    
    private boolean value;
    
    /**
     * Creates a literal boolean value.
     * 
     * @param value The value.
     */
    private LiteralBoolResult(boolean value) {
        this.value = value;
    }
    
    @Override
    public String toCppString() {
        return value ? "1" : "0";
    }
    
    @Override
    public String toNonCppString() {
        return toCppString();
    }
    
    @Override
    public Formula toFormula() {
        return value ? True.INSTANCE : False.INSTANCE;
    }
    
}
