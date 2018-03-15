package net.ssehub.kernel_haven.non_boolean.replacer;

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.ssehub.kernel_haven.non_boolean.FiniteIntegerVariable;
import net.ssehub.kernel_haven.non_boolean.NonBooleanVariable;
import net.ssehub.kernel_haven.non_boolean.parser.CppParser;
import net.ssehub.kernel_haven.non_boolean.parser.ast.CppExpression;
import net.ssehub.kernel_haven.non_boolean.parser.ast.ExpressionList;
import net.ssehub.kernel_haven.non_boolean.parser.ast.FunctionCall;
import net.ssehub.kernel_haven.non_boolean.parser.ast.ICppExressionVisitor;
import net.ssehub.kernel_haven.non_boolean.parser.ast.IntegerLiteral;
import net.ssehub.kernel_haven.non_boolean.parser.ast.Operator;
import net.ssehub.kernel_haven.non_boolean.parser.ast.Variable;
import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;
import net.ssehub.kernel_haven.variability_model.VariabilityModel;
import net.ssehub.kernel_haven.variability_model.VariabilityVariable;

/**
 * A replacer that turns non-boolean expressions (from the C preprocessor (CPP) or another source) into pure-boolean
 * ones. This requires a set of {@link NonBooleanVariable}s (and optionally a map of constants) to work properly.
 *
 * @author Adam
 */
public class NonBooleanReplacer {
    
    private Map<String, NonBooleanVariable> variables;
    
    private Map<String, Long> constants;
    
    private CppParser parser;
    
    /**
     * Creates a new {@link NonBooleanReplacer}.
     * 
     * @param variables The known {@link NonBooleanVariable}s.
     * @param constants A {@link Map} of constant values to replace in the expressions.
     */
    public NonBooleanReplacer(Map<String, NonBooleanVariable> variables, Map<String, Long> constants) {
        this.variables = variables;
        this.constants = constants;
        this.parser = new CppParser();
    }
    
    /**
     * Creates a new {@link NonBooleanReplacer} with {@link NonBooleanVariable} created from the
     * {@link FiniteIntegerVariable} given variability model.
     * 
     * @param varModel The {@link VariabilityModel} to get the {@link FiniteIntegerVariable}s from.
     * @param constants A {@link Map} of constant values to replace in the expressions.
     */
    public NonBooleanReplacer(VariabilityModel varModel, Map<String, Long> constants) {
        this.variables = new HashMap<>();
        for (VariabilityVariable variable : varModel.getVariables()) {
            if (variable instanceof FiniteIntegerVariable) {
                FiniteIntegerVariable intVar = (FiniteIntegerVariable) variable;
                Set<Long> variableConstants = new HashSet<>();
                for (int i = 0; i < intVar.getSizeOfRange(); i++) {
                    variableConstants.add((long) intVar.getValue(i));
                }
                variables.put(variable.getName(), new NonBooleanVariable(variable.getName(), variableConstants));
            }
        }
        
        this.constants = constants;
        this.parser = new CppParser();
    }

    /**
     * Takes an expression that is not from the CPP and does non-boolean replacements in it. This is basically the same
     * as {@link #replaceCpp(String)} but without defined() functions.
     * 
     * @param expression The expression to replace non-boolean variables in.
     * 
     * @return The expressions with replacements done.
     * 
     * @throws ExpressionFormatException If parsing or evaluating the expression fails.
     */
    public String replaceNonCpp(String expression) throws ExpressionFormatException {
        return replaceImpl(expression, false);
    }
    
    /**
     * Takes a #if or #elif line from the CPP and replaces everything that is non-boolean.
     * 
     * @param cppLine The CPP line to replace.
     * @return The replaced CPP line.
     * 
     * @throws ExpressionFormatException If parsing or evaluating the given CPP line fails.
     */
    public String replaceCpp(String cppLine) throws ExpressionFormatException {
        String expression = null;
        String prepend = null;
        
        if (cppLine.startsWith("#if")) {
            expression = cppLine.substring("#if".length());
            prepend = "#if";
        } else if (cppLine.startsWith("#elif")) {
            expression = cppLine.substring("#elif".length());
            prepend = "#elif";
        }
        if (expression == null) {
            throw new ExpressionFormatException("Line does not start with #if or #elif:\n" + cppLine);
        }
        
        return prepend + replaceImpl(expression, true);
    }
    
    /**
     * Takes an expression and does non-boolean replacements in it.
     * 
     * @param expr The expression to do replacements for.
     * @param cpp Whether this should use CPP defined() calls or not.
     * 
     * @return The expression with replacements done.
     * 
     * @throws ExpressionFormatException If parsing or evaluating the given CPP line fails.
     */
    private String replaceImpl(String expr, boolean cpp) throws ExpressionFormatException {
        CppExpression parsed = parser.parse(expr);
        Result result = parsed.accept(new AstEvaluator(cpp));
        
        return cpp ? result.toCppString() : result.toNonCppString();
    }
    
    /**
     * <p>
     * A visitor that evaluates {@link CppExpression}s based on the given {@link NonBooleanVariable}s and constants.
     * Every integer operation is calculated and resolved. Boolean operators are left as-is (new ones are obviously
     * added).
     * </p>
     * <p>
     * The {@link CppExpression} AST is evaluated bottom-up. Every node is turned into a {@link Result}:
     *  <ul>
     *      <li>Literals and Constants are turned into {@link LiteralIntResult}</li>
     *      <li>{@link NonBooleanVariable}s are turned into {@link VariableWithValues}s</li>
     *      <li>Unknown variables are turned into {@link VariableResult}s with
     *      {@link VariableResult#isUnknownVariable()} set to <code>true</code></li>
     *      <li><code>defined(VAR)</code> calls are turned into {@link VariableResult}s.</li>
     *  </ul>
     * </p>
     * <p>
     * The {@link Operator}s combine these {@link Result} to new {@link Result}:
     *  <ul>
     *      <li>Integer calculation operators on literals create new {@link LiteralIntResult}</li>
     *      <li>Comparisons on {@link VariableResult}s with {@link VariableResult#isUnknownVariable()} create
     *      no-unknown {@link VariableResult}s (in the form of <code>UNKNOWN_eq_2</code> or
     *      <code>UNKNOWN_eq_VAR</code>)</li>
     *      <li>Integer calculation operators on {@link VariableWithValues} modify their current value</li>
     *      <li>Comparisons on {@link VariableWithValues} create boolean formulas that define which original values
     *      of the {@link VariableWithValues} satisfy the condition</li>
     *  </ul>
     * Pretty much every other combination is not allowed an throws an {@link ExpressionFormatException} (e.g. adding
     * a literal to a boolean value).
     * </p>
     * <p>
     * After this we have a tree of {@link Result}s which contains only boolean operations. On this,
     * {@link Result#toCppString()} is called to turn everything back into a CPP string:
     *  <ul>
     *      <li>{@link LiteralIntResult}s are turned into "1" (true) if they are not 0, "0" (false) otherwise</li>
     *      <li>{@link VariableResult}s with not unknown variable content just add a defined() around their variable
     *      name</li>
     *      <li>{@link VariableResult}s with unknown variable content become !defined(UNKNOWN_VAR_NAME_eq_0)</li>
     *      <li>{@link VariableWithValues}s that are left (i.e. there wasn't a comparison for them) turn every current
     *      value that is 0 into a !defined() and AND them together (<code>#if VAR</code> is true for all cases where
     *      VAR is not 0)</li>
     *      <li>{@link BoolResult}s are just written in the obious way</li>
     *  </ul>
     * </p>
     */
    private class AstEvaluator implements ICppExressionVisitor<Result> {

        private boolean cpp;
        
        /**
         * Creates a new {@link AstEvaluator}.
         * 
         * @param cpp Whether this should consider defined() calls or not.
         */
        public AstEvaluator(boolean cpp) {
            this.cpp = cpp;
        }
        
        @Override
        public Result visitExpressionList(ExpressionList expressionList) throws ExpressionFormatException {
            throw new ExpressionFormatException("This code can't be reached.");
        }

        @Override
        public Result visitFunctionCall(FunctionCall call) throws ExpressionFormatException {
            if (!cpp) {
                throw new ExpressionFormatException("Can't handle function " + call.getFunctionName());
            }
            
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
                    result = new VariablesWithValues(variable.getName(), var.getConstants());
                    
                } else {
                    result = new VariableResult(variable.getName(), true);
                }
            }
            
            
            return result;
        }

        // CHECKSTYLE:OFF // TODO: this method is too long
        @Override
        public Result visitOperator(Operator operator) throws ExpressionFormatException {
        //CHECKSTYLE:ON
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
                result = notNull(rightSide).cmpLt(leftSide);
                break;
            case CMP_GE:
                result = notNull(rightSide).cmpLe(leftSide);
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
