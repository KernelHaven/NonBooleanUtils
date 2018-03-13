package net.ssehub.kernel_haven.non_boolean.replacer;

import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;

public class VariableResult extends Result {
    
    private String var;
    
    private boolean unknownVariable;
    
    public VariableResult(String var) {
        this.var = var;
    }
    
    public VariableResult(String var, boolean unknownVariable) {
        this.var = var;
        this.unknownVariable = unknownVariable;
    }
    
    @Override
    public Result cmpLt(Result other) throws ExpressionFormatException {
        if (!unknownVariable) {
            throw new ExpressionFormatException("Can't apply operator < or > on VariableResult");
        }
        
        Result result;
        if (other instanceof LiteralIntResult) {
            LiteralIntResult lit = (LiteralIntResult) other;
            result = new VariableResult(var + "_lt_" + lit.getValue());
            
        } else if (other instanceof VariableResult && ((VariableResult) other).unknownVariable) {
            VariableResult o = (VariableResult) other;
            result = new VariableResult(var + "_lt_" + o.getVar());
            
        } else if (other instanceof VariableWithValues) {
            VariableWithValues o = (VariableWithValues) other;
            result = new VariableResult(o.getVar() + "_gt_" + var);
            
        } else {
            throw new ExpressionFormatException("Can't apply operator < or > on " + other.getClass().getSimpleName());
        }
        return result;
    }
    
    public String getVar() {
        return var;
    }

    @Override
    public Result cmpLe(Result other) throws ExpressionFormatException {
        if (!unknownVariable) {
            throw new ExpressionFormatException("Can't apply operator <= or => on VariableResult");
        }
        
        Result result;
        if (other instanceof LiteralIntResult) {
            LiteralIntResult lit = (LiteralIntResult) other;
            result = new VariableResult(var + "_le_" + lit.getValue());
            
        } else if (other instanceof VariableResult && ((VariableResult) other).unknownVariable) {
            VariableResult o = (VariableResult) other;
            result = new VariableResult(var + "_le_" + o.getVar());
            
        } else if (other instanceof VariableWithValues) {
            VariableWithValues o = (VariableWithValues) other;
            result = new VariableResult(o.getVar() +  "_ge_" + var);
            
        } else {
            throw new ExpressionFormatException("Can't apply operator <= or >= on " + other.getClass().getSimpleName());
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
            result = new VariableResult(var + "_eq_" + lit.getValue());
            
        } else if (other instanceof VariableResult && ((VariableResult) other).unknownVariable) {
            VariableResult o = (VariableResult) other;
            result = new VariableResult(var + "_eq_" + o.getVar());
            
        } else if (other instanceof VariableWithValues) {
            VariableWithValues o = (VariableWithValues) other;
            result = new VariableResult(o.getVar() +  "_eq_" + var);
            
        } else {
            throw new ExpressionFormatException("Can't apply operator == or != on " + other.getClass().getSimpleName());
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

}
