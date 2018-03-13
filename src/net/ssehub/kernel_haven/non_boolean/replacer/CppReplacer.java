package net.ssehub.kernel_haven.non_boolean.replacer;

import java.util.Map;

import net.ssehub.kernel_haven.non_boolean.NonBooleanPreperation.NonBooleanVariable;
import net.ssehub.kernel_haven.non_boolean.parser.CppParser;
import net.ssehub.kernel_haven.non_boolean.parser.ast.CppExpression;
import net.ssehub.kernel_haven.non_boolean.parser.ast.ExpressionList;
import net.ssehub.kernel_haven.non_boolean.parser.ast.FunctionCall;
import net.ssehub.kernel_haven.non_boolean.parser.ast.ICppExressionVisitor;
import net.ssehub.kernel_haven.non_boolean.parser.ast.IntegerLiteral;
import net.ssehub.kernel_haven.non_boolean.parser.ast.Operator;
import net.ssehub.kernel_haven.non_boolean.parser.ast.Variable;
import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;

public class CppReplacer {
    
    private Map<String, NonBooleanVariable> variables;
    
    private Map<String, Long> constants;
    
    private CppParser parser;
    
    public CppReplacer(Map<String, NonBooleanVariable> variables, Map<String, Long> constants) {
        this.variables = variables;
        this.constants = constants;
        this.parser = new CppParser();
    }

    public String replace(String cppLine) throws ExpressionFormatException {
        String expression = null;
        String prepend = null;
        
        for (String prefix : new String[] {"#if ", "#elif "}) {
            if (cppLine.startsWith(prefix)) {
                expression = cppLine.substring(prefix.length());
                prepend = prefix;
                break;
            }
        }
        if (expression == null) {
            throw new ExpressionFormatException("Line does not start with #if or #elif:\n" + cppLine);
        }
        
        CppExpression parsed = parser.parse(expression);
        Result result = parsed.accept(new AstEvaluator());
        cppLine = prepend + result.toCppString();
        
        return cppLine;
    }
    
    private class AstEvaluator implements ICppExressionVisitor<Result> {

        @Override
        public Result visitExpressionList(ExpressionList expressionList) throws ExpressionFormatException {
            throw new ExpressionFormatException("This code can't be reached.");
        }

        @Override
        public Result visitFunctionCall(FunctionCall call) throws ExpressionFormatException {
            Result result = null;
            if (call.getFunctionName().equals("defined") && call.getArgument() instanceof Variable) {
                result = new VariableResult(((Variable) call.getArgument()).getName());
                
            } else {
                String argumentClass = "null";
                if (call.getArgument() != null) {
                    argumentClass = call.getArgument().getClass().getSimpleName();
                }
                throw new ExpressionFormatException("Got function that isn't defined(VARIABLE):\n"
                        + call.getFunctionName() + "(" + argumentClass + ")");
            }
            
            return result;
        }

        @Override
        public Result visitVariable(Variable variable) throws ExpressionFormatException {
            Result result;
            
            Long constantValue = constants.get(variable.getName());
            if (constantValue != null) {
                result = new LiteralIntResult(constantValue);
                
            } else {
                NonBooleanVariable var = variables.get(variable.getName());
                if (var != null) {
                    result = new VariableWithValues(variable.getName(), var.getConstants());
                    
                } else {
                    result = new VariableResult(variable.getName(), true);
                }
            }
            
            
            return result;
        }

        @Override
        public Result visitOperator(Operator operator) throws ExpressionFormatException {
            Result leftSide = operator.getLeftSide().accept(this);
            Result rightSide = null;
            if (operator.getOperator().isBinary()) {
                rightSide = operator.getRightSide().accept(this);
            }
            
            Result result = null;
            
            switch (operator.getOperator()) {
            case BOOL_AND:
                result = new BoolAnd(leftSide, rightSide);
                break;
            case BOOL_OR:
                result = new BoolOr(leftSide, rightSide);
                break;
            case BOOL_NOT:
                result = new BoolNot(leftSide);
                break;
            
            case INT_ADD:
                result = leftSide.add(rightSide);
                break;
            case INT_ADD_UNARY:
                // doens't do anything
                result = leftSide;
                break;
            case INT_SUB:
                result = leftSide.sub(rightSide);
                break;
            case INT_SUB_UNARY:
                result = leftSide.subUnary();
                break;
            case INT_MUL:
                result = leftSide.mul(rightSide);
                break;
            case INT_DIV:
                result = leftSide.div(rightSide);
                break;
            case INT_MOD:
                result = leftSide.mod(rightSide);
                break;
//            case INT_INC:
//                break;
//            case INT_DEC:
//                break;
                
            case BIN_AND:
                result = leftSide.binAnd(rightSide);
                break;
            case BIN_OR:
                result = leftSide.binOr(rightSide);
                break;
            case BIN_XOR:
                result = leftSide.binXor(rightSide);
                break;
            case BIN_INV:
                result = leftSide.binInv();
                break;
//            case BIN_SHL:
//                break;
//            case BIN_SHR:
//                break;
                
            case CMP_EQ:
                result = leftSide.cmpEq(rightSide);
                break;
            case CMP_NE:
                result = new BoolNot(leftSide.cmpEq(rightSide));
                break;
            case CMP_LT:
                result = leftSide.cmpLt(rightSide);
                break;
            case CMP_LE:
                result = leftSide.cmpLe(rightSide);
                break;
            case CMP_GT:
                result = rightSide.cmpLt(leftSide);
                break;
            case CMP_GE:
                result = rightSide.cmpLe(leftSide);
                break;
                
            default:
                throw new ExpressionFormatException("Don't know how to handle operator " + operator.getOperator());
            }
            
            return result;
        }
        
        @Override
        public Result visitLiteral(IntegerLiteral literal) {
            return new LiteralIntResult(literal.getValue());
        }
        
    }
    
}
