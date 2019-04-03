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

import net.ssehub.kernel_haven.util.logic.Disjunction;
import net.ssehub.kernel_haven.util.logic.Formula;

/**
 * A boolean disjunction of two {@link Result}s.
 *
 * @author Adam
 */
class BoolOr extends BoolResult {

    private Result leftSide;
    
    private Result rightSide;
    
    /**
     * Creates this disjunction.
     * 
     * @param leftSide The left side.
     * @param rightSide The right side.
     */
    public BoolOr(Result leftSide, Result rightSide) {
        this.leftSide = leftSide;
        this.rightSide = rightSide;
    }
    
    @Override
    public String toCppString() {
        return "(" + leftSide.toCppString() + ") || (" + rightSide.toCppString() + ")";
    }
    
    @Override
    public String toNonCppString() {
        return "(" + leftSide.toNonCppString() + ") || (" + rightSide.toNonCppString() + ")";
    }
    
    @Override
    public Formula toFormula() {
        return new Disjunction(leftSide.toFormula(), rightSide.toFormula());
    }
    
}
