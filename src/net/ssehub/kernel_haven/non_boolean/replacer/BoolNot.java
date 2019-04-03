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

import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.Negation;

/**
 * A boolean negation of a {@link Result}.
 *
 * @author Adam
 */
class BoolNot extends BoolResult {
    
    private Result nested;
    
    /**
     * Creates this negation.
     * 
     * @param nested The nested {@link Result}.
     */
    public BoolNot(Result nested) {
        this.nested = nested;
    }
    
    @Override
    public String toCppString() {
        String result;
        if (nested == LiteralBoolResult.FALSE) {
            result = "1"; // not false
        } else if (nested == LiteralBoolResult.TRUE) {
            result = "0"; // not true
        } else {
            result = "!(" + nested.toCppString() + ")";
        }
        return result;
    }
    
    @Override
    public String toNonCppString() {
        String result;
        if (nested == LiteralBoolResult.FALSE) {
            result = "1"; // not false
        } else if (nested == LiteralBoolResult.TRUE) {
            result = "0"; // not true
        } else {
            result = "!(" + nested.toNonCppString() + ")";
        }
        return result;
    }
    
    @Override
    public Formula toFormula() {
        return new Negation(nested.toFormula());
    }

}
