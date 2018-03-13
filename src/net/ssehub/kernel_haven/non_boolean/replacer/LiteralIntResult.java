package net.ssehub.kernel_haven.non_boolean.replacer;

import java.util.function.BiFunction;

import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;

class LiteralIntResult extends Result {

    private long value;
    
    public LiteralIntResult(long value) {
        this.value = value;
    }
    
    public long getValue() {
        return value;
    }

    @Override
    public Result cmpLt(Result other) throws ExpressionFormatException {
        Result result;
        if (other instanceof LiteralIntResult) {
            result = value < ((LiteralIntResult) other).value ? LiteralBoolResult.TRUE : LiteralBoolResult.FALSE;
            
        } else if (other instanceof VariableWithValues) {
            VariableWithValues o = (VariableWithValues) other;
            result = o.apply((value) -> this.value < value);
            
        } else if (other instanceof VariableResult) {
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
            
        } else if (other instanceof VariableWithValues) {
            VariableWithValues o = (VariableWithValues) other;
            result = o.apply((value) -> this.value <= value);
            
        } else if (other instanceof VariableResult) {
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
            
        } else if (other instanceof VariableWithValues) {
            VariableWithValues o = (VariableWithValues) other;
            result = o.apply((value) -> this.value == value);
            
        } else if (other instanceof VariableResult) {
            VariableResult o = (VariableResult) other;
            result = new VariableResult(o.getVar() + "_eq_" + value);
            
        } else {
            throw new ExpressionFormatException("Can't apply operator == or != on " + other.getClass().getSimpleName());
        }
        return result;
    }
    
    private Result applyOperation(Result other, BiFunction<Long, Long, Long> op, String opcode)
            throws ExpressionFormatException{
        
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
        return applyOperation(other, (a, b) -> a + b, "+");
    }
    
    @Override
    public Result sub(Result other) throws ExpressionFormatException {
        return applyOperation(other, (a, b) -> a - b, "-");
    }
    
    @Override
    public Result mul(Result other) throws ExpressionFormatException {
        return applyOperation(other, (a, b) -> a * b, "*");
    }
    
    @Override
    public Result div(Result other) throws ExpressionFormatException {
        return applyOperation(other, (a, b) -> a / b, "/");
    }
    
    @Override
    public Result mod(Result other) throws ExpressionFormatException {
        return applyOperation(other, (a, b) -> a % b, "%");
    }
    
    @Override
    public Result subUnary() throws ExpressionFormatException {
        return new LiteralIntResult(-value);
    }
    
    @Override
    public Result binAnd(Result other) throws ExpressionFormatException {
        return applyOperation(other, (a, b) -> a & b, "&");
    }
    
    @Override
    public Result binOr(Result other) throws ExpressionFormatException {
        return applyOperation(other, (a, b) -> a | b, "|");
    }
    
    @Override
    public Result binXor(Result other) throws ExpressionFormatException {
        return applyOperation(other, (a, b) -> a ^ b, "^");
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
