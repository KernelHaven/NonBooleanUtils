/*
 * Copyright 2017-2019 University of Hildesheim, Software Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ssehub.kernel_haven.non_boolean.replacer;

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.ssehub.kernel_haven.non_boolean.FiniteIntegerVariable;
import net.ssehub.kernel_haven.non_boolean.InfiniteIntegerVariable;
import net.ssehub.kernel_haven.non_boolean.NonBooleanVariable;
import net.ssehub.kernel_haven.non_boolean.replacer.VariableResult.Type;
import net.ssehub.kernel_haven.util.cpp.parser.CppParser;
import net.ssehub.kernel_haven.util.cpp.parser.ast.CppExpression;
import net.ssehub.kernel_haven.util.cpp.parser.ast.FunctionCall;
import net.ssehub.kernel_haven.util.cpp.parser.ast.ICppExressionVisitor;
import net.ssehub.kernel_haven.util.cpp.parser.ast.NumberLiteral;
import net.ssehub.kernel_haven.util.cpp.parser.ast.Operator;
import net.ssehub.kernel_haven.util.cpp.parser.ast.Variable;
import net.ssehub.kernel_haven.util.logic.Formula;
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
    
    private Set<String> definedLikeFunctions;
    
    private Set<String> ignoredFunctions;
    
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
        this.definedLikeFunctions = new HashSet<>();
        this.ignoredFunctions = new HashSet<>();
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
                
            } else if (variable instanceof InfiniteIntegerVariable) {
                variables.put(variable.getName(), new NonBooleanVariable(variable.getName(), new HashSet<>(), true));
            }
        }
        
        this.constants = constants;
        this.parser = new CppParser();
        this.definedLikeFunctions = new HashSet<>();
        this.ignoredFunctions = new HashSet<>();
    }
    
    /**
     * Overrides the map of constants set in the constructor.
     * 
     * @param constants The new map of constants to use.
     */
    public void setConstants(Map<String, Long> constants) {
        this.constants = constants;
    }
    
    /**
     * Sets which functions should be handled like <code>defined</code> in the C preprocessor. This is basically a set
     * of aliases for <code>defined</code>. <code>defined</code> itself is automatically added, if
     * {@link #replaceCpp(String)} is used, so it doesn't need to be explicitly added here.
     * 
     * @param definedLikeFunctions The set of <code>defined</code> like function names.
     */
    public void setDefinedLikeFunctions(Set<String> definedLikeFunctions) {
        this.definedLikeFunctions = definedLikeFunctions;
    }
    
    /**
     * Sets which functions should be ignored. If such a function is found in the CPP line, then its only argument
     * is used as the "return value" for this function without any modification.
     * 
     * @param ignoredFunctions The set of function names to "ignore".
     */
    public void setIgnoredFunctions(Set<String> ignoredFunctions) {
        this.ignoredFunctions = ignoredFunctions;
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
        
        if (cppLine.startsWith("#if ") || cppLine.startsWith("#if(")) {
            expression = cppLine.substring("#if".length());
            prepend = "#if ";
        } else if (cppLine.startsWith("#elif ") || cppLine.startsWith("#elif(")) {
            expression = cppLine.substring("#elif".length());
            prepend = "#elif ";
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
     * @throws ExpressionFormatException If parsing or evaluating the given expression fails.
     */
    private String replaceImpl(String expr, boolean cpp) throws ExpressionFormatException {
        CppExpression parsed = parser.parse(expr);
        
        boolean removeDefined = false;
        Result result;
        try {
            // add defined to the definedLikeFunctions, if we should parse like the CPP
            if (cpp && !definedLikeFunctions.contains("defined")) {
                definedLikeFunctions.add("defined");
                removeDefined = true;
            }
            result = parsed.accept(new AstEvaluator());
            
        } finally {
            // remove defined again, if we only temporarily added it for this call
            if (removeDefined) {
                definedLikeFunctions.remove("defined");
            }
        }
        
        
        
        return cpp ? result.toCppString() : result.toNonCppString();
    }
    
    /**
     * Does non-boolean replacements in the non-CPP expression and returns a boolean {@link Formula} of the result.
     * 
     * @param expression The expression to do non-boolean replacements in.
     * 
     * @return A boolean {@link Formula} of the expression with non-boolean replacements.
     * 
     * @throws ExpressionFormatException If parsing or evaluating the given expression fails.
     */
    public Formula nonCppToFormula(String expression) throws ExpressionFormatException {
        CppExpression parsed = parser.parse(expression);
        Result result = parsed.accept(new AstEvaluator());
        
        return result.toFormula();
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

        @Override
        public Result visitFunctionCall(FunctionCall call) throws ExpressionFormatException {
            
            Result result;
            if (definedLikeFunctions.contains(call.getFunctionName())) {
                CppExpression argument = call.getArgument();
                while (argument instanceof FunctionCall
                        && ignoredFunctions.contains(((FunctionCall) argument).getFunctionName())) {
                    argument = ((FunctionCall) argument).getArgument();
                }
                
                if (argument instanceof Variable) {
                    result = new VariableResult(((Variable) argument).getName(), Type.FINAL);
                    
                } else {
                    String argumentClass = "null";
                    if (argument != null) {
                        argumentClass = argument.getClass().getSimpleName();
                    }
                    throw new ExpressionFormatException("Got function that isn't defined(VARIABLE):\n"
                            + call.getFunctionName() + "(" + argumentClass + ")");
                }
                
            } else if (ignoredFunctions.contains(call.getFunctionName())) {
                result = call.getArgument().accept(this);
                
            } else {
                throw new ExpressionFormatException("Can't handle function " + call.getFunctionName());
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
                    if (var.isInfinite()) {
                        result = new VariableResult(variable.getName(), Type.INFINITE);
                    } else {
                        result = new VariablesWithValues(variable.getName(), var.getConstants());
                    }
                    
                } else {
                    result = new VariableResult(variable.getName(), Type.UNKNOWN);
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
                // CHECKSTYLE:OFF // TODO: too "complex" condition
                if ((leftSide instanceof VariableResult && ((VariableResult) leftSide).getType() == Type.INFINITE
                        && rightSide instanceof LiteralIntResult)
                        || (rightSide instanceof VariableResult && ((VariableResult) rightSide).getType()
                                == Type.INFINITE && leftSide instanceof LiteralIntResult)) {
                // CHECKSTYLE:ON
                    
                    // don't add a NOT if we compare a literal with an INFINITE variable
                    // TODO: this is quite hacky...
                    result = leftSide.cmpEq(rightSide);
                } else {
                    result = new BoolNot(leftSide.cmpEq(rightSide));
                }
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
        public Result visitLiteral(NumberLiteral literal) {
            Result result;
            if (literal.getValue() instanceof Long) {
                result = new LiteralIntResult(literal.getValue().longValue());
            } else {
                result = new VariableResult(literal.getValue().toString().replace('.', '_'), Type.UNKNOWN);
            }
            return result;
        }
        
    }
    
}
