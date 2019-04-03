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
import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;

/**
 * A {@link Result} of an evaluation. This may be a tree of {@link BoolResult}s with other {@link Result}s in the
 * leaf-nodes.
 *
 * @author Adam
 */
abstract class Result {
    
    /**
     * Does a <code>&lt;</code> operation on this result on the left-hand side, and the given other result on the
     * right-hand side.
     * 
     * @param other The other result.
     * 
     * @return The result of applying the operation.
     * 
     * @throws ExpressionFormatException If this operation is not supported by this or other.
     */
    public abstract Result cmpLt(Result other) throws ExpressionFormatException;
    
    /**
     * Does a <code>&lt;=</code> operation on this result on the left-hand side, and the given other result on the
     * right-hand side.
     * 
     * @param other The other result.
     * 
     * @return The result of applying the operation.
     * 
     * @throws ExpressionFormatException If this operation is not supported by this or other.
     */
    public abstract Result cmpLe(Result other) throws ExpressionFormatException;
    
    /**
     * Does a <code>==</code> operation on this result on the left-hand side, and the given other result on the
     * right-hand side.
     * 
     * @param other The other result.
     * 
     * @return The result of applying the operation.
     * 
     * @throws ExpressionFormatException If this operation is not supported by this or other.
     */
    public abstract Result cmpEq(Result other) throws ExpressionFormatException;
    
    /**
     * Does a <code>+</code> operation on this result on the left-hand side, and the given other result on the
     * right-hand side.
     * 
     * @param other The other result.
     * 
     * @return The result of applying the operation.
     * 
     * @throws ExpressionFormatException If this operation is not supported by this or other.
     */
    public abstract Result add(Result other) throws ExpressionFormatException;
    
    /**
     * Does a <code>-</code> operation on this result on the left-hand side, and the given other result on the
     * right-hand side.
     * 
     * @param other The other result.
     * 
     * @return The result of applying the operation.
     * 
     * @throws ExpressionFormatException If this operation is not supported by this or other.
     */
    public abstract Result sub(Result other) throws ExpressionFormatException;
    
    /**
     * Does a <code>*</code> operation on this result on the left-hand side, and the given other result on the
     * right-hand side.
     * 
     * @param other The other result.
     * 
     * @return The result of applying the operation.
     * 
     * @throws ExpressionFormatException If this operation is not supported by this or other.
     */
    public abstract Result mul(Result other) throws ExpressionFormatException;
    
    /**
     * Does a <code>/</code> operation on this result on the left-hand side, and the given other result on the
     * right-hand side.
     * 
     * @param other The other result.
     * 
     * @return The result of applying the operation.
     * 
     * @throws ExpressionFormatException If this operation is not supported by this or other.
     */
    public abstract Result div(Result other) throws ExpressionFormatException;
    
    /**
     * Does a <code>%</code> operation on this result on the left-hand side, and the given other result on the
     * right-hand side.
     * 
     * @param other The other result.
     * 
     * @return The result of applying the operation.
     * 
     * @throws ExpressionFormatException If this operation is not supported by this or other.
     */
    public abstract Result mod(Result other) throws ExpressionFormatException;
    
    /**
     * Does a unary <code>-</code> operation on this result.
     * 
     * @return The result of applying the operation.
     * 
     * @throws ExpressionFormatException If this operation is not supported by this.
     */
    public abstract Result subUnary() throws ExpressionFormatException;
    
    /**
     * Does a <code>&amp;</code> operation on this result on the left-hand side, and the given other result on the
     * right-hand side.
     * 
     * @param other The other result.
     * 
     * @return The result of applying the operation.
     * 
     * @throws ExpressionFormatException If this operation is not supported by this or other.
     */
    public abstract Result binAnd(Result other) throws ExpressionFormatException;
    
    /**
     * Does a <code>|</code> operation on this result on the left-hand side, and the given other result on the
     * right-hand side.
     * 
     * @param other The other result.
     * 
     * @return The result of applying the operation.
     * 
     * @throws ExpressionFormatException If this operation is not supported by this or other.
     */
    public abstract Result binOr(Result other) throws ExpressionFormatException;
    
    /**
     * Does a <code>^</code> operation on this result on the left-hand side, and the given other result on the
     * right-hand side.
     * 
     * @param other The other result.
     * 
     * @return The result of applying the operation.
     * 
     * @throws ExpressionFormatException If this operation is not supported by this or other.
     */
    public abstract Result binXor(Result other) throws ExpressionFormatException;
    
    /**
     * Does a unary <code>~</code> operation on this result.
     * 
     * @return The result of applying the operation.
     * 
     * @throws ExpressionFormatException If this operation is not supported by this.
     */
    public abstract Result binInv() throws ExpressionFormatException;
    
    /**
     * Turns this {@link Result} (or tree of {@link Result}s) into a CPP compatible string.
     * 
     * @return A string representation of this {@link Result}.
     */
    public abstract String toCppString();

    /**
     * Turns this {@link Result} (or tree of {@link Result}s) into a non-CPP string. Basically the same as
     * {@link #toCppString()} but without defined() calls.
     * 
     * @return A string representation of this {@link Result}.
     */
    public abstract String toNonCppString();
    
    @Override
    public String toString() {
        return toNonCppString();
    }
    
    /**
     * Turns this {@link Result} (or tree of {@link Result}s) into a boolean formula.
     * 
     * @return This result as a boolean formula.
     */
    public abstract Formula toFormula();
    
}
