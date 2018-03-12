package net.ssehub.kernel_haven.non_boolean.parser.ast;

/**
 * A visitor for the {@link CppExpression} AST.
 *
 * @param <T> The return type of this visitor.
 *
 * @author Adam
 */
public interface ICppExressionVisitor<T> {

    /**
     * Visits an {@link ExpressionList}. These kind of nodes only appear during parsing.
     * 
     * @param expressionList The expression list to visit.
     * 
     * @return Something.
     */
    public T visitExpressionList(ExpressionList expressionList);
    
    /**
     * Visits a {@link FunctionCall}.
     * 
     * @param call The function call to visit.
     * 
     * @return Something.
     */
    public T visitFunctionCall(FunctionCall call);
    
    /**
     * Visits a {@link Variable}.
     * 
     * @param variable The variable to visit.
     * 
     * @return Something.
     */
    public T visitVariable(Variable variable);
    
    /**
     * Visits an {@link Operator}.
     * 
     * @param operator The operator to visit.
     * 
     * @return Something.
     */
    public T visitOperator(Operator operator);
    
    /**
     * Visits an {@link IntegerLiteral}.
     * 
     * @param literal The literal to visit.
     * 
     * @return Something.
     */
    public T visitLiteral(IntegerLiteral literal);
    
}
