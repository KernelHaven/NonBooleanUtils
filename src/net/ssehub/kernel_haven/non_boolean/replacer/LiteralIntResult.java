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

import java.util.function.BiFunction;

import net.ssehub.kernel_haven.non_boolean.replacer.VariableResult.Type;
import net.ssehub.kernel_haven.util.logic.False;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;

/**
 * A literal integer value.
 *
 * @author Adam
 */
class LiteralIntResult extends Result {

    private long value;
    
    /**
     * Creates this literal integer value.
     * 
     * @param value The value of this.
     */
    public LiteralIntResult(long value) {
        this.value = value;
    }
    
    /**
     * Returns the value of this.
     * 
     * @return The value of this.
     */
    public long getValue() {
        return value;
    }

    @Override
    public Result cmpLt(Result other) throws ExpressionFormatException {
        Result result;
        if (other instanceof LiteralIntResult) {
            result = value < ((LiteralIntResult) other).value ? LiteralBoolResult.TRUE : LiteralBoolResult.FALSE;
            
        } else if (other instanceof VariablesWithValues) {
            VariablesWithValues o = (VariablesWithValues) other;
            result = o.apply((value) -> this.value < value);
            
        } else if (other instanceof VariableResult && ((VariableResult) other).getType() != Type.FINAL) {
            VariableResult o = (VariableResult) other;
            if (o.getType() == Type.UNKNOWN) {
                o.setVar(o.getVar() + "_gt_" + value);
            }
            // no change for o.type==INFINITE
            
            o.setType(Type.FINAL);
            result = o;
            
        } else {
            throw new ExpressionFormatException("Can't apply operator < or > on Literal and "
                    + other.getClass().getSimpleName());
        }
        return result;
    }

    @Override
    public Result cmpLe(Result other) throws ExpressionFormatException {
        Result result;
        
        if (other instanceof LiteralIntResult) {
            result = value <= ((LiteralIntResult) other).value ? LiteralBoolResult.TRUE : LiteralBoolResult.FALSE;
            
        } else if (other instanceof VariablesWithValues) {
            VariablesWithValues o = (VariablesWithValues) other;
            result = o.apply((value) -> this.value <= value);
            
        } else if (other instanceof VariableResult && ((VariableResult) other).getType() != Type.FINAL) {
            VariableResult o = (VariableResult) other;
            if (o.getType() == Type.UNKNOWN) {
                o.setVar(o.getVar() + "_ge_" + value);
            }
            // no change for o.type==INFINITE
            
            o.setType(Type.FINAL);
            result = o;
            
        } else {
            throw new ExpressionFormatException("Can't apply operator <= or >= on Literal and "
                    + other.getClass().getSimpleName());
        }
        return result;
    }
    
    @Override
    public Result cmpEq(Result other) throws ExpressionFormatException {
        Result result;
        
        if (other instanceof LiteralIntResult) {
            result = value == ((LiteralIntResult) other).value ? LiteralBoolResult.TRUE : LiteralBoolResult.FALSE;
            
        } else if (other instanceof VariablesWithValues) {
            VariablesWithValues o = (VariablesWithValues) other;
            result = o.apply((value) -> this.value == value);
            
        } else if (other instanceof VariableResult && ((VariableResult) other).getType() != Type.FINAL) {
            VariableResult o = (VariableResult) other;
            if (o.getType() == Type.UNKNOWN) {
                o.setVar(o.getVar() + "_eq_" + value);
            }
            // no change for o.type==INFINITE
            
            o.setType(Type.FINAL);
            result = o;
            
        } else {
            throw new ExpressionFormatException("Can't apply operator == or != on Literal and "
                    + other.getClass().getSimpleName());
        }
        return result;
    }
    
    /**
     * Applies the given binary operation on this value and returns the result. The only allowed value for other
     * is another {@link LiteralIntResult} or {@link VariablesWithValues}.
     * 
     * @param other The other value to use as the right-hand side in the binary operation.
     * @param op The operation. This value is the left-hand side, other is the right-hand side.
     * @param opcode A string representation of the operation. Used for error messages.
     * 
     * @return The new value after applying the given binary operation.
     * 
     * @throws ExpressionFormatException If other is not a {@link LiteralIntResult} or a {@link VariablesWithValues}.
     */
    private Result applyOperation(Result other, BiFunction<Long, Long, Long> op, String opcode)
            throws ExpressionFormatException {
        
        Result result;
        
        if (other instanceof LiteralIntResult) {
            LiteralIntResult o = (LiteralIntResult) other;
            this.value = op.apply(this.value, o.value);
            result = this;
            
        } else if (other instanceof VariablesWithValues) {
            VariablesWithValues o = (VariablesWithValues) other;
            result = o.applyOperation(this, op, opcode, true);
            
        } else if (other instanceof VariableResult && ((VariableResult) other).getType() == Type.INFINITE) {
            // integer operations have no effect on INFINITE integer variables
            VariableResult o = (VariableResult) other;
            result = o;
            
        } else {
            throw new ExpressionFormatException("Can't apply operator " + opcode + " on Literal and "
                    + other.getClass().getSimpleName());
        }
        return result;
    }
    
    @Override
    public Result add(Result other) throws ExpressionFormatException {
        return applyOperation(other, (aa, bb) -> aa + bb, "+");
    }
    
    @Override
    public Result sub(Result other) throws ExpressionFormatException {
        return applyOperation(other, (aa, bb) -> aa - bb, "-");
    }
    
    @Override
    public Result mul(Result other) throws ExpressionFormatException {
        return applyOperation(other, (aa, bb) -> aa * bb, "*");
    }
    
    @Override
    public Result div(Result other) throws ExpressionFormatException {
        return applyOperation(other, (aa, bb) -> aa / bb, "/");
    }
    
    @Override
    public Result mod(Result other) throws ExpressionFormatException {
        return applyOperation(other, (aa, bb) -> aa % bb, "%");
    }
    
    @Override
    public Result subUnary() throws ExpressionFormatException {
        this.value = -this.value;
        return this;
    }
    
    @Override
    public Result binAnd(Result other) throws ExpressionFormatException {
        return applyOperation(other, (aa, bb) -> aa & bb, "&");
    }
    
    @Override
    public Result binOr(Result other) throws ExpressionFormatException {
        return applyOperation(other, (aa, bb) -> aa | bb, "|");
    }
    
    @Override
    public Result binXor(Result other) throws ExpressionFormatException {
        return applyOperation(other, (aa, bb) -> aa ^ bb, "^");
    }
    
    @Override
    public Result binInv() throws ExpressionFormatException {
        this.value = ~this.value;
        return this;
    }
    
    @Override
    public String toCppString() {
        // everything except 0 is true
        return value == 0 ? "0" : "1";
    }
    
    @Override
    public String toNonCppString() {
        // everything except 0 is true
        return value == 0 ? "0" : "1";
    }
    
    @Override
    public Formula toFormula() {
        // everything except 0 is true
        return value == 0 ? False.INSTANCE : True.INSTANCE;
    }
    
}
