package net.ssehub.kernel_haven.non_boolean.replacer;

import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.Negation;
import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;

/**
 * A variable with a string name. This can be an unknown variable, in which case {@link #isUnknownVariable()} returns
 * true. An unknown variable can still be the target of comparisons. If this is not unknown, then this variable name
 * is probably in the form of <code>VAR_eq_2</code> (except this was created as a result of a defined() call).
 *
 * @author Adam
 */
class VariableResult extends Result {
    
    private String var;
    
    private boolean unknownVariable;
    
    /**
     * Creates a new (not unknown) variable result with the given name. No operations can be done on this anymore.
     * 
     * @param var The name of this variable.
     */
    public VariableResult(String var) {
        this.var = var;
    }
    
    /**
     * Creates a new variable. If unknownVariable is <code>true</code>, then there may be still be comparison operations
     * done on this variable.
     * 
     * @param var The name of this variable.
     * @param unknownVariable Whether this is an unknown variable or not.
     */
    public VariableResult(String var, boolean unknownVariable) {
        this.var = var;
        this.unknownVariable = unknownVariable;
    }
    
    /**
     * Whether this represents an unknown (<code>true</code>) or an already "resolved" variable. "Resolved" variables
     * cannot be used in operations anymore; unknown variables can be used in comparisons.
     * 
     * @return Whether this variable is unknown.
     */
    public boolean isUnknownVariable() {
        return unknownVariable;
    }
    
    /**
     * Overrides whether this is an unknown variable.
     * 
     * @param unknownVariable Whether this is an unknown variable.
     * 
     * @see #isUnknownVariable()
     */
    public void setUnknownVariable(boolean unknownVariable) {
        this.unknownVariable = unknownVariable;
    }
    
    /**
     * Returns the name of this variable.
     * 
     * @return The name of this variable.
     */
    public String getVar() {
        return var;
    }
    
    /**
     * Changes the name of this variable.
     * 
     * @param var The new name of this variable.
     */
    public void setVar(String var) {
        this.var = var;
    }
    
    @Override
    public Result cmpLt(Result other) throws ExpressionFormatException {
        if (!unknownVariable) {
            throw new ExpressionFormatException("Can't apply operator < or > on VariableResult");
        }
        
        Result result;
        if (other instanceof LiteralIntResult) {
            LiteralIntResult lit = (LiteralIntResult) other;
            this.var = var + "_lt_" + lit.getValue();
            this.unknownVariable = false;
            result = this;
            
        } else if (other instanceof VariableResult && ((VariableResult) other).unknownVariable) {
            VariableResult o = (VariableResult) other;
            this.var = var + "_lt_" + o.getVar();
            this.unknownVariable = false;
            result = this;
            
        } else if (other instanceof VariablesWithValues && ((VariablesWithValues) other).getNumVars() == 1) {
            VariablesWithValues o = (VariablesWithValues) other;
            this.var = o.getVarName(0) + "_gt_" + var;
            this.unknownVariable = false;
            result  = this;
            
        } else {
            throw new ExpressionFormatException("Can't apply operator < or > on Unknown variable and "
                    + other.getClass().getSimpleName());
        }
        return result;
    }
    
    @Override
    public Result cmpLe(Result other) throws ExpressionFormatException {
        if (!unknownVariable) {
            throw new ExpressionFormatException("Can't apply operator <= or => on VariableResult");
        }
        
        Result result;
        if (other instanceof LiteralIntResult) {
            LiteralIntResult lit = (LiteralIntResult) other;
            this.var = var + "_le_" + lit.getValue();
            this.unknownVariable = false;
            result = this;
            
        } else if (other instanceof VariableResult && ((VariableResult) other).unknownVariable) {
            VariableResult o = (VariableResult) other;
            this.var = var + "_le_" + o.getVar();
            this.unknownVariable = false;
            result = this;
            
        } else if (other instanceof VariablesWithValues && ((VariablesWithValues) other).getNumVars() == 1) {
            VariablesWithValues o = (VariablesWithValues) other;
            this.var = o.getVarName(0) +  "_ge_" + var;
            this.unknownVariable = false;
            result = this;
            
        } else {
            throw new ExpressionFormatException("Can't apply operator <= or >= on Unknown variable and "
                    + other.getClass().getSimpleName());
        }
        return result;
    }
    
    @Override
    public Result cmpEq(Result other) throws ExpressionFormatException {
        if (!unknownVariable) {
            throw new ExpressionFormatException("Can't apply operator == or != on VariableResult");
        }
        
        Result result;
        if (other instanceof LiteralIntResult) {
            LiteralIntResult lit = (LiteralIntResult) other;
            this.var = var + "_eq_" + lit.getValue();
            this.unknownVariable = false;
            result = this;
            
        } else if (other instanceof VariableResult && ((VariableResult) other).unknownVariable) {
            VariableResult o = (VariableResult) other;
            this.var = var + "_eq_" + o.getVar();
            this.unknownVariable = false;
            result = this;
            
        } else if (other instanceof VariablesWithValues  && ((VariablesWithValues) other).getNumVars() == 1) {
            VariablesWithValues o = (VariablesWithValues) other;
            this.var = o.getVarName(0) +  "_eq_" + var;
            this.unknownVariable = false;
            result = this;
            
        } else {
            throw new ExpressionFormatException("Can't apply operator == or != on Unknown variable and "
                    + other.getClass().getSimpleName());
        }
        return result;
    }
    
    @Override
    public Result subUnary() throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator unary subtraction on VariableResult");
    }
    
    @Override
    public Result add(Result other) throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator + on VariableResult");
    }
    
    @Override
    public Result sub(Result other) throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator - on VariableResult");
    }
    
    @Override
    public Result mul(Result other) throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator * on VariableResult");
    }
    
    @Override
    public Result div(Result other) throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator / on VariableResult");
    }
    
    @Override
    public Result mod(Result other) throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator % on VariableResult");
    }
    
    @Override
    public Result binAnd(Result other) throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator % on VariableResult");
    }
    @Override
    public Result binOr(Result other) throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator | on VariableResult");
    }
    @Override
    public Result binXor(Result other) throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator ^ on VariableResult");
    }
    @Override
    public Result binInv() throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator ~ on VariableResult");
    }
    
    @Override
    public String toCppString() {
        String result;
        
        if (unknownVariable) {
            // var was an unknown variable found outside of a defined()
            result = "!defined(" + var + "_eq_0)";
            
        } else {
            // var is something along the lines of VAR_eq_0
            result = "defined(" + var + ")";
        }
        
        return result;
    }
    
    @Override
    public String toNonCppString() {
        String result;
        
        if (unknownVariable) {
            result = "!" + var + "_eq_0";
        } else {
            result = var;
        }
        
        return result;
    }
    
    @Override
    public Formula toFormula() {
        Formula result;
        
        if (unknownVariable) {
            result = new Negation(new net.ssehub.kernel_haven.util.logic.Variable(var + "_eq_0"));
        } else {
            result = new net.ssehub.kernel_haven.util.logic.Variable(var);
        }
        
        return result;
    }

}
