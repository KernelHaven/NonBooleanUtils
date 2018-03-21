package net.ssehub.kernel_haven.non_boolean.replacer;

import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.not;

import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;

/**
 * A variable with a string name. This can be an unknown variable, in which case {@link #isUnknownVariable()} returns
 * true. An unknown variable can still be the target of comparisons. If this is not unknown, then this variable name
 * is probably in the form of <code>VAR_eq_2</code> (except this was created as a result of a defined() call).
 *
 * @author Adam
 */
class VariableResult extends Result {
    
    /**
     * Types of {@link VariableResult}s.
     */
    public static enum Type {
        
        /**
         * Represents an unknown variable. This allows comparisons to be done on this result. If no comparison is done
         * on this, then !defined(VAR_eq_0) is created as the CPP string.
         */
        UNKNOWN,
        
        /**
         * Represents an infinite integer variable. This always results in defined(VAR) without an explicit value, no
         * matter what integer operations or comparisons are done on it. Only exception: Comparison with another single 
         * variable: this creates something like VAR1_eq_VAR2.
         */
        INFINITE,
        
        /**
         * A final result variable; this is the result of a comparison.
         * Something like defined(VAR_eq_2). No operations or comparisons can be done on this.
         */
        FINAL;
        
    }
    
    private String var;
    
    private Type type;
    
    /**
     * Creates a new (not unknown) variable result with the given name. No operations can be done on this anymore.
     * 
     * @param var The name of this variable.
     * @param type The {@link Type} of this variable.
     */
    public VariableResult(String var, Type type) {
        this.var = var;
        this.type = type;
    }
    
    /**
     * The {@link Type} of this variable.
     * 
     * @return The type of this variable.
     */
    public Type getType() {
        return type;
    }
    
    /**
     * Overrides the {@link Type} of this variable.
     * 
     * @param type The new type of this variable.
     */
    public void setType(Type type) {
        this.type = type;
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
        if (type == Type.FINAL) {
            throw new ExpressionFormatException("Can't apply operator < or > on final VariableResult");
        }
        
        Result result;
        if (other instanceof LiteralIntResult) {
            LiteralIntResult lit = (LiteralIntResult) other;
            if (this.type == Type.UNKNOWN) {
                this.var = var + "_lt_" + lit.getValue();
            }
            // no change for type==INFINITE
            
            this.type = Type.FINAL;
            result = this;
            
        } else if (other instanceof VariableResult && ((VariableResult) other).type != Type.FINAL) {
            VariableResult o = (VariableResult) other;
            this.var = var + "_lt_" + o.getVar();
            this.type = Type.FINAL;
            result = this;
            
        } else if (other instanceof VariablesWithValues && ((VariablesWithValues) other).getNumVars() == 1) {
            VariablesWithValues o = (VariablesWithValues) other;
            this.var = o.getVarName(0) + "_gt_" + var;
            this.type = Type.FINAL;
            result  = this;
            
        } else {
            throw new ExpressionFormatException("Can't apply operator < or > on " + this.type + " VariableResult and "
                    + other.getClass().getSimpleName());
        }
        return result;
    }
    
    @Override
    public Result cmpLe(Result other) throws ExpressionFormatException {
        if (type == Type.FINAL) {
            throw new ExpressionFormatException("Can't apply operator <= or => on final VariableResult");
        }
        
        Result result;
        if (other instanceof LiteralIntResult) {
            LiteralIntResult lit = (LiteralIntResult) other;
            if (this.type == Type.UNKNOWN) {
                this.var = var + "_le_" + lit.getValue();
            }
            // no change for type==INFINITE
            
            this.type = Type.FINAL;
            result = this;
            
        } else if (other instanceof VariableResult && ((VariableResult) other).type != Type.FINAL) {
            VariableResult o = (VariableResult) other;
            this.var = var + "_le_" + o.getVar();
            this.type = Type.FINAL;
            result = this;
            
        } else if (other instanceof VariablesWithValues && ((VariablesWithValues) other).getNumVars() == 1) {
            VariablesWithValues o = (VariablesWithValues) other;
            this.var = o.getVarName(0) +  "_ge_" + var;
            this.type = Type.FINAL;
            result = this;
            
        } else {
            throw new ExpressionFormatException("Can't apply operator <= or >= on " + this.type + " VariableResult and "
                    + other.getClass().getSimpleName());
        }
        return result;
    }
    
    @Override
    public Result cmpEq(Result other) throws ExpressionFormatException {
        if (type == Type.FINAL) {
            throw new ExpressionFormatException("Can't apply operator == or != on final VariableResult");
        }
        
        Result result;
        if (other instanceof LiteralIntResult) {
            LiteralIntResult lit = (LiteralIntResult) other;
            if (this.type == Type.UNKNOWN) {
                this.var = var + "_eq_" + lit.getValue();
            }
            // no change for type==INFINITE
            
            this.type = Type.FINAL;
            result = this;
            
        } else if (other instanceof VariableResult && ((VariableResult) other).type != Type.FINAL) {
            VariableResult o = (VariableResult) other;
            this.var = var + "_eq_" + o.getVar();
            this.type = Type.FINAL;
            result = this;
            
        } else if (other instanceof VariablesWithValues  && ((VariablesWithValues) other).getNumVars() == 1) {
            VariablesWithValues o = (VariablesWithValues) other;
            this.var = o.getVarName(0) +  "_eq_" + var;
            this.type = Type.FINAL;
            result = this;
            
        } else {
            throw new ExpressionFormatException("Can't apply operator == or != on " + this.type + " VariableResult and "
                    + other.getClass().getSimpleName());
        }
        return result;
    }
    
    @Override
    public Result subUnary() throws ExpressionFormatException {
        if (type == Type.INFINITE) {
            // integer operations have no effect on infinite variables
            return this;
        }
        throw new ExpressionFormatException("Can't apply operator unary subtraction on VariableResult");
    }
    
    @Override
    public Result add(Result other) throws ExpressionFormatException {
        if (type == Type.INFINITE) {
            // integer operations have no effect on infinite variables
            return this;
        }
        throw new ExpressionFormatException("Can't apply operator + on VariableResult");
    }
    
    @Override
    public Result sub(Result other) throws ExpressionFormatException {
        if (type == Type.INFINITE) {
            // integer operations have no effect on infinite variables
            return this;
        }
        throw new ExpressionFormatException("Can't apply operator - on VariableResult");
    }
    
    @Override
    public Result mul(Result other) throws ExpressionFormatException {
        if (type == Type.INFINITE) {
            // integer operations have no effect on infinite variables
            return this;
        }
        throw new ExpressionFormatException("Can't apply operator * on VariableResult");
    }
    
    @Override
    public Result div(Result other) throws ExpressionFormatException {
        if (type == Type.INFINITE) {
            // integer operations have no effect on infinite variables
            return this;
        }
        throw new ExpressionFormatException("Can't apply operator / on VariableResult");
    }
    
    @Override
    public Result mod(Result other) throws ExpressionFormatException {
        if (type == Type.INFINITE) {
            // integer operations have no effect on infinite variables
            return this;
        }
        throw new ExpressionFormatException("Can't apply operator % on VariableResult");
    }
    
    @Override
    public Result binAnd(Result other) throws ExpressionFormatException {
        if (type == Type.INFINITE) {
            // integer operations have no effect on infinite variables
            return this;
        }
        throw new ExpressionFormatException("Can't apply operator & on VariableResult");
    }
    @Override
    public Result binOr(Result other) throws ExpressionFormatException {
        if (type == Type.INFINITE) {
            // integer operations have no effect on infinite variables
            return this;
        }
        throw new ExpressionFormatException("Can't apply operator | on VariableResult");
    }
    @Override
    public Result binXor(Result other) throws ExpressionFormatException {
        if (type == Type.INFINITE) {
            // integer operations have no effect on infinite variables
            return this;
        }
        throw new ExpressionFormatException("Can't apply operator ^ on VariableResult");
    }
    @Override
    public Result binInv() throws ExpressionFormatException {
        if (type == Type.INFINITE) {
            // integer operations have no effect on infinite variables
            return this;
        }
        throw new ExpressionFormatException("Can't apply operator ~ on VariableResult");
    }
    
    @Override
    public String toCppString() {
        String result;
        
        if (type == Type.UNKNOWN) {
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
        
        if (type == Type.UNKNOWN) {
            result = "!" + var + "_eq_0";
        } else {
            result = var;
        }
        
        return result;
    }
    
    @Override
    public Formula toFormula() {
        Formula result;
        
        if (type == Type.UNKNOWN) {
            result = not(var + "_eq_0");
        } else {
            result = new net.ssehub.kernel_haven.util.logic.Variable(var);
        }
        
        return result;
    }

}
