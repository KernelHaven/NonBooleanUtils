package net.ssehub.kernel_haven.non_boolean.replacer;

import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;

abstract class Result {
    
    public abstract Result cmpLt(Result other) throws ExpressionFormatException;
    public abstract Result cmpLe(Result other) throws ExpressionFormatException;
    public abstract Result cmpEq(Result other) throws ExpressionFormatException;
    
    public abstract Result add(Result other) throws ExpressionFormatException;
    public abstract Result sub(Result other) throws ExpressionFormatException;
    public abstract Result mul(Result other) throws ExpressionFormatException;
    public abstract Result div(Result other) throws ExpressionFormatException;
    public abstract Result mod(Result other) throws ExpressionFormatException;
    public abstract Result subUnary() throws ExpressionFormatException;
    
    public abstract Result binAnd(Result other) throws ExpressionFormatException;
    public abstract Result binOr(Result other) throws ExpressionFormatException;
    public abstract Result binXor(Result other) throws ExpressionFormatException;
    public abstract Result binInv() throws ExpressionFormatException;
    
    public abstract String toCppString();
    
    @Override
    public String toString() {
        return toCppString();
    }

}
