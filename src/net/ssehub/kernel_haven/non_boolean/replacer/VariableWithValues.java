package net.ssehub.kernel_haven.non_boolean.replacer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;

public class VariableWithValues extends Result {
    
    private static final class Value {
        
        private long original;
        private long current;
        
        public Value(long original) {
            this.original = original;
            this.current = original;
        }
    }
    
    private String var;
    
    private List<Value> values;
    
    public VariableWithValues(String var, long ... values) {
        this.var = var;
        this.values = new ArrayList<>(values.length);
        for (Long value : values) {
            this.values.add(new Value(value));
        }
    }
    
    public String getVar() {
        return var;
    }
    
    public Result apply(Function<Long, Boolean> filter) {
        List<Value> newValues = new LinkedList<>();
        for (Value value : values) {
            if (filter.apply(value.current)) {
                newValues.add(value);
            }
        }
        
        Result result;
        if (newValues.isEmpty()) {
            result = LiteralBoolResult.FALSE;
            
        } else {
            Iterator<Value> it = newValues.iterator();
            result = new VariableResult(var + "_eq_" + it.next().original);
            while (it.hasNext()) {
                result = new BoolOr(result, new VariableResult(var + "_eq_" + it.next().original));
            }
        }
        
        return result;
    }

    @Override
    public Result cmpLt(Result other) throws ExpressionFormatException {
        Result result;
        if (other instanceof LiteralIntResult) {
            LiteralIntResult o = (LiteralIntResult) other;
            result = apply((value) -> value < o.getValue());
            
        } else if (other instanceof VariableResult) {
            VariableResult o = (VariableResult) other;
            result = new VariableResult(var + "_lt_" + o.getVar());
            
        } else if (other instanceof VariableWithValues) {
            result = join(this, (VariableWithValues) other, (v1, v2) -> v1 < v2);
            
        } else {
            throw new ExpressionFormatException("Can't apply operator < or > on " + other.getClass().getSimpleName());
        }
        return result;
    }
    
    @Override
    public Result cmpLe(Result other) throws ExpressionFormatException {
        Result result;
        if (other instanceof LiteralIntResult) {
            LiteralIntResult o = (LiteralIntResult) other;
            result = apply((value) -> value <= o.getValue());
            
        } else if (other instanceof VariableResult) {
            VariableResult o = (VariableResult) other;
            result = new VariableResult(var + "_le_" + o.getVar());
            
        } else if (other instanceof VariableWithValues) {
            result = join(this, (VariableWithValues) other, (v1, v2) -> v1 <= v2);
            
        } else {
            throw new ExpressionFormatException("Can't apply operator <= or >= on " + other.getClass().getSimpleName());
        }
        return result;
    }
    
    @Override
    public Result cmpEq(Result other) throws ExpressionFormatException {
        Result result;
        if (other instanceof LiteralIntResult) {
            LiteralIntResult o = (LiteralIntResult) other;
            result = apply((value) -> value == o.getValue());
            
        } else if (other instanceof VariableResult) {
            VariableResult o = (VariableResult) other;
            result = new VariableResult(var + "_eq_" + o.getVar());
            
        } else if (other instanceof VariableWithValues) {
            result = join(this, (VariableWithValues) other, (v1, v2) -> v1 == v2);
            
        } else {
            throw new ExpressionFormatException("Can't apply operator == or != on " + other.getClass().getSimpleName());
        }
        return result;
    }
    
    private static Result join(VariableWithValues var1, VariableWithValues var2, BiFunction<Long, Long, Boolean> comparison) {
        List<BoolAnd> parts = new ArrayList<>(var1.values.size() * var2.values.size());
        
        for (Value value1 : var1.values) {
            for (Value value2 : var2.values) {
                
                if (comparison.apply(value1.current, value2.current)) {
                    parts.add(new BoolAnd(
                            new VariableResult(var1.getVar() + "_eq_" + value1.original),
                            new VariableResult(var2.getVar() + "_eq_" + value2.original)));
                }
                
            }
        }
        
        Result result;
        if (parts.isEmpty()) {
            result = LiteralBoolResult.FALSE;
        } else {
            result = parts.get(0);
            for (int i = 1; i < parts.size(); i++) {
                result = new BoolOr(result, parts.get(i));
            }
        }
        
        return result;
    }
    
    @Override
    public Result subUnary() throws ExpressionFormatException {
        for (Value value : values) {
            value.current = -value.current;
        }
        return this;
    }
    
    private Result applyOperation(Result other, BiFunction<Long, Long, Long> op, String opcode) 
            throws ExpressionFormatException{
        Result result;
        if (other instanceof LiteralIntResult) {
            LiteralIntResult o = (LiteralIntResult) other;
            for (Value value : values) {
                value.current = op.apply(value.current, o.getValue());
            }
            result = this;
            
        } else {
            throw new ExpressionFormatException("Can't apply operator " + opcode + " on VariableWithValues with operand "
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
        for (Value value : values) {
            value.current = ~value.current;
        }
        return this;
    }

    @Override
    public String toCppString() {
        String result = "0";
        List<Long> originalValuesThatAreNowZero = new LinkedList<>();
        
        for (Value value : values) {
            if (value.current == 0) {
                originalValuesThatAreNowZero.add(value.original);
            }
        }
        
        if (!originalValuesThatAreNowZero.isEmpty()) {
            StringBuilder builder = new StringBuilder("(");
            
            Iterator<Long> it = originalValuesThatAreNowZero.iterator();
            builder.append("!defined(").append(var).append("_eq_").append(it.next()).append(")");
            while (it.hasNext()) {
                builder.append("&& !defined(").append(var).append("_eq_").append(it.next()).append(")");
            }
            
            builder.append(")");
            result = builder.toString();
        }
        return result;
    }

}
