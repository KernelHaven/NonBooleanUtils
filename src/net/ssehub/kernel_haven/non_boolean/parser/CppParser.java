package net.ssehub.kernel_haven.non_boolean.parser;

import static net.ssehub.kernel_haven.non_boolean.parser.CppOperator.BIN_AND;
import static net.ssehub.kernel_haven.non_boolean.parser.CppOperator.BIN_INV;
import static net.ssehub.kernel_haven.non_boolean.parser.CppOperator.BIN_OR;
import static net.ssehub.kernel_haven.non_boolean.parser.CppOperator.BIN_SHL;
import static net.ssehub.kernel_haven.non_boolean.parser.CppOperator.BIN_SHR;
import static net.ssehub.kernel_haven.non_boolean.parser.CppOperator.BIN_XOR;
import static net.ssehub.kernel_haven.non_boolean.parser.CppOperator.BOOL_AND;
import static net.ssehub.kernel_haven.non_boolean.parser.CppOperator.BOOL_NOT;
import static net.ssehub.kernel_haven.non_boolean.parser.CppOperator.BOOL_OR;
import static net.ssehub.kernel_haven.non_boolean.parser.CppOperator.CMP_EQ;
import static net.ssehub.kernel_haven.non_boolean.parser.CppOperator.CMP_GE;
import static net.ssehub.kernel_haven.non_boolean.parser.CppOperator.CMP_GT;
import static net.ssehub.kernel_haven.non_boolean.parser.CppOperator.CMP_LE;
import static net.ssehub.kernel_haven.non_boolean.parser.CppOperator.CMP_LT;
import static net.ssehub.kernel_haven.non_boolean.parser.CppOperator.CMP_NE;
import static net.ssehub.kernel_haven.non_boolean.parser.CppOperator.INT_ADD;
import static net.ssehub.kernel_haven.non_boolean.parser.CppOperator.INT_ADD_UNARY;
import static net.ssehub.kernel_haven.non_boolean.parser.CppOperator.INT_DEC;
import static net.ssehub.kernel_haven.non_boolean.parser.CppOperator.INT_DIV;
import static net.ssehub.kernel_haven.non_boolean.parser.CppOperator.INT_INC;
import static net.ssehub.kernel_haven.non_boolean.parser.CppOperator.INT_MOD;
import static net.ssehub.kernel_haven.non_boolean.parser.CppOperator.INT_MUL;
import static net.ssehub.kernel_haven.non_boolean.parser.CppOperator.INT_SUB;
import static net.ssehub.kernel_haven.non_boolean.parser.CppOperator.INT_SUB_UNARY;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import net.ssehub.kernel_haven.non_boolean.NumberUtils;
import net.ssehub.kernel_haven.non_boolean.parser.ast.CppExpression;
import net.ssehub.kernel_haven.non_boolean.parser.ast.ExpressionList;
import net.ssehub.kernel_haven.non_boolean.parser.ast.FunctionCall;
import net.ssehub.kernel_haven.non_boolean.parser.ast.ICppExressionVisitor;
import net.ssehub.kernel_haven.non_boolean.parser.ast.Variable;
import net.ssehub.kernel_haven.non_boolean.parser.ast.IntegerLiteral;
import net.ssehub.kernel_haven.non_boolean.parser.ast.Operator;
import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * A parser for expressions in #if, #elif, etc. of the C preprocessor (CPP).
 *
 * @author Adam
 */
public class CppParser {
    
    /**
     * Finds unary versions of {@link CppOperator#INT_ADD} and {@link CppOperator#INT_SUB}.
     *
     * @author Adam
     */
    private static class UnaryOperatorFinder implements ICppExressionVisitor<CppExpression> {

        @Override
        public CppExpression visitExpressionList(ExpressionList expressionList) throws ExpressionFormatException {
            ExpressionList newList = new ExpressionList();
            
            for (int i = 0; i < expressionList.getExpressionSize(); i++) {
                CppExpression currentExpr = expressionList.getExpression(i);
                
                if (currentExpr instanceof Operator) {
                    CppOperator currentOp = ((Operator) currentExpr).getOperator();
                    if (currentOp == INT_ADD || currentOp == INT_SUB) {
                        
                        // if we are at the very left, or there is another operator to our left, then this is unary
                        if (i == 0 || expressionList.getExpression(i - 1) instanceof Operator) {
                            CppOperator newOp;
                            if (currentOp == INT_ADD) {
                                newOp = INT_ADD_UNARY;
                            } else {
                                newOp = INT_SUB_UNARY;
                            }
                            currentExpr = new Operator(newOp);
                        }
                    }
                }
                
                newList.addExpression(currentExpr.accept(this));
            }
            return newList;
        }

        @Override
        public CppExpression visitFunctionCall(FunctionCall call) throws ExpressionFormatException {
            CppExpression arg = call.getArgument();
            if (arg != null) {
                arg = arg.accept(this);
            }
            return new FunctionCall(call.getFunctionName(), arg);
        }

        @Override
        public CppExpression visitVariable(Variable variable) throws ExpressionFormatException {
            return variable;
        }

        @Override
        public CppExpression visitOperator(Operator operator) throws ExpressionFormatException {
            return operator;
        }

        @Override
        public CppExpression visitLiteral(IntegerLiteral literal) throws ExpressionFormatException {
            return literal;
        }
        
    }
    
    /**
     * Finds "variable" which are actually integer literals.
     */
    private static class LiteralFinder implements ICppExressionVisitor<CppExpression> {

        private String expression;
        
        /**
         * Creates a new {@link LiteralFinder}.
         * 
         * @param expression The currently parsed expression. Used for error messages.
         */
        public LiteralFinder(String expression) {
            this.expression = expression;
        }
        
        @Override
        public CppExpression visitExpressionList(ExpressionList expressionList) throws ExpressionFormatException {
            ExpressionList newList = new ExpressionList();
            
            for (int i = 0; i < expressionList.getExpressionSize(); i++) {
                newList.addExpression(expressionList.getExpression(i).accept(this));
            }
            
            return newList;
        }

        @Override
        public CppExpression visitFunctionCall(FunctionCall call) throws ExpressionFormatException {
            CppExpression arg = call.getArgument();
            if (arg != null) {
                arg = arg.accept(this);
            }
            return new FunctionCall(call.getFunctionName(), arg);
        }

        @Override
        public CppExpression visitVariable(Variable variable) throws ExpressionFormatException {
            CppExpression result = variable;
            if (Character.isDigit(variable.getName().charAt(0))) {
                String literal = variable.getName().toLowerCase();
                if (literal.endsWith("ul")) {
                    literal = literal.substring(0, literal.length() - 2);
                } else if (literal.endsWith("l")) {
                    literal = literal.substring(0, literal.length() - 1);
                }
                try {
                    Number value = NumberUtils.convertToNumber(literal);
                    if (value == null || !(value instanceof Long)) {
                        throw new NumberFormatException();
                    }
                    result = new IntegerLiteral((Long) value);
                } catch (NumberFormatException e) {
                    throw makeException(expression, "Cannot parse literal " + variable.getName());
                }
            }
            return result;
        }

        @Override
        public CppExpression visitOperator(Operator operator) throws ExpressionFormatException {
            return operator;
        }

        @Override
        public CppExpression visitLiteral(IntegerLiteral literal) throws ExpressionFormatException {
            return literal;
        }
        
    }
    
    /**
     * A visitor that finds functions calls (variables followed by expression list (brackets)).
     */
    private static class FunctionCallTranslator implements ICppExressionVisitor<CppExpression> {
        
        @Override
        public CppExpression visitExpressionList(ExpressionList expressionList) throws ExpressionFormatException {
            ExpressionList newList = new ExpressionList();
            
            for (int i = 0; i < expressionList.getExpressionSize(); i++) {
                CppExpression currentExpr = expressionList.getExpression(i);
                boolean foundFunction = false;
                
                if (currentExpr instanceof Variable && i + 1 < expressionList.getExpressionSize()) {
                    CppExpression nextExpr = expressionList.getExpression(i + 1);
                    if (nextExpr instanceof ExpressionList) {
                        foundFunction = true;
                        nextExpr = nextExpr.accept(this);
                        String name = ((Variable) currentExpr).getName();
                        
                        ExpressionList nextExprList = (ExpressionList) nextExpr;
                        // unpack argument list if we only have one element
                        if (nextExprList.getExpressionSize() == 1) {
                            nextExpr = nextExprList.getExpression(0);
                            
                        } else if (nextExprList.getExpressionSize() == 0) {
                            // function with no arguments
                            nextExpr = null;
                        }
                        
                        newList.addExpression(new FunctionCall(name, nextExpr));
                        i++; // increment, since we already handled nextExpr
                    }
                }
                
                if (!foundFunction) {
                    newList.addExpression(currentExpr.accept(this));
                }
                
            }
            
            return newList;
        }

        @Override
        public CppExpression visitFunctionCall(FunctionCall call) throws ExpressionFormatException {
            throw new ExpressionFormatException("This code cannot be reached");
        }

        @Override
        public CppExpression visitVariable(Variable variable) throws ExpressionFormatException {
            return variable;
        }

        @Override
        public CppExpression visitOperator(Operator operator) throws ExpressionFormatException {
            return operator;
        }

        @Override
        public CppExpression visitLiteral(IntegerLiteral literal) throws ExpressionFormatException {
            return literal;
        }
        
    }
    
    /**
     * Finds and sets the left and right side for the operators.
     */
    private static class OperatorResolver implements ICppExressionVisitor<CppExpression> {

        private String expression;
        
        /**
         * Creates a new {@link OperatorResolver}.
         * 
         * @param expression The currently parsed expression. Used for error messages.
         */
        public OperatorResolver(String expression) {
            this.expression = expression;
        }
        
        @Override
        public CppExpression visitExpressionList(ExpressionList expressionList) throws ExpressionFormatException {
            CppExpression result;
            
            if (expressionList.getExpressionSize() == 0) {
                throw makeException(expression, "Expected variable");
                
            } else if (expressionList.getExpressionSize() == 1) {
                result = expressionList.getExpression(0).accept(this); 
                
            } else {
                int highestOpPos = -1;
                int highestOpPrecedence = Integer.MAX_VALUE;
                CppOperator highestOp = null;
                Operator highestOpNode = null;
                
                for (int i = 0; i < expressionList.getExpressionSize(); i++) {
                    CppExpression currentExpr = expressionList.getExpression(i);
                    
                    if (currentExpr instanceof Operator) {
                        CppOperator op = ((Operator) currentExpr).getOperator();
                        
                        if (op.getPrecedence() < highestOpPrecedence) {
                            highestOp = op;
                            highestOpPos = i;
                            highestOpPrecedence = op.getPrecedence();
                            highestOpNode = (Operator) currentExpr;
                        }
                    }
                }
                
                if (highestOp == null) {
                    throw makeException(expression, "Couldn't find operator");
                    
                } else {
                    parseParameters(expressionList, highestOpNode, highestOpPos);
                }
                
                
                result = highestOpNode;
            }
            
            return result;
        }
        
        /**
         * Parses the arguments (left and right for binary, only one side for unary operators) and adds them to the
         * given operator.
         * 
         * @param expressionList The {@link ExpressionList} containing all nodes of the current nesting depth.
         * @param operator The operator to parse the parameters for.
         * @param operatorIndex The index of the operator in <code>expressionList</code>.
         * 
         * @throws ExpressionFormatException If finding the parameters fails.
         */
        private void parseParameters(ExpressionList expressionList, Operator operator, int operatorIndex)
                throws ExpressionFormatException {
            
            if (operator.getOperator().isUnary()) {
                if (operatorIndex != 0
                        // special case: ++ and -- may be on right side
                        && !(
                                (operator.getOperator() == INT_DEC || operator.getOperator() == INT_INC)
                                && operatorIndex == expressionList.getExpressionSize() - 1
                            )) {
                        
                    throw makeException(expression, "Found elements on wrong side of unary operator");
                        
                } else {
                    ExpressionList nested = new ExpressionList();
                    for (int i = 0; i < expressionList.getExpressionSize(); i++) {
                        if (i != operatorIndex) {
                            nested.addExpression(expressionList.getExpression(i));
                        }
                    }
                    operator.setLeftSide(nested.accept(this));
                    
                }
            } else {
                if (operatorIndex == 0 || operatorIndex == expressionList.getExpressionSize() - 1) {
                    throw makeException(expression, "Didn't find elements on both sides of binary operator");
                    
                } else {
                    ExpressionList leftSide = new ExpressionList();
                    ExpressionList rightSide = new ExpressionList();
                    for (int i = 0; i < expressionList.getExpressionSize(); i++) {
                        if (i < operatorIndex) {
                            leftSide.addExpression(expressionList.getExpression(i));
                            
                        } else if (i > operatorIndex) {
                            rightSide.addExpression(expressionList.getExpression(i));
                        }
                    }
                    
                    operator.setLeftSide(leftSide.accept(this));
                    operator.setRightSide(rightSide.accept(this));

                }
            }
        }

        @Override
        public CppExpression visitFunctionCall(FunctionCall call) throws ExpressionFormatException {
            CppExpression arg = call.getArgument();
            if (arg != null) {
                arg = arg.accept(this);
            }
            return new FunctionCall(call.getFunctionName(), arg);
        }

        @Override
        public CppExpression visitVariable(Variable variable) throws ExpressionFormatException {
            return variable;
        }

        @Override
        public CppExpression visitOperator(Operator operator) throws ExpressionFormatException {
            return operator;
        }

        @Override
        public CppExpression visitLiteral(IntegerLiteral literal) throws ExpressionFormatException {
            return literal;
        }
        
    }
    
    /**
     * Partially parses the given CPP expression. The resulting AST only has bracket hierarchies and function calls
     * resolved.
     * 
     * @param expression The expression to parse.
     * @return A (partially) parsed AST for the given expression.
     * 
     * @throws ExpressionFormatException If the expression is malformed.
     */
    public CppExpression parse(String expression) throws ExpressionFormatException {
        CppToken[] tokens = lex(expression);
        
        Deque<ExpressionList> expressionListStack = new LinkedList<>();
        expressionListStack.push(new ExpressionList());
        
        for (int tokenIndex = 0; tokenIndex < tokens.length; tokenIndex++) {
            
            CppToken currentToken = tokens[tokenIndex];
            
            if (currentToken instanceof Bracket) {
                Bracket currentBracket = (Bracket) currentToken;
                
                if (currentBracket.isOpening()) {
                    ExpressionList newList = new ExpressionList();
                    expressionListStack.peek().addExpression(newList);
                    expressionListStack.push(newList);
                    
                } else {
                    expressionListStack.pop();
                    if (expressionListStack.size() < 1) {
                        throw makeException(expression, "Unbalanced brackets (too many closing)",
                                currentToken.getPos());
                    }
                }
                
            } else if (currentToken instanceof IdentifierToken) {
                expressionListStack.peek().addExpression(new Variable(((IdentifierToken) currentToken).getName()));
                
            } else if (currentToken instanceof OperatorToken) {
                expressionListStack.peek().addExpression(new Operator(((OperatorToken) currentToken).getOperator()));
                
            } else {
                throw makeException(expression, "Unexpected token: " + currentToken, currentToken.getPos());
            }
            
        }
        
        if (expressionListStack.size() != 1) {
            CppToken lastToken = tokens[tokens.length - 1];
            throw makeException(expression, "Unbalanced brackets (missing closing)",
                    lastToken.getPos() + lastToken.getLength());
        }
        
        ExpressionList resultList = expressionListStack.pop();
        CppExpression result = resultList;
        // unpack surrounding list if we only have one element
        if (resultList.getExpressionSize() == 1) {
            result = resultList.getExpression(0);
        }
        
        result = result.accept(new LiteralFinder(expression));
        result = result.accept(new FunctionCallTranslator());
        result = result.accept(new UnaryOperatorFinder());
        result = result.accept(new OperatorResolver(expression));
        
        return result;
    }
    
    /**
     * Lex (tokenize) the given expression. Package visibility for test cases.
     * 
     * @param expression The expression to turn into tokens.
     * @return The tokens.
     * 
     * @throws ExpressionFormatException If invalid characters appear in the expression.
     */
    CppToken[] lex(String expression) throws ExpressionFormatException {
        List<CppToken> tokens = new ArrayList<>(50);
        
        IdentifierToken currentIdentifier = null;
        
        char[] expr = expression.toCharArray();
        // iterate over the string; i is incremented based on which token was identified
        for (int exprPos = 0; exprPos < expr.length;) {
            
            CppOperator op = getOperator(expr, exprPos);
            
            if (isWhitespace(expr, exprPos)) {
                currentIdentifier = null;
                exprPos++;
                
            } else if (isOpeningBracket(expr, exprPos)) {
                currentIdentifier = null;
                tokens.add(new Bracket(exprPos, false));
                exprPos++;
                
            } else if (isClosingBracket(expr, exprPos)) {
                currentIdentifier = null;
                tokens.add(new Bracket(exprPos, true));
                exprPos++;
                
            } else if (op != null) {
                currentIdentifier = null;
                tokens.add(new OperatorToken(exprPos, op));
                exprPos += op.getSymbol().length();
                
            } else if (isIdentifierChar(expr, exprPos)) {
                if (currentIdentifier == null) {
                    currentIdentifier = new IdentifierToken(exprPos, "");
                    tokens.add(currentIdentifier);
                }
                currentIdentifier.setName(currentIdentifier.getName() + expr[exprPos]);
                exprPos++;
                
            } else {
                throw makeException(expression, "Invalid character in expression: '" + expr[exprPos] + "'", exprPos);
            }
        }
        
        return tokens.toArray(new CppToken[0]);
    }
    
    /**
     * Whether the given character is a whitespace.
     * 
     * @param expr An array containing characters.
     * @param exprPos Which character in the array should be checked.
     * 
     * @return Whether the given character is a whitespace.
     */
    private boolean isWhitespace(char[] expr, int exprPos) {
        return Character.isWhitespace(expr[exprPos]);
    }
    
    /**
     * Whether the given character is an opening bracket.
     * 
     * @param expr An array containing characters.
     * @param exprPos Which character in the array should be checked.
     * 
     * @return Whether the given character is an opening bracket.
     */
    private boolean isOpeningBracket(char[] expr, int exprPos) {
        return safeCheckChar(expr, exprPos, '(');
    }
    
    /**
     * Whether the given character is an closing bracket.
     * 
     * @param expr An array containing characters.
     * @param exprPos Which character in the array should be checked.
     * 
     * @return Whether the given character is an closing bracket.
     */
    private boolean isClosingBracket(char[] expr, int exprPos) {
        return safeCheckChar(expr, exprPos, ')');
    }
    
    /**
     * Returns the operator that is found the given position in the given array.
     * 
     * @param expr The expression that the operator is searched in.
     * @param exprPos The position in the expression where the operator is at.
     * 
     * @return The operator at the given position, or <code>null</code> if there is no operator.
     */
    private CppOperator getOperator(char[] expr, int exprPos) {
        CppOperator result = null;
        
        /*
         * Double char operators
         */
        if (safeCheckChar(expr, exprPos, '&') && safeCheckChar(expr, exprPos + 1, '&')) {
            result = BOOL_AND;
        } else if (safeCheckChar(expr, exprPos, '|') && safeCheckChar(expr, exprPos + 1, '|')) {
            result = BOOL_OR;
            
        } else if (safeCheckChar(expr, exprPos, '+') && safeCheckChar(expr, exprPos + 1, '+')) {
            result = INT_INC;
        } else if (safeCheckChar(expr, exprPos, '-') && safeCheckChar(expr, exprPos + 1, '-')) {
            result = INT_DEC;
            
        } else if (safeCheckChar(expr, exprPos, '=') && safeCheckChar(expr, exprPos + 1, '=')) {
            result = CMP_EQ;
        } else if (safeCheckChar(expr, exprPos, '!') && safeCheckChar(expr, exprPos + 1, '=')) {
            result = CMP_NE;
        } else if (safeCheckChar(expr, exprPos, '<') && safeCheckChar(expr, exprPos + 1, '=')) {
            result = CMP_LE;
        } else if (safeCheckChar(expr, exprPos, '>') && safeCheckChar(expr, exprPos + 1, '=')) {
            result = CMP_GE;
            
        } else if (safeCheckChar(expr, exprPos, '>') && safeCheckChar(expr, exprPos + 1, '>')) {
            result = BIN_SHR;
        } else if (safeCheckChar(expr, exprPos, '<') && safeCheckChar(expr, exprPos + 1, '<')) {
            result = BIN_SHL;
            
        /*
         *  single char operators
         */
        } else if (safeCheckChar(expr, exprPos, '!')) {
            result = BOOL_NOT;
            
        } else if (safeCheckChar(expr, exprPos, '+')) {
            result = INT_ADD;
        } else if (safeCheckChar(expr, exprPos, '-')) {
            result = INT_SUB;
        } else if (safeCheckChar(expr, exprPos, '*')) {
            result = INT_MUL;
        } else if (safeCheckChar(expr, exprPos, '/')) {
            result = INT_DIV;
        } else if (safeCheckChar(expr, exprPos, '%')) {
            result = INT_MOD;
            
        } else if (safeCheckChar(expr, exprPos, '<')) {
            result = CMP_LT;
        } else if (safeCheckChar(expr, exprPos, '>')) {
            result = CMP_GT;
            
        } else if (safeCheckChar(expr, exprPos, '&')) {
            result = BIN_AND;
        } else if (safeCheckChar(expr, exprPos, '|')) {
            result = BIN_OR;
        } else if (safeCheckChar(expr, exprPos, '^')) {
            result = BIN_XOR;
        } else if (safeCheckChar(expr, exprPos, '~')) {
            result = BIN_INV;
        }
        
        return result;
    }
    
    /**
     * Whether the given character is a valid identifier character.
     * 
     * @param expr An array containing characters.
     * @param exprPos Which character in the array should be checked.
     * 
     * @return Whether the given character is a valid identifier character.
     */
    private boolean isIdentifierChar(char[] expr, int exprPos) {
        // CHECKSTYLE:OFF
        // checkstyle thinks that this boolean formula is too complex;
        // but we need it this way because every other option is not as
        // performant, and this
        // is a rather performance critical code path.
        return (expr[exprPos] >= 'a' && expr[exprPos] <= 'z') || (expr[exprPos] >= 'A' && expr[exprPos] <= 'Z')
                || (expr[exprPos] >= '0' && expr[exprPos] <= '9') || (expr[exprPos] == '_');
        // CHECKSTYLE:ON
    }
    
    /**
     * Safely checks if the character at the given position in the array is equal to the given character.
     * 
     * @param expr The array to check in.
     * @param exprPos The position to check in the array. May be out of bounds.
     * @param check The character that should be checked against.
     * 
     * @return <code>true</code> if <code>check</code> is at the array in the given position; <code>false</code> if not
     *      or <code>exprPos</code> is out of bounds.
     */
    private static boolean safeCheckChar(char[] expr, int exprPos, char check) {
        boolean result = false;
        if (exprPos >= 0 && exprPos < expr.length) {
            result = expr[exprPos] == check;
        }
        return result;
    }
    
    /**
     * Creates an {@link ExpressionFormatException}.
     * 
     * @param expression The expression that couldn't be parsed.
     * @param message The message describing the exception.
     * @param markers The indices for characters in the expression where markers should be displayed. Must be sorted
     *      ascending.
     * 
     * @return The exception with the proper error message.
     */
    private static @NonNull ExpressionFormatException makeException(@NonNull String expression, @NonNull String message,
            int ... markers) {
        
        StringBuilder fullMessage = new StringBuilder(message).append("\nIn formula: ").append(expression);
        if (markers.length > 0) {
            fullMessage.append("\n            ");
            int markersIndex = 0;
            for (int i = 0; markersIndex < markers.length; i++) {
                if (i == markers[markersIndex]) {
                    fullMessage.append('^');
                    markersIndex++;
                    
                } else {
                    fullMessage.append(' ');
                }
            }
        }
        return new ExpressionFormatException(fullMessage.toString());
    }
    
}
