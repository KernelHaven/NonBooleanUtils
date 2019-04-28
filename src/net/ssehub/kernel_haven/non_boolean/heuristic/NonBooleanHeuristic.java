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
package net.ssehub.kernel_haven.non_boolean.heuristic;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.non_boolean.NonBooleanSettings;
import net.ssehub.kernel_haven.non_boolean.NonBooleanVariable;
import net.ssehub.kernel_haven.non_boolean.PreprocessorConditionVisitor;
import net.ssehub.kernel_haven.util.Logger;

/**
 * A heuristic to find allowed values for integer variables. This walks through all source files in a given source
 * tree.
 * <p>
 * After creating a {@link NonBooleanHeuristic}, use {@link #addAllSourceFiles(File)} and / or
 * {@link #addSingleCppLine(String)} to add C preprocessor lines to consider. After this, call {@link #getResult()}
 * to compute the final result.
 * 
 * @author Adam
 */
public class NonBooleanHeuristic {

    private static final Logger LOGGER = Logger.get();
    
    private static final String GROUP_NAME_VARIABLE = "variable";
    private static final String GROUP_NAME_OPERATOR = "operator";
    private static final String GROUP_NAME_VALUE = "value";
    private static final String SUPPORTED_OPERATORS_REGEX = "==|!=|<|>|<=|>=";
    private static final String INTEGER_REGEX = "-?[0-9]+[U|u]?[L|l]{0,2}";
    
    /**
     * The non boolean operations that were found in the source files. Maps variable name -> NonBooleanOperation.
     */
    private Map<String, Set<NonBooleanOperation>> nonBooleanOperations;
    
    private Pattern variableNamePattern;
    private Pattern leftSide;
    private Pattern rightSide;
    
    /**
     * A non boolean operation on a variability variable.
     * E.g. <code>>= 3</code>.
     */
    private static final class NonBooleanOperation {
        
        private String operator;
        
        private long value;

        /**
         * Sole constructor.
         * 
         * @param operator One of {@link NonBooleanHeuristic#SUPPORTED_OPERATORS_REGEX}
         * @param value A number
         */
        public NonBooleanOperation(String operator, long value) {
            this.operator = operator;
            this.value = value;
        }
        
        @Override
        public int hashCode() {
            return operator.hashCode() + Long.hashCode(value);
        }
        
        @Override
        public boolean equals(Object obj) {
            boolean equal = false;
            if (obj instanceof NonBooleanOperation) {
                NonBooleanOperation other = (NonBooleanOperation) obj;
                equal = this.operator.equals(other.operator) && this.value == other.value;
            }
            return equal;
        }
        
    }
    
    /**
     * Creates this heuristic with the given {@link Configuration}.
     * 
     * @param config The {@link Configuration} to use.
     * 
     * @throws SetUpException If setting up this heuristic fails.
     */
    public NonBooleanHeuristic(Configuration config) throws SetUpException {
        this.nonBooleanOperations = new HashMap<>();
        
        this.variableNamePattern = config.getValue(NonBooleanSettings.VARIABLE_REGEX);
        String variableRegex = variableNamePattern.pattern();
        
        try {
            
            // <variable> <operator> <value>
            leftSide = Pattern.compile("^"
                + createdNamedCaptureGroup(GROUP_NAME_VARIABLE, variableRegex)
                + "\\s*"
                + createdNamedCaptureGroup(GROUP_NAME_OPERATOR, SUPPORTED_OPERATORS_REGEX)
                + "\\s*"
                + createdNamedCaptureGroup(GROUP_NAME_VALUE, INTEGER_REGEX)
                + ".*");
            
            // <value> <operator> <variable>
            rightSide = Pattern.compile("^"
                + createdNamedCaptureGroup(GROUP_NAME_VALUE, INTEGER_REGEX)
                + "\\s*"
                + createdNamedCaptureGroup(GROUP_NAME_OPERATOR, SUPPORTED_OPERATORS_REGEX)
                + "\\s*"
                + createdNamedCaptureGroup(GROUP_NAME_VARIABLE, variableRegex)
                + ".*");
            
        } catch (PatternSyntaxException e) {
            throw new SetUpException(e);
        }
    }
    
    /**
     * Creates a named capturing group.
     * @param groupName The name of the captured group.
     * @param groupContents The sub RegEx of the group without parenthesis.
     * 
     * @return A named capturing group, with enclosing parenthesis.
     */
    private static String createdNamedCaptureGroup(String groupName, String groupContents) {
        return "(?<" + groupName + ">" + groupContents + ")";
    }
    
    /**
     * Walks through all source files in the given source tree and uses the heuristic to find
     * {@link NonBooleanVariable}s.
     * 
     * @param sourceTree The directory to walk through.
     * 
     * @throws IOException If reading source file fails.
     */
    public void addAllSourceFiles(File sourceTree) throws IOException {
        new PreprocessorConditionVisitor() {
            
            @Override
            public void visit(File file, String line) {
                collectNonBooleanFromLine(file, line);
            }
            
        }.visitAllFiles(sourceTree);
    }
    
    /**
     * Uses the heuristic to find {@link NonBooleanVariable}s in the given C preprocessor line.
     * 
     * @param line The line to find {@link NonBooleanVariable}s in.
     */
    public void addSingleCppLine(String line) {
        collectNonBooleanFromLine(null, line);
    }
    
    /**
     * Preparation phase: Collects variables and required constants.
     * 
     * @param file The file we are currently in (used for error messages).
     * @param line A CPP expression (e.g. if expression).
     */
    private void collectNonBooleanFromLine(File file, String line) {
        Matcher variableNameMatcher = variableNamePattern.matcher(line);
        
        while (variableNameMatcher.find()) {
            int index = variableNameMatcher.start();
            String left = line.substring(index);
            
            // Expression is in form of: <variable> <operator> <constant>
            boolean handled = identifyNonBooleanOperation(file, left, leftSide); 
            if (!handled) {
                handled = identifyNonBooleanOperation(file, left, rightSide); 
            }
        }
    }
    
    /**
     * Identifies {@link NonBooleanOperation}s in preprocessor blocks.
     * 
     * @param file The file we are currently in (used for error messages).
     * @param expression The expression to expression of the preprocessor block to test
     * @param variableValuePattern The expression to identify a {@link NonBooleanOperation}, one of {@link #leftSide}
     *     or {@link #rightSide}
     *     
     * @return <tt>true</tt> if the location matches to the given pattern, <tt>false</tt> otherwise.
     */
    private boolean identifyNonBooleanOperation(File file, String expression, Pattern variableValuePattern) {
        boolean matchFound = false;
        Matcher m = variableValuePattern.matcher(expression);
        if (m.matches()) {
            matchFound = true;
            putNonBooleanOperation(m.group(GROUP_NAME_VARIABLE), m.group(GROUP_NAME_OPERATOR),
                parseConstant(file, m.group(GROUP_NAME_VALUE)));
        }
        return matchFound;
    }
    
    /**
     * Parses a given integer constant string.
     * 
     * @param file The file we are currently in (used for error messages).
     * @param constant The constant to parse.
     * 
     * @return The parsed constant.
     * 
     * @throws NumberFormatException If the constant cannot be parsed.
     */
    private Long parseConstant(File file, String constant) throws NumberFormatException {
        while (!constant.isEmpty() && (constant.toLowerCase().endsWith("l") || constant.toLowerCase().endsWith("u"))) {
            constant = constant.substring(0, constant.length() - 1);
        }
        
        Long result;
        try {
            result = Long.valueOf(constant);
        } catch (NumberFormatException exc) {
            if (null != file) {
                LOGGER.logException("Could not parse \"" + constant + "\" in " + file.getAbsolutePath(), exc);
            } else {
                LOGGER.logException("Could not parse \"" + constant + "\"", exc);
            }
            throw exc;
        }
        
        return result;
    }
    
    /**
     * Puts a found non-boolean operation in the nonBooleanOperations map.
     * 
     * @param variable The variable that the operation was found for.
     * @param operator The operation that was found.
     * @param value The literal value that the operation was done with.
     */
    private void putNonBooleanOperation(String variable, String operator, long value) {
        Set<NonBooleanOperation> l = nonBooleanOperations.get(variable);
        if (l == null) {
            l = new HashSet<>();
            nonBooleanOperations.put(variable, l);
        }
        l.add(new NonBooleanOperation(operator, value));
    }
    
    /**
     * Uses all previously found non-boolean operations to create a set of {@link NonBooleanVariable}s.
     * 
     * @return A map of variableName -&gt; {@link NonBooleanVariable}s.
     */
    public Map<String, NonBooleanVariable> getResult() {
        Map<String, NonBooleanVariable> variables = new HashMap<>();
        
        // No variability model available -> use heuristic (use gathered values from code)
        for (Map.Entry<String, Set<NonBooleanOperation>> entry : nonBooleanOperations.entrySet()) {
            Set<Long> requiredConstants = new HashSet<>();
            
            for (NonBooleanOperation op : entry.getValue()) {
                switch (op.operator) {
                case "==":
                case "!=":
                case ">=":
                case "<=":
                    requiredConstants.add(op.value);
                    break;
                    
                case ">":
                    requiredConstants.add(op.value + 1);
                    break;
                    
                case "<":
                    requiredConstants.add(op.value - 1);
                    break;
                    
                default:
                    LOGGER.logError("Unknown operator: " + op.operator);
                    break;
                }
            }
            
            variables.put(entry.getKey(), new NonBooleanVariable(entry.getKey(), requiredConstants));
        }
        
        return variables;
    }
    
}
