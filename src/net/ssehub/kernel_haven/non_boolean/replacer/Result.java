package net.ssehub.kernel_haven.non_boolean.replacer;

import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;

/**
 * A {@link Result} of an evaluation. This may be a tree of {@link BoolResult}s with other {@link Result}s in the
 * leaf-nodes.
 *
 * @author Adam
 */
abstract class Result {
    
    /**
     * Does a <code>&lt;</code> operation on this result on the left-hand side, and the given other result on the
     * right-hand side.
     * 
     * @param other The other result.
     * 
     * @return The result of applying the operation.
     * 
     * @throws ExpressionFormatException If this operation is not supported by this or other.
     */
    public abstract Result cmpLt(Result other) throws ExpressionFormatException;
    
    /**
     * Does a <code>&lt;=</code> operation on this result on the left-hand side, and the given other result on the
     * right-hand side.
     * 
     * @param other The other result.
     * 
     * @return The result of applying the operation.
     * 
     * @throws ExpressionFormatException If this operation is not supported by this or other.
     */
    public abstract Result cmpLe(Result other) throws ExpressionFormatException;
    
    /**
     * Does a <code>==</code> operation on this result on the left-hand side, and the given other result on the
     * right-hand side.
     * 
     * @param other The other result.
     * 
     * @return The result of applying the operation.
     * 
     * @throws ExpressionFormatException If this operation is not supported by this or other.
     */
    public abstract Result cmpEq(Result other) throws ExpressionFormatException;
    
    /**
     * Does a <code>+</code> operation on this result on the left-hand side, and the given other result on the
     * right-hand side.
     * 
     * @param other The other result.
     * 
     * @return The result of applying the operation.
     * 
     * @throws ExpressionFormatException If this operation is not supported by this or other.
     */
    public abstract Result add(Result other) throws ExpressionFormatException;
    
    /**
     * Does a <code>-</code> operation on this result on the left-hand side, and the given other result on the
     * right-hand side.
     * 
     * @param other The other result.
     * 
     * @return The result of applying the operation.
     * 
     * @throws ExpressionFormatException If this operation is not supported by this or other.
     */
    public abstract Result sub(Result other) throws ExpressionFormatException;
    
    /**
     * Does a <code>*</code> operation on this result on the left-hand side, and the given other result on the
     * right-hand side.
     * 
     * @param other The other result.
     * 
     * @return The result of applying the operation.
     * 
     * @throws ExpressionFormatException If this operation is not supported by this or other.
     */
    public abstract Result mul(Result other) throws ExpressionFormatException;
    
    /**
     * Does a <code>/</code> operation on this result on the left-hand side, and the given other result on the
     * right-hand side.
     * 
     * @param other The other result.
     * 
     * @return The result of applying the operation.
     * 
     * @throws ExpressionFormatException If this operation is not supported by this or other.
     */
    public abstract Result div(Result other) throws ExpressionFormatException;
    
    /**
     * Does a <code>%</code> operation on this result on the left-hand side, and the given other result on the
     * right-hand side.
     * 
     * @param other The other result.
     * 
     * @return The result of applying the operation.
     * 
     * @throws ExpressionFormatException If this operation is not supported by this or other.
     */
    public abstract Result mod(Result other) throws ExpressionFormatException;
    
    /**
     * Does a unary <code>-</code> operation on this result.
     * 
     * @return The result of applying the operation.
     * 
     * @throws ExpressionFormatException If this operation is not supported by this.
     */
    public abstract Result subUnary() throws ExpressionFormatException;
    
    /**
     * Does a <code>&amp;</code> operation on this result on the left-hand side, and the given other result on the
     * right-hand side.
     * 
     * @param other The other result.
     * 
     * @return The result of applying the operation.
     * 
     * @throws ExpressionFormatException If this operation is not supported by this or other.
     */
    public abstract Result binAnd(Result other) throws ExpressionFormatException;
    
    /**
     * Does a <code>|</code> operation on this result on the left-hand side, and the given other result on the
     * right-hand side.
     * 
     * @param other The other result.
     * 
     * @return The result of applying the operation.
     * 
     * @throws ExpressionFormatException If this operation is not supported by this or other.
     */
    public abstract Result binOr(Result other) throws ExpressionFormatException;
    
    /**
     * Does a <code>^</code> operation on this result on the left-hand side, and the given other result on the
     * right-hand side.
     * 
     * @param other The other result.
     * 
     * @return The result of applying the operation.
     * 
     * @throws ExpressionFormatException If this operation is not supported by this or other.
     */
    public abstract Result binXor(Result other) throws ExpressionFormatException;
    
    /**
     * Does a unary <code>~</code> operation on this result.
     * 
     * @return The result of applying the operation.
     * 
     * @throws ExpressionFormatException If this operation is not supported by this.
     */
    public abstract Result binInv() throws ExpressionFormatException;
    
    /**
     * Turns this {@link Result} (or tree of {@link Result}s) into a CPP compatible string.
     * 
     * @return A string representation of this {@link Result}.
     */
    public abstract String toCppString();
    
    @Override
    public String toString() {
        return toCppString();
    }

}
