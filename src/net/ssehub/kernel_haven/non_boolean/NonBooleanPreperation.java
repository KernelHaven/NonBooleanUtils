package net.ssehub.kernel_haven.non_boolean;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.ssehub.kernel_haven.IPreparation;
import net.ssehub.kernel_haven.PipelineConfigurator;
import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.config.DefaultSettings;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Util;
import net.ssehub.kernel_haven.variability_model.VariabilityModel;
import net.ssehub.kernel_haven.variability_model.VariabilityVariable;

//TODO: tidy up this temporary hack
//CHECKSTYLE:OFF

/**
 * @author Adam
 * @author El-Sharkawy
 */
// TODO SE: @Adam please check whether and how NonBooleanConditionConverter can be used/integrated
public class NonBooleanPreperation implements IPreparation {
    
    static final String VAR_IDENTIFICATION_REGEX_CONFIG = "code.extractor.variable_regex";
    
    private static final String GROUP_NAME_VARIABLE = "variable";
    private static final String GROUP_NAME_OPERATOR = "operator";
    private static final String GROUP_NAME_VALUE = "value";
    private static final String SUPPORTED_OPERATORS_REGEX = "==|!=|<|>|<=|>=";
    private static final String INTEGER_REGEX = "-?[0-9]+[U|u]?[L|l]{0,2}";
    private static final String SEPARATOR_REGEX = "[(|)|\\s]{1}+";
    private static final boolean REMOVE_CONSISTENCY_CHECKS = true;
    
    private static final Pattern UNECESSARY_BRACKET_PATTERN = Pattern.compile("[^\\w](\\((\\w[\\s\\w]*)\\))");
    
    private static final Logger LOGGER = Logger.get();
    
    private File originalSourceTree;
    
    private File copiedSourceTree;
    
    /**
     * The non boolean operations that were found in the source files. Maps variable name -> NonBooleanOperation.
     */
    private Map<String, Set<NonBooleanOperation>> nonBooleanOperations;
    
    /**
     * The non boolean variables. These were either gathered from the heuristic, or the VariabilityModel (if it contains
     * FiniteIntegerVariables). Maps variable name -> NonBooleanVariable.
     */
    private Map<String, NonBooleanVariable> variables;
    
    private String variableRegex ;
    private Pattern variableNamePattern;
    private Pattern leftSide;
    private Pattern rightSide;
    
    private Pattern leftSideFinder;
    private Pattern rightSideFinder;
    private Pattern twoVariablesExpression;
    
    /**
     * Defines whether we got the NonBooleanVariables from the VariabilityModel or the heuristic.
     * <code>true</code> means we read from the variability model.
     * <code>false</code> means we used the heuristic.
     */
    private boolean nonBooleanModelRead = false;
    
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

    @Override
    public void run(Configuration config) throws SetUpException {
        NonBooleanSettings.registerAllSettings(config);
        
        copiedSourceTree = config.getValue(NonBooleanSettings.DESTINATION_DIR);
        originalSourceTree = config.getValue(DefaultSettings.SOURCE_TREE);
        
        try {
            if (Util.isNestedInDirectory(originalSourceTree, copiedSourceTree)) {
                throw new SetUpException(NonBooleanSettings.DESTINATION_DIR.getKey() + " points to a location inside "
                        + "of " + DefaultSettings.SOURCE_TREE);
            }
        } catch (IOException e1) {
            throw new SetUpException(e1);
        }
        
        variableNamePattern = config.getValue(NonBooleanSettings.VARIABLE_REGEX);
        variableRegex = variableNamePattern.pattern();
        
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
            
            leftSideFinder = Pattern.compile(
                    SEPARATOR_REGEX
                + createdNamedCaptureGroup(GROUP_NAME_VARIABLE, variableRegex)
                + "\\s*"
                + createdNamedCaptureGroup(GROUP_NAME_OPERATOR, SUPPORTED_OPERATORS_REGEX)
                + "\\s*"
                + createdNamedCaptureGroup(GROUP_NAME_VALUE, INTEGER_REGEX));
            
            rightSideFinder = Pattern.compile(
                SEPARATOR_REGEX
                + createdNamedCaptureGroup(GROUP_NAME_VALUE, INTEGER_REGEX)
                + "\\s*"
                + createdNamedCaptureGroup(GROUP_NAME_OPERATOR, SUPPORTED_OPERATORS_REGEX)
                + "\\s*"
                + createdNamedCaptureGroup(GROUP_NAME_VARIABLE, variableRegex));
            
            twoVariablesExpression = Pattern.compile(
                createdNamedCaptureGroup(GROUP_NAME_VARIABLE, variableRegex)
                + "\\s*"
                + createdNamedCaptureGroup(GROUP_NAME_OPERATOR, SUPPORTED_OPERATORS_REGEX)
                + "\\s*"
                + createdNamedCaptureGroup(GROUP_NAME_VALUE, variableRegex));
        } catch (PatternSyntaxException e) {
            throw new SetUpException(e);
        }
        
        try {
            prepare();
        } catch (IOException e) {
            throw new SetUpException(e);
        }
        
        config.setValue(DefaultSettings.SOURCE_TREE, copiedSourceTree);
        PipelineConfigurator.instance().getCmProvider().setConfig(config);
    }

    private static final class NonBooleanVariable {
        
        private String name;
        
        private long[] constants;
        
        public NonBooleanVariable(String name, Set<Long> constants) {
            this.name = name;
            this.constants = new long[constants.size()];
            int i = 0;
            for (Long c : constants) {
                this.constants[i++] = c;
            }
        }
        
        public long[] getConstants() {
            return constants;
        }
        
        public String getConstantName(long constant) {
            return name + "_eq_" + constant;
        }
        
        @Override
        public String toString() {
            return name + Arrays.toString(constants);
        }
        
    }
    
    /**
     * A non boolean operation on a variability variable.
     * E.g. <code>>= 3</code>.
     */
    private static final class NonBooleanOperation {
        
        private String operator;
        
        private long value;

        /**
         * Sole constructor
         * @param operator One of {@link NonBooleanPreperation#SUPPORTED_OPERATORS_REGEX}
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
     * Prepare the source tree.
     * 
     * @throws IOException IF reading files fails.
     */
    private synchronized void prepare() throws IOException {
        LOGGER.logDebug("Starting preperation...");
        
        // make sure that the destination is empty
        if (copiedSourceTree.exists()) {
            Util.deleteFolder(copiedSourceTree);
        }
        copiedSourceTree.mkdir();
        
        nonBooleanOperations = new HashMap<>();
        
        // walk through all *.c and *.h files in the source_tree, and collect non boolean operations.
        // this fills this.nonBooleanOperations and this.burntVariables
        new PreprocessorConditionVisitor() {
            
            @Override
            public void visit(String line) {
                try {
                    collectNonBooleanFromLine(line);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.visitAllFiles(originalSourceTree);
        
        
        variables = new HashMap<>();
        
        // convert the nonBooleanOperations we found earlier into NonBooleanVariables (this is our heuristic)
        // if the variability model contains FiniteIntegerVariables, use them instead of our heuristic
        // this fills this.variables
        gatherConstantValues();
        
        // copy the source_tree to destination, while replacing the relational expressions with NonBoolean variables
        LOGGER.logDebug("Copying from " + originalSourceTree.getAbsolutePath() + " to " + copiedSourceTree.getAbsolutePath());
        copy(originalSourceTree, copiedSourceTree);
    }

    private void gatherConstantValues() {
        VariabilityModel varModel = PipelineConfigurator.instance().getVmProvider().getResult();
        nonBooleanModelRead = false;
        
        // Try to use information of variability model -> exact approach
        if (null != varModel) {
            for (VariabilityVariable variable : varModel.getVariables()) {
                Set<Long> requiredConstants = new HashSet<>();
                if (null != variable && variable instanceof FiniteIntegerVariable) {
                    nonBooleanModelRead = true;
                    FiniteIntegerVariable intVar = (FiniteIntegerVariable) variable;
                    for (int i = 0; i < intVar.getSizeOfRange(); i++) {
                        requiredConstants.add((long) intVar.getValue(i));
                    }
                    variables.put(variable.getName(), new NonBooleanVariable(variable.getName(), requiredConstants));
                }
            }
        }
        
        if (!nonBooleanModelRead) {
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
                        System.err.println("Unknown operator: " + op.operator);
                        break;
                    }
                }
                
                variables.put(entry.getKey(), new NonBooleanVariable(entry.getKey(), requiredConstants));
            }
        }
    }
    
    private NonBooleanVariable getVariableForced(String name) {
        NonBooleanVariable variable = variables.get(name);
        if (null == variable) {
            variable = new NonBooleanVariable(name, new HashSet<Long>());
            variables.put(name, variable);
        }
        
        return variable;
    }
    
    private NonBooleanVariable getVariableForcedIfHeuristic(String name) {
        if (nonBooleanModelRead) {
            return variables.get(name);
        } else {
            return getVariableForced(name);
        }
    }
    
    private void copy(File from, File to) throws IOException {
        for (File f : from.listFiles()) {
            
            File newF = new File(to, f.getName());
            
            if (f.isDirectory()) {
                newF.mkdir();
                copy(f, newF);
            } else {
                if (f.getName().endsWith(".c") || f.getName().endsWith(".h")) {
                    copySourceFile(f, newF);
                } else {
                    Util.copyFile(f, newF);
                }
            }
        }
    }
    
    private void copySourceFile(File from, File to) throws IOException {
        try (BufferedReader in = new BufferedReader(new FileReader(from))) {
            
            try (Writer out = createWriter(to)) {
                
                String line;
                while (continueReading(line = in.readLine())) {
                    // Replace variable occurrences of #if's and #elif's
                    if (CPPUtils.isIfOrElifStatement(line)) {
                        
                        // Consider continuation
                        while (line.charAt(line.length() - 1) == '\\') {
                            String tmp = in.readLine();
                            if (null != tmp) {
                                line += tmp;
                            } else {
                                break;
                            }
                        }
                        
                        line = line.trim();
                        line = replaceInLine(line, from);
                    }
                    
                    out.write(line);
                    if (out instanceof BufferedWriter) {
                        out.write("\n");
                    }
                }
                
            }
        }
    }
    
    /**
     * Creates the writer to use (either {@link BufferedWriter} or {@link CppBufferedWriter}.
     * @param to The destination where to save the copied file.
     * @return The writer to use.
     * @throws IOException If the file exists but is a directory rather than a regular file, does not exist
     *     but cannot be created, or cannot be opened for any other reason
     */
    private Writer createWriter(File to) throws IOException {
        Writer writer;
        BufferedWriter out = new BufferedWriter(new FileWriter(to));
        if (REMOVE_CONSISTENCY_CHECKS) {
            writer = new CppBufferedWriter(out);
        } else {
            writer = out;
        }
        
        return writer;
    }
    
    /**
     * Specifies when to abort copying a code file (line-based copying).
     * @param line The line which will be copied
     * @return <tt>true</tt> if this line should still be copied, <tt>false</tt> if this line and all subsequent lines
     *     shall be discarded (default when the end of the file is reached, i.e., <tt>line !=null</tt>).
     */
    protected boolean continueReading(String line) {
        return line != null;
    }
    
    /**
     * Removes unnecessary brackets from the given formula.
     * 
     * @param condition The unparsed condition from which the unnecessary brackets shall be detected and removed from.
     * @return An equivalent condition with removed unnecessary brackets, maybe the same instance if the condition does
     *     not contain any unnecessary brackets.
     */
    private static String removeUnnecessaryBrackets(String condition) {
        Matcher matcher = UNECESSARY_BRACKET_PATTERN.matcher(condition);
        while (matcher.find()) {
            condition = condition.replace(matcher.group(1), matcher.group(2));
            matcher = UNECESSARY_BRACKET_PATTERN.matcher(condition);
        }
        return condition;
    }
    
    private String replaceInLine(String line, File from) {
        String result = removeUnnecessaryBrackets(line);
        Matcher m;
        
        // replace known constants in result
        Map<String, Long> constants = getConstants();
        if (!constants.isEmpty()) {
            m = variableNamePattern.matcher(result);
            while (m.find()) {
                String constCandidate = m.group();
                if (constants.containsKey(constCandidate)) {
                    String before = result;
                    result = result.substring(0, m.start()) + constants.get(constCandidate) + result.substring(m.end());

                    LOGGER.logDebug("Replacing constant " + constCandidate + " in " + before, " -> " + result);
                    // This is necessary if there is more than one match in order to get correct indexes
                    m = variableNamePattern.matcher(result);
                }
            }
        }
        
        result = convertRelationalExpressionOnVarAndValue(result, leftSideFinder, true);
        result = convertRelationalExpressionOnVarAndValue(result, rightSideFinder, false);

        // Convert bit operators
        Pattern p = Pattern.compile("\\(\\s*" 
            + createdNamedCaptureGroup(GROUP_NAME_VARIABLE, variableRegex)
            + "\\s*"
            + createdNamedCaptureGroup("bitOperator", Pattern.quote("%") + "|" + Pattern.quote("&") + "|" + Pattern.quote("^") + "|" + Pattern.quote("|"))
            + "\\s*"
            + createdNamedCaptureGroup("bitValue", INTEGER_REGEX)
            + "\\s*\\)\\s*"
            + createdNamedCaptureGroup(GROUP_NAME_OPERATOR, SUPPORTED_OPERATORS_REGEX)
            + "\\s*"
            + createdNamedCaptureGroup(GROUP_NAME_VALUE, INTEGER_REGEX)
            // Expect separator or end of line after detected expression
            + "((|)|\\s|$){1}+");
        m = p.matcher(result);
        
        while (m.find()) {
            String whole = m.group();
            String variable = m.group(GROUP_NAME_VARIABLE);
            String bitOpTmp = null;
            
            NonBooleanVariable var = getVariableForcedIfHeuristic(variable);
            if (var == null) {
                continue;
            }
            try {
                bitOpTmp = m.group("bitOperator");
            } catch (IllegalArgumentException exc) {
                LOGGER.logException("Could not determine Bit operator in expression :" + whole, exc);
            }
            final String bitOp = bitOpTmp;
            
            String bitValue = null;
            try {
                bitValue = m.group("bitValue");
            } catch (IllegalArgumentException exc) {
                LOGGER.logException("Could not determine Bit value in expression :" + whole, exc);
            }
            
            String op = m.group(GROUP_NAME_OPERATOR);
            String value = m.group(GROUP_NAME_VALUE);
            
            if (null != bitOp && null != bitValue) {
                Long tmpBit = null;
                try {
                    tmpBit = parseConstant(bitValue);
                } catch (NumberFormatException exc) {
                    LOGGER.logException("Could not parse Bit in expression: " + whole, exc);
                }
                final Long bit = tmpBit;
                Long tmpConstantValue = null;
                try {
                    tmpConstantValue = parseConstant(value);
                } catch (NumberFormatException exc) {
                    LOGGER.logException("Could not parse constant in expression: " + whole, exc);
                }
                final Long constantValue = tmpConstantValue;
                
                if (var.constants.length > 0 && null != bit && null != constantValue) {
                    List<Long> matchesList = new ArrayList<>(var.getConstants().length);
                    
                    Arrays.stream(var.constants)
                        .filter(l -> bitComparion(bitOperation(l, bit, bitOp, whole), constantValue, op, whole))
                        .forEach(matchesList::add);
                    
                    if (!matchesList.isEmpty()) {
                        String replacement = expandComparison(var, matchesList);
                        if (!whole.equals(replacement)) {
                            result = result.replace(whole, replacement);
                            m = twoVariablesExpression.matcher(result);
                        }
                    } else {
                        LOGGER.logWarning("Bit expression does not allow any legal values: " + whole);
                    }
                }
            }
        }
        
        
        // Check if it is a comparison between two variables and try it again
        m = twoVariablesExpression.matcher(result);
        while (m.find()) {
            String whole = m.group();
            String firstVar = m.group(GROUP_NAME_VARIABLE);
            String op = m.group(GROUP_NAME_OPERATOR);
            String secondVar = m.group(GROUP_NAME_VALUE);
            
            NonBooleanVariable var1 = getVariableForcedIfHeuristic(firstVar);
            NonBooleanVariable var2 = getVariableForcedIfHeuristic(secondVar);
            
            if (var1 == null || var2 == null) {
                continue;
            }
            
            String replacement = whole;
            
            if (var1.constants.length > 0 || var2.constants.length > 0) {
                switch (op) {
                case "==":
                case "<":
                case ">":
                case "<=":
                case ">=":
                case "!=":
                    String expaned = expandComparison(var1, op, var2);
                    if (null != expaned) {
                        replacement = expaned;
                    }
                    
                    LOGGER.logDebug("Exchanged", whole, "to", replacement);
                    break;
                    
                default :
                    LOGGER.logWarning("Could not prepare non boolean expression because of unsuppoted type: " + whole);
                    break;
                }
            }
            if (!whole.equals(replacement)) {
                result = result.replace(whole, replacement);
                m = twoVariablesExpression.matcher(result);
            }
        }
        
        // Replace comparison on two constant numbers
        String terminal = "(#if|#elif|$|\\(|\\)|\\|\\||&&)";
        p = Pattern.compile(SEPARATOR_REGEX
            + createdNamedCaptureGroup("constantcomparison",
                createdNamedCaptureGroup(GROUP_NAME_VARIABLE, INTEGER_REGEX)
                + "\\s*"
                + createdNamedCaptureGroup(GROUP_NAME_OPERATOR, SUPPORTED_OPERATORS_REGEX)
                + "\\s*"
                + createdNamedCaptureGroup(GROUP_NAME_VALUE, INTEGER_REGEX))
            // Expect separator or end of line after detected expression
            + "((|)|\\s|$){1}+");
        result = convertRelationalExpressionsOnNumbers(result, p);
        
        // Replace #if (VAR)
        result = convertBooleanVariableExpressions(from, result,
            Pattern.compile(terminal + "\\s*" + "(" + variableNamePattern + ")" + "\\s*" + terminal), true);
        // Replace #if (!VAR)
        result = convertBooleanVariableExpressions(from, result,
            Pattern.compile(terminal + "\\s*" + "(!" + variableNamePattern + ")" + "\\s*" + terminal), false);
        
        return result;
    }
    
    private Long parseConstant(String constant) {
        while (!constant.isEmpty() && (constant.endsWith("L") || constant.endsWith("l")
            || constant.endsWith("u") || constant.endsWith("U"))) {
            
            constant = constant.substring(0, constant.length() - 1);
        }
        
        return Long.valueOf(constant);
    }
    
    private Long bitOperation(long number, Long bit, String bitOp, String whole) {
        Long result;
        
        if (null != bit) {
            switch (bitOp) {
            case "&":
                result = number & bit;
                break;
            case "|":
                result = number | bit;
                break;
            case "^":
                result = number ^ bit;
                break;
            case "%":
                result = number % bit;
                break;
            default:
                result = null;
                LOGGER.logError("Could not parse Bit expression, due to unexpected Bit operator: " + whole);
                break;
            }
        } else {
            result = null;
            LOGGER.logError("Could not parse Bit expression, due unparsed Bit value: " + whole);
        }
        
        return result;
    }
    
    private boolean bitComparion(Long resultOfBitOperation, long constantValue, String op, String whole) {
        boolean matches = false;
        
        if (null != resultOfBitOperation) {
            switch (op) {
            case "==":
                matches = resultOfBitOperation == constantValue;
                break;
            case "<":
                matches = resultOfBitOperation < constantValue;
                break;
            case ">":
                matches = resultOfBitOperation > constantValue;
                break;
            case "<=":
                matches = resultOfBitOperation <= constantValue;
                break;
            case ">=":
                matches = resultOfBitOperation >= constantValue;
                break;
            case "!=":
                matches = resultOfBitOperation != constantValue;
                break;
            default:
                LOGGER.logError("Could not parse Bit expression, due to unexpected relational operator: " + whole);
                break;
            }
        }
        
        return matches;
    }

    /**
     * Replaces expressions in the sense of <tt>if &lt;number&gt; &lt;operator&gt; &lt;number&gt;</tt>.
     * @param result The currently processed line, which is converted.
     * @param p The regular expression, to detect such expressions.
     * @return The converted line, maybe the same line as passed as input if nothing could be changed.
     */
    private String convertRelationalExpressionsOnNumbers(String result, Pattern p) {
        Matcher m = p.matcher(result);
        while (m.find()) {
            String whole = m.group("constantcomparison");
            String firstValue = m.group(GROUP_NAME_VARIABLE);
            String op = m.group(GROUP_NAME_OPERATOR);
            String secondValue = m.group(GROUP_NAME_VALUE);
            
            try {
                long value1 = parseConstant(firstValue);
                long value2 = parseConstant(secondValue);
                
                switch (op) {
                case "==":
                    if (value1 == value2) {
                        result = result.replace(whole, "1");
                    } else {
                        result = result.replace(whole, "0");
                    }
                    m = p.matcher(result);
                    break;
                case "<":
                    if (value1 < value2) {
                        result = result.replace(whole, "1");
                    } else {
                        result = result.replace(whole, "0");
                    }
                    m = p.matcher(result);
                    break;
                case ">":
                    if (value1 > value2) {
                        result = result.replace(whole, "1");
                    } else {
                        result = result.replace(whole, "0");
                    }
                    m = p.matcher(result);
                    break;
                case "<=":
                    if (value1 <= value2) {
                        result = result.replace(whole, "1");
                    } else {
                        result = result.replace(whole, "0");
                    }
                    m = p.matcher(result);
                    break;
                case ">=":
                    if (value1 >= value2) {
                        result = result.replace(whole, "1");
                    } else {
                        result = result.replace(whole, "0");
                    }
                    m = p.matcher(result);
                    break;
                case "!=":
                    if (value1 != value2) {
                        result = result.replace(whole, "1");
                    } else {
                        result = result.replace(whole, "0");
                    }
                    m = p.matcher(result);
                    break;

                default:
                    LOGGER.logWarning("Could not simplify constant expression, since an unexpected operator was used: "
                        + whole);
                    break;
                }
                
                m = p.matcher(result);
            } catch (NumberFormatException exc) {
                LOGGER.logInfo("Could not simplify constant expression: " + whole);
            }
        }
        
        return result;
    }

    /**
     * Replaces expressions in the sense of if (BOOLEAN_VAR) without any operators.
     * @param from The source file which is currently converted (used to produce sufficient error logs).
     * @param result The currently processed line, which is converted.
     * @param p The regular expression, to detect such boolean expressions.
     * @param negate <tt>true</tt> for the detection of if(VAR) (must be reflected by the pattern,
     *     <tt>false</tt> for the detection of if(<b>!</b>VAR)
     * @return The converted line, maybe the same line as passed as input if nothing could be changed.
     */
    private String convertBooleanVariableExpressions(File from, String result, Pattern p, boolean negate) {
        Matcher m = p.matcher(result);
        
        while (m.find()) {
            String varCandidate = m.group(2);
            
            // Check that we did not find a key word
            boolean handle = !"defined".equals(varCandidate) && !"!defined".equals(varCandidate);
            // Check that we did not find an already replaced variable
            if (handle) {
                handle = !varCandidate.contains("_eq_");
            }
            // Check that we did not detect a whole define statement
            if (handle) {
                // Consider arbitrary white spaces before and after brackets
                int startIndex = Math.max(0, m.start() - 9);
                int endIndex = Math.min(result.length(), m.end() + 2);
                String region = result.substring(startIndex, endIndex).replace(" ", "");
                handle = !region.contains("defined(" + varCandidate + ")");
            }
            
            // Avoid double replacement...
            if (handle) {
                
                NonBooleanVariable var1 = negate ? getVariableForced(varCandidate)
                    : getVariableForced(varCandidate.substring(1));
                
                if (var1.constants.length > 0) {
                    String replacement = (negate ? "!defined(" : "defined(") + var1.getConstantName(0) + ")";
                    if (!varCandidate.equals(replacement)) {
                        result = replace(result, varCandidate, replacement, m.start());
                        m = p.matcher(result);
                    }
                } else {
                    LOGGER.logWarning("Found variable without a relational expression, which is also not known by the "
                        + "variability model. Don't know how to handle " + result + " in file " + from.getAbsolutePath());
                }
            }
        }
        return result;
    }

    /**
     * Converts all occurrences of specified pattern with a variable equals constant expressions.
     * @param cppLine One preprocessor line, which shall be checked and potentially rewritten.
     * @param relationalExpressionPattern A pattern to identify variable, operator, and constant.
     *     One of {@link #leftSideFinder} or {@link #rightSideFinder}
     * @param variableOnLeftSide Depending on the pattern, variable is expected on left side of operation
     *     (<tt>true</tt>) or on right side (<tt>false</tt>).
     * 
     * @return The rewritten results, maybe the original input (if the pattern was not found).
     */
    private String convertRelationalExpressionOnVarAndValue(String cppLine, Pattern relationalExpressionPattern,
        boolean variableOnLeftSide) {
        
        Matcher m;
        m = relationalExpressionPattern.matcher(cppLine);
        while (m.find()) {
            String whole = m.group();
            String name = m.group(GROUP_NAME_VARIABLE);
            String op = m.group(GROUP_NAME_OPERATOR);
            long value = parseConstant(m.group(GROUP_NAME_VALUE));
            
            String replacement = "ERROR_WHILE_REPLACING";
            NonBooleanVariable var = variables.get(name);
            
            if (var != null) {
                switch (op) {
                case "==":
                    replacement = "defined(" + var.getConstantName(value) + ")";
                    break;
                    
                case "!=":
                    replacement = "!defined(" + var.getConstantName(value) + ")";
                    break;
                    
                case ">":
                    if (variableOnLeftSide) {
                        value++;
                    } else {
                        value--;
                    }
                    // fall through
                case ">=":
                    List<Long> greaterValuesToAdd = new ArrayList<>(var.getConstants().length);
                    
                    for (long c : var.getConstants()) {
                        if (variableOnLeftSide) {
                            if (c >= value) {
                                greaterValuesToAdd.add(c);
                            }
                        } else {
                            if (c <= value) {
                                greaterValuesToAdd.add(c);
                            }
                        }
                    }
                    
                    replacement = expandComparison(var, greaterValuesToAdd);
                    
                    break;
                    
                case "<":
                    if (variableOnLeftSide) {
                        value--;
                    } else {
                        value++; 
                    }
                    // fall through
                case "<=":
                    List<Long> lesserValuesToAdd = new ArrayList<>(var.getConstants().length);
                    
                    for (long c : var.getConstants()) {
                        if (variableOnLeftSide) {
                            if (c <= value) {
                                lesserValuesToAdd.add(c);
                            }
                        } else {
                            if (c >= value) {
                                lesserValuesToAdd.add(c);
                            }
                        }
                    }
                    
                    replacement = expandComparison(var, lesserValuesToAdd);
                    
                    break;
                }
                
                /* 
                 * Only replace if we had no error, i.e., if we could resolve all parts
                 * Took replacing function from https://stackoverflow.com/a/16229133, which should be faster than JDK
                 * version. Also avoid multiple replacements, since they may lead to unexpected side effects
                 */
                // Skip first character which is an separator
                whole = whole.substring(1);
                cppLine = replace(cppLine, whole, replacement);
                
                // Find new match after string has been changed!
                m = relationalExpressionPattern.matcher(cppLine);
            }
        }
        return cppLine;
    }

    /**
     * Replaces only a single (<b>first</b>) occurrence of a substring in the whole String.
     * Based on <a href="https://stackoverflow.com/a/16229133">Stackoverflow / Apache IO</a> and uses a StringBuffer
     * to be faster.
     * @param whole The complete String in which a part shall be replaced
     * @param old The old substring to be replaced
     * @param replacement The replacement for the old substring.
     * @return The complete String, where the old part was replaced if it could be found.
     */
    private String replace(String whole, String old, String replacement) {
        return replace(whole, old, replacement, 0);
    }
    
    /**
     * Replaces only a single occurrence of a substring in the whole String.
     * Based on <a href="https://stackoverflow.com/a/16229133">Stackoverflow / Apache IO</a> and uses a StringBuffer
     * to be faster.
     * @param whole The complete String in which a part shall be replaced
     * @param old The old substring to be replaced
     * @param replacement The replacement for the old substring.
     * @param startIndex The 0-based index at which the element may be found for the first time (earlier matches will be
     *     ignored).
     * @return The complete String, where the old part was replaced if it could be found.
     */
    private String replace(String whole, String old, String replacement, int startIndex) {
        int start = 0;
        int end = whole.indexOf(old, startIndex);
        int replacementLength = old.length();
        int increase = replacement.length() - replacementLength;
        
        increase = (increase < 0 ? 0 : increase);
        StringBuffer buf = new StringBuffer(whole.length() + increase);
        buf.append(whole.substring(start, end));
        buf.append(replacement);
        start = end + replacementLength;
        buf.append(whole.substring(start));
        whole = buf.toString();
        
        return whole;
    }

    /**
     * Creates a disjunction constraints containing comparisons for all values passed to this method.
     * @param var A variable for which multiple comparisons shall be created for.
     * @param legalValues The values which shall be added to the comparison.
     * @return One Boolean disjunction expression.
     */
    private String expandComparison(NonBooleanVariable var, List<Long> legalValues) {
        String replacement;
        if (!legalValues.isEmpty()) {
            replacement = "(defined(" + var.getConstantName(legalValues.get(0)) + ")";
            for (int i = 1; i < legalValues.size(); i++) {
                replacement += " || defined(" + var.getConstantName(legalValues.get(i)) + ")";
            }
            replacement += ")";
        } else {
            replacement = "0";
            // I think an exception would be more appropriate
            LOGGER.logWarning("Could not replace values for variable: " + var.name);
        }
        return replacement;
    }
    
    private static class Pair {
        private long value1;
        private long value2;
        
        public Pair(long value1, long value2) {
            this.value1 = value1;
            this.value2 = value2;
        }
    }
    
    private String expandComparison(NonBooleanVariable var1, String op, NonBooleanVariable var2) {
        StringBuffer replacement = new StringBuffer();
        List<Pair> pairs = new LinkedList<>();
        
        long[] var1Values = var1.constants;
        long[] var2Values = var2.constants;
        
        if (!nonBooleanModelRead) {
            /*
             * We used a heuristic to get var1 and var2 constants; there may be some missing, so we use the joined
             * set of constants for both variables.
             * 
             * Example: a has constants {0, 1}, and b has constants {1, 2}; then we (temporarily) handle a and b as if
             * they both have the constants {0, 1, 2}
             * 
             * TODO: this is probably still not enough, since we  only "expand" the values for this one comparison;
             *       what should actually happen is that the newly added constants are also considered in all other
             *       comparisons
             */
            Set<Long> allValues = new TreeSet<Long>();
            for (long l : var1Values) {
                allValues.add(l);
            }
            for (long l : var2Values) {
                allValues.add(l);
            }
            
            long[] newValues = new long[allValues.size()];
            int i = 0;
            for (Long l : allValues) {
                newValues[i++] = l;
            }
            
            var1Values = newValues;
            var2Values = newValues;
        }
        
        /*
         * collect "pairs" of values for var1 and var2 that fulfill op
         * 
         * e.g., if op is "==" and var1 has values {0, 1, 2} and var2 has values {1, 2, 3},
         * pairs would be {(1, 1), (2, 2)}
         * 
         * for op = "<", and same var1 and var2, pairs would be {(0, 1), (0, 2), (0, 3), (1, 2), (1, 3), (2, 3)}
         */
        
        for (long value1 : var1Values) {
            for (long value2 : var2Values) {
                
                boolean add;
                switch (op) {
                case "==":
                    add = (value1 == value2);
                    break;
                case "<":
                    add = (value1 < value2);
                    break;
                case "<=":
                    add = (value1 <= value2);
                    break;
                case ">":
                    add = (value1 > value2);
                    break;
                case ">=":
                    add = (value1 >= value2);
                    break;
                case "!=":
                    add = (value1 != value2);
                    break;
                default:
                    LOGGER.logWarning("Unkown operator: " + op);
                    continue;
                }
                
                if (add) {
                    pairs.add(new Pair(value1, value2));
                }
                
            }
        }
        
        if (pairs.isEmpty()) {
            // There exist no overlapping
            replacement = new StringBuffer(" 0 ");
            LOGGER.logWarning(var1.name + " " + op + " " + var2.name +
                    " could not be replaced, since the ranges do not overlap!");
            
        } else {
            replacement.append("(");
            
            for (Pair pair : pairs) {
                replacement.append("(");
                
                replacement.append("defined(").append(var1.getConstantName(pair.value1)).append(")");
                replacement.append(" && ");
                replacement.append("defined(").append(var2.getConstantName(pair.value2)).append(")");
                
                replacement.append(") || ");
            }
            
            // remove trailing " || "
            replacement.replace(replacement.length() - " || ".length(), replacement.length(), "");
            replacement.append(")");
        }
        
        return replacement.toString();
    }
    
    private void putNonBooleanOperation(String variable, String operator, long value) {
        Set<NonBooleanOperation> l = nonBooleanOperations.get(variable);
        if (l == null) {
            l = new HashSet<>();
            nonBooleanOperations.put(variable, l);
        }
        l.add(new NonBooleanOperation(operator, value));
    }
    
    /**
     * Preparation phase: Collects variables and required constants.
     * @param line A CPP expression (e.g. if expression).
     * @throws IOException
     */
    private void collectNonBooleanFromLine(String line) throws IOException {
        Matcher variableNameMatcher = variableNamePattern.matcher(line);
        
        while (variableNameMatcher.find()) {
            int index = variableNameMatcher.start();
            String left = line.substring(index);
            
            
            // Expression is in form of: <variable> <operator> <constant>
            boolean handled = identifyNonBooleanOperation(left, leftSide); 
            if (!handled) {
                handled = identifyNonBooleanOperation(left, rightSide); 
            }
        }
        
    }

    /**
     * Identifies {@link NonBooleanOperation}s in preprocessor blocks.
     * @param expression The expression to expression of the preprocessor block to test
     * @param variableValuePattern The expression to identify a {@link NonBooleanOperation}, one of {@link #leftSide}
     *     or {@link #rightSide}
     * @return <tt>true</tt> if the location matches to the given pattern, <tt>false</tt> otherwise.
     */
    private boolean identifyNonBooleanOperation(String expression, Pattern variableValuePattern) {
        boolean matchFound = false;
        Matcher m = variableValuePattern.matcher(expression);
        if (m.matches()) {
            matchFound = true;
            putNonBooleanOperation(m.group(GROUP_NAME_VARIABLE), m.group(GROUP_NAME_OPERATOR),
                parseConstant(m.group(GROUP_NAME_VALUE)));
        }
        return matchFound;
    }
    
    /**
     * Returns a map of variables that are constants. The key of the map are the variable names, the values in the map
     * are the constant values. Occurrences of these constant variables will be replaced by their value.
     * 
     * @return A {@link Map} of constant variables.
     */
    protected Map<String, Long> getConstants() {
        // we don't have any source to get constants from.
        // other preparations may inherit from this class and overwrite this method, if they do have a source of constants.
        return new HashMap<>();
    }

}
