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
 * <p>
 * A {@link Result} representing a {@link NonBooleanVariable}. Has a name and a set of possible values. This keeps track
 * of the current value of these values (modified through integer operations) and the original values that they come
 * from (when encountering a comparison, the current values are used to check if the comparison is satisfiable, but the
 * original values are used in the resulting VAR_eq_value string).
 * </p>
 * <p>
 * This can also contain multiple {@link NonBooleanVariable}s. In this case, it keeps tracks of combinations of
 * original values and the current value for this combination. For example, consider NonBooleanVariables A and B with
 * possible values 0 and 1 (each). The addition A + B produces the following pairs of (original value of A, original
 * value of B, current value): (0, 0, 0), (0, 1, 1), (1, 0, 1), (1, 1, 2).
 * </p>
 *
 * @author Adam
 */
class VariablesWithValues extends Result {
    
    private String[] varNames;
    
    /**
     * <p>First dimension: List of arrays of current and original values</p>
     * <p>Second dimension: First varNames.length entries are original values, last value is current.</p>
     */
    private long[][] values;
    
    /**
     * Creates a variable with the given possible values.
     * 
     * @param var The variable name.
     * @param values The possible values.
     */
    public VariablesWithValues(String var, long ... values) {
        this.varNames = new String[] {var};
        
        this.values = new long[values.length][2];
        for (int i = 0; i < values.length; i++) {
            this.values[i][0] = values[i];
            this.values[i][1] = values[i];
        }
    }
    
    /**
     * Creates a {@link VariablesWithValues} for the given varNames and values.
     * 
     * @param varNames The variable names.
     * @param values The values.
     */
    private VariablesWithValues(String[] varNames, long[][] values) {
        this.varNames = varNames;
        this.values = values;
    }
    
    /**
     * Returns the current value for the given line in values.
     * 
     * @param lineIndex The line index of values.
     * 
     * @return The current value in the line.
     */
    private long getCurrentValue(int lineIndex) {
        long[] line = this.values[lineIndex];
        return line[line.length - 1];
    }
    
    /**
     * Sets the current value for the given line in values.
     * 
     * @param lineIndex The line index of values.
     * @param value The new current value in the line.
     */
    private void setCurrentValue(int lineIndex, long value) {
        long[] line = this.values[lineIndex];
        line[line.length - 1] = value;
    }
    
    /**
     * Returns the number of lines (original values plus current value pairs).
     * 
     * @return The number of lines.
     */
    private int getNumberOfLines() {
        return values.length;
    }
    
    /**
     * Returns the variable name for the given variable index.
     * 
     * @param varIndex The variable index.
     * 
     * @return The name of the variable.
     */
    public String getVarName(int varIndex) {
        return varNames[varIndex];
    }
    
    /**
     * Returns the number of variables this class holds.
     * 
     * @return The number of variables.
     */
    public int getNumVars() {
        return varNames.length;
    }
    
    /**
     * Creates a boolean expression for the original values in the given line.
     * 
     * @param line The line with the original values. (a line from this.values).
     * 
     * @return A boolean result expression.
     */
    private Result buildResultForCombination(long[] line) {
        Result result =  new VariableResult(getVarName(0) + "_eq_" + line[0]);
        
        for (int i = 1; i < getNumVars(); i++) {
            result = new BoolAnd(result, new VariableResult(getVarName(i) + "_eq_" + line[i]));
        }
        
        return result;
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
        List<long[]> newValues = new LinkedList<>();
        for (int i = 0; i < getNumberOfLines(); i++) {
            if (filter.apply(getCurrentValue(i))) {
                newValues.add(values[i]);
            }
        }
        
        Result result;
        if (newValues.isEmpty()) {
            result = LiteralBoolResult.FALSE;
            
        } else {
            Iterator<long[]> it = newValues.iterator();
            result = buildResultForCombination(it.next());
            while (it.hasNext()) {
                result = new BoolOr(result, buildResultForCombination(it.next()));
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
            if (getNumVars() > 1) {
                throw new ExpressionFormatException(
                        "Can't compare unknown variable with multiple VariablesWithResults");
            }
            VariableResult o = (VariableResult) other;
            result = new VariableResult(varNames[0] + "_lt_" + o.getVar());
            
        } else if (other instanceof VariablesWithValues) {
            result = join(this, (VariablesWithValues) other, (v1, v2) -> v1 < v2);
            
        } else {
            throw new ExpressionFormatException("Can't apply operator < or > on VariablesWithValues and "
                    + other.getClass().getSimpleName());
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
            if (getNumVars() > 1) {
                throw new ExpressionFormatException(
                        "Can't compare unknown variable with multiple VariablesWithResults");
            }
            VariableResult o = (VariableResult) other;
            result = new VariableResult(varNames[0] + "_le_" + o.getVar());
            
        } else if (other instanceof VariablesWithValues) {
            result = join(this, (VariablesWithValues) other, (v1, v2) -> v1 <= v2);
            
        } else {
            throw new ExpressionFormatException("Can't apply operator <= or >= on VariablesWithValues and "
                    + other.getClass().getSimpleName());
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
            if (getNumVars() > 1) {
                throw new ExpressionFormatException(
                        "Can't compare unknown variable with multiple VariablesWithResults");
            }
            VariableResult o = (VariableResult) other;
            result = new VariableResult(varNames[0] + "_eq_" + o.getVar());
            
        } else if (other instanceof VariablesWithValues) {
            result = join(this, (VariablesWithValues) other, (v1, v2) -> v1 == v2);
            
        } else {
            throw new ExpressionFormatException("Can't apply operator == or != on VariablesWithValues and "
                    + other.getClass().getSimpleName());
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
    private static Result join(VariablesWithValues var1, VariablesWithValues var2,
            BiFunction<Long, Long, Boolean> comparison) {
        
        List<BoolAnd> parts = new ArrayList<>(var1.getNumberOfLines() * var2.getNumberOfLines());
        
        for (int values1Index = 0; values1Index < var1.getNumberOfLines(); values1Index++) {
            for (int values2Index = 0; values2Index < var2.getNumberOfLines(); values2Index++) {
                
                if (comparison.apply(var1.getCurrentValue(values1Index), var2.getCurrentValue(values2Index))) {
                    parts.add(new BoolAnd(
                            var1.buildResultForCombination(var1.values[values1Index]),
                            var2.buildResultForCombination(var2.values[values2Index])));
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
        for (int i = 0; i < getNumberOfLines(); i++) {
            setCurrentValue(i, -getCurrentValue(i));
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
     * @param switchSides Whether left- and right-hand side should be reversed.
     * 
     * @return this, with the operation applied to all current values.
     * 
     * @throws ExpressionFormatException If other is not a {@link LiteralIntResult}.
     */
    public Result applyOperation(Result other, BiFunction<Long, Long, Long> op, String opcode, boolean switchSides) 
            throws ExpressionFormatException {
        
        Result result;
        if (other instanceof LiteralIntResult) {
            LiteralIntResult o = (LiteralIntResult) other;
            for (int i = 0; i < getNumberOfLines(); i++) {
                if (switchSides) {
                    setCurrentValue(i, op.apply(o.getValue(), getCurrentValue(i)));
                } else {
                    setCurrentValue(i, op.apply(getCurrentValue(i), o.getValue()));
                }
            }
            result = this;
            
        } else if (other instanceof VariablesWithValues) {
            VariablesWithValues o = (VariablesWithValues) other;
            
            String[] varNames = new String[this.getNumVars() + o.getNumVars()];
            // varNames = {this.varNames, o.VarNames}
            System.arraycopy(this.varNames, 0, varNames, 0, this.varNames.length);
            System.arraycopy(o.varNames, 0, varNames, this.varNames.length, o.varNames.length);

            long[][] values = new long[this.values.length * o.values.length][this.getNumVars() + o.getNumVars() + 1];
            int valuesIndex = 0;
            for (int thisIndex = 0; thisIndex < getNumberOfLines(); thisIndex++) {
                for (int oIndex = 0; oIndex < o.getNumberOfLines(); oIndex++) {
                    long[] line = values[valuesIndex];
                    valuesIndex++;
                    
                    long[] thisLine = this.values[thisIndex];
                    long[] oLine = o.values[oIndex];
                    
                    // line = {thisLine (except last), oLine (except last), combineCurrentValue}
                    System.arraycopy(thisLine, 0, line, 0, thisLine.length - 1);
                    System.arraycopy(oLine, 0, line, thisLine.length - 1, oLine.length - 1);
                    if (switchSides) {
                        line[line.length - 1] = op.apply(o.getCurrentValue(oIndex), this.getCurrentValue(thisIndex)); 
                    } else {
                        line[line.length - 1] = op.apply(this.getCurrentValue(thisIndex), o.getCurrentValue(oIndex)); 
                    }
                }
            }
            
            result = new VariablesWithValues(varNames, values);
            
        } else {
            throw new ExpressionFormatException("Can't apply operator " + opcode
                    + " on VariableWithValues and " + other.getClass().getSimpleName());
        }
        return result;
    }
    
    @Override
    public Result add(Result other) throws ExpressionFormatException {
        return applyOperation(other, (aa, bb) -> aa + bb, "+", false);
    }
    
    @Override
    public Result sub(Result other) throws ExpressionFormatException {
        return applyOperation(other, (aa, bb) -> aa - bb, "-", false);
    }
    
    @Override
    public Result mul(Result other) throws ExpressionFormatException {
        return applyOperation(other, (aa, bb) -> aa * bb, "*", false);
    }
    
    @Override
    public Result div(Result other) throws ExpressionFormatException {
        return applyOperation(other, (aa, bb) -> aa / bb, "/", false);
    }
    
    @Override
    public Result mod(Result other) throws ExpressionFormatException {
        return applyOperation(other, (aa, bb) -> aa % bb, "%", false);
    }
    
    @Override
    public Result binAnd(Result other) throws ExpressionFormatException {
        return applyOperation(other, (aa, bb) -> aa & bb, "&", false);
    }
    
    @Override
    public Result binOr(Result other) throws ExpressionFormatException {
        return applyOperation(other, (aa, bb) -> aa | bb, "|", false);
    }
    
    @Override
    public Result binXor(Result other) throws ExpressionFormatException {
        return applyOperation(other, (aa, bb) -> aa ^ bb, "^", false);
    }
    
    @Override
    public Result binInv() throws ExpressionFormatException {
        for (int i = 0; i < getNumberOfLines(); i++) {
            setCurrentValue(i, ~getCurrentValue(i));
        }
        return this;
    }

    @Override
    public String toCppString() {
        return new BoolNot(apply((currentValue) -> currentValue == 0)).toCppString();
    }

}
