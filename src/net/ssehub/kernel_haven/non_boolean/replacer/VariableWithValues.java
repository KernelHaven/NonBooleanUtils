package net.ssehub.kernel_haven.non_boolean.replacer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.ssehub.kernel_haven.non_boolean.NonBooleanPreperation.NonBooleanVariable;
import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;

/**
 * A {@link Result} representing a {@link NonBooleanVariable}. Has a name and a set of possible values. This keeps track
 * of the current value of these values (modified through integer operations) and the original values that they come
 * from (when encountering a comparison, the current values are used to check if the comparison is satisfiable, but the
 * original values are used in the resulting VAR_eq_value string). 
 *
 * @author Adam
 */
class VariableWithValues extends Result {
    
    /**
     * A pair of original and current values.
     *
     * @author Adam
     */
    private static final class Value {
        
        private long original;
        private long current;
        
        /**
         * Creates this pair. Initially, original == current.
         * 
         * @param original The original value.
         */
        public Value(long original) {
            this.original = original;
            this.current = original;
        }
    }
    
    private String var;
    
    private List<Value> values;
    
    /**
     * Creates a variable with the given possible values.
     * 
     * @param var The variable name.
     * @param values The possible values.
     */
    public VariableWithValues(String var, long ... values) {
        this.var = var;
        this.values = new ArrayList<>(values.length);
        for (Long value : values) {
            this.values.add(new Value(value));
        }
    }
    
    /**
     * Returns the variable name.
     * 
     * @return The variable name.
     */
    public String getVar() {
        return var;
    }
    
    /**
     * Applies the given filter (comparison operator) on all values of this and returns a {@link BoolResult} tree
     * with {@link VariableResult}s for all original values that survive the filter.
     * 
     * @param filter The filter to apply on current values.
     * 
     * @return The resulting boolean expression that defines which original values satisfy the filter.
     */
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
            
        } else if (other instanceof VariableResult && ((VariableResult) other).isUnknownVariable()) {
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
            
        } else if (other instanceof VariableResult && ((VariableResult) other).isUnknownVariable()) {
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
            
        } else if (other instanceof VariableResult && ((VariableResult) other).isUnknownVariable()) {
            VariableResult o = (VariableResult) other;
            result = new VariableResult(var + "_eq_" + o.getVar());
            
        } else if (other instanceof VariableWithValues) {
            result = join(this, (VariableWithValues) other, (v1, v2) -> v1 == v2);
            
        } else {
            throw new ExpressionFormatException("Can't apply operator == or != on " + other.getClass().getSimpleName());
        }
        return result;
    }
    
    /**
     * Creates a {@link BoolResult} tree with {@link VariableResult}s for a comparison with the given two
     * {@link VariableWithValues}s. This boolean expression will list all the possible combinations of original values
     * that satisfy the given comparison with their current values.
     * 
     * @param var1 The left-hand side of the comparison.
     * @param var2 The right-hand side of the comparison.
     * @param comparison The comparison operator.
     * 
     * @return A boolean expression that fulfills the given comparison.
     */
    private static Result join(VariableWithValues var1, VariableWithValues var2,
            BiFunction<Long, Long, Boolean> comparison) {
        
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
    
    /**
     * Applies the given integer arithmetic operation on all current values. Other is the right-hand side of the
     * operation; it must be an {@link LiteralIntResult}.
     * 
     * @param other The right-hand side of the operation.
     * @param op The operation to perform on all current values.
     * @param opcode A string representation of the operation. Used in error messages.
     * 
     * @return this, with the operation applied to all current values.
     * 
     * @throws ExpressionFormatException If other is not a {@link LiteralIntResult}.
     */
    private Result applyOperation(Result other, BiFunction<Long, Long, Long> op, String opcode) 
            throws ExpressionFormatException {
        
        Result result;
        if (other instanceof LiteralIntResult) {
            LiteralIntResult o = (LiteralIntResult) other;
            for (Value value : values) {
                value.current = op.apply(value.current, o.getValue());
            }
            result = this;
            
        } else {
            throw new ExpressionFormatException("Can't apply operator " + opcode
                    + " on VariableWithValues with operand " + other.getClass().getSimpleName());
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
        for (Value value : values) {
            value.current = ~value.current;
        }
        return this;
    }

    @Override
    public String toCppString() {
        String result = "1";
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
                builder.append(" && !defined(").append(var).append("_eq_").append(it.next()).append(")");
            }
            
            builder.append(")");
            result = builder.toString();
        }
        return result;
    }

}
