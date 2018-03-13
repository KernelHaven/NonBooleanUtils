package net.ssehub.kernel_haven.non_boolean.replacer;

import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;

/**
 * A boolean {@link Result}. This is an operator (&&, ||, !) or a {@link LiteralBoolResult}.
 *
 * @author Adam
 */
abstract class BoolResult extends Result {

    @Override
    public Result cmpLt(Result other) throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator < or > on boolean");
    }

    @Override
    public Result cmpLe(Result other) throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator <= or >= on boolean");
    }
    
    @Override
    public Result cmpEq(Result other) throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator == or != on boolean");
    }
    
    @Override
    public Result add(Result other) throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator + on boolean");
    }
    @Override
    public Result sub(Result other) throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator - on boolean");
    }
    @Override
    public Result mul(Result other) throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator * on boolean");
    }
    @Override
    public Result div(Result other) throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator / on boolean");
    }
    @Override
    public Result mod(Result other) throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator % on boolean");
    }
    
    @Override
    public Result subUnary() throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator unary subtraction on boolean");
    }

    @Override
    public Result binAnd(Result other) throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator & on boolean");
    }

    @Override
    public Result binOr(Result other) throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator | on boolean");
    }

    @Override
    public Result binXor(Result other) throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator ^ on boolean");
    }

    @Override
    public Result binInv() throws ExpressionFormatException {
        throw new ExpressionFormatException("Can't apply operator ~ on boolean");
    }

}
