package net.ssehub.kernel_haven.non_boolean.replacer;

import java.util.function.BiFunction;

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
            
        } else if (other instanceof VariableResult && ((VariableResult) other).isUnknownVariable()) {
            VariableResult o = (VariableResult) other;
            result = new VariableResult(o.getVar() + "_gt_" + value);
            
        } else {
            throw new ExpressionFormatException("Can't apply operator < or > on " + other.getClass().getSimpleName());
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
            
        } else if (other instanceof VariableResult && ((VariableResult) other).isUnknownVariable()) {
            VariableResult o = (VariableResult) other;
            result = new VariableResult(o.getVar() + "_ge_" + value);
            
        } else {
            throw new ExpressionFormatException("Can't apply operator <= or >= on " + other.getClass().getSimpleName());
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
            
        } else if (other instanceof VariableResult && ((VariableResult) other).isUnknownVariable()) {
            VariableResult o = (VariableResult) other;
            result = new VariableResult(o.getVar() + "_eq_" + value);
            
        } else {
            throw new ExpressionFormatException("Can't apply operator == or != on " + other.getClass().getSimpleName());
        }
        return result;
    }
    
    /**
     * Applies the given binary operation on this value and returns the result. The only allowed value for other
     * is another {@link LiteralIntResult}.
     * 
     * @param other The other value to use as the right-hand side in the binary operation.
     * @param op The operation. This value is the left-hand side, other is the right-hand side.
     * @param opcode A string representation of the operation. Used for error messages.
     * 
     * @return The new value after applying the given binary operation.
     * 
     * @throws ExpressionFormatException If other is not a {@link LiteralIntResult}.
     */
    private Result applyOperation(Result other, BiFunction<Long, Long, Long> op, String opcode)
            throws ExpressionFormatException {
        
        Result result;
        
        if (other instanceof LiteralIntResult) {
            LiteralIntResult o = (LiteralIntResult) other;
            result = new LiteralIntResult(op.apply(value, o.value));
            
        } else {
            throw new ExpressionFormatException("Can't apply operator " + opcode + " on literal and "
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
        return new LiteralIntResult(-value);
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
        return new LiteralIntResult(~value);
    }
    
    @Override
    public String toCppString() {
        // everything except 0 is true
        return value == 0 ? "0" : "1";
    }

    
}
