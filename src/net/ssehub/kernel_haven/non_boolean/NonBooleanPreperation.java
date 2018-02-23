package net.ssehub.kernel_haven.non_boolean;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    
    /**
     * A set of burnt variables. A variable is burnt, if we found a non boolean expression with it, that we cannot
     * handle. Currently, these are only logged to console (info level). In the future, this set may be used to
     * remove them from any analysis, since the results may be screwed.
     */
    private Set<String> burntVariables;
    
    private Pattern variableNamePattern;
    private Pattern leftSide;
    
    /**
     * Checks that a variable stands on the <b>left</b> side of an expression.<br/>
     * <tt> &lt;variable&gt; &lt;operator&gt; * </tt>
     */
    private Pattern comparisonLeft;
    
    /**
     * Checks that a variable stands on the <b>right</b> side of an expression.<br/>
     * <tt> * &lt;operator&gt; &lt;variable&gt; </tt>
     */
    private Pattern comparisonRight;
    private Pattern leftSideFinder;
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
        String variableRegex = variableNamePattern.pattern();
        
        try {
            
            leftSide = Pattern.compile("^"
                + createdNamedCaptureGroup(GROUP_NAME_VARIABLE, variableRegex)
                + "\\s*"
                + createdNamedCaptureGroup(GROUP_NAME_OPERATOR, SUPPORTED_OPERATORS_REGEX)
                + "\\s*"
                + createdNamedCaptureGroup(GROUP_NAME_VALUE, "-?[0-9]+")
                + ".*");

            comparisonLeft = Pattern.compile("^"
                + createdNamedCaptureGroup(GROUP_NAME_VARIABLE, variableRegex)
                + "\\s*"
                + createdNamedCaptureGroup(GROUP_NAME_OPERATOR, SUPPORTED_OPERATORS_REGEX)
                + ".*");
            
            comparisonRight = Pattern.compile(".*"
                + createdNamedCaptureGroup(GROUP_NAME_OPERATOR, SUPPORTED_OPERATORS_REGEX)
                + "\\s*"
                + createdNamedCaptureGroup(GROUP_NAME_VARIABLE, variableRegex)
                + "$");
            
            leftSideFinder = Pattern.compile(
                createdNamedCaptureGroup(GROUP_NAME_VARIABLE, variableRegex)
                + "\\s*"
                + createdNamedCaptureGroup(GROUP_NAME_OPERATOR, SUPPORTED_OPERATORS_REGEX)
                + "\\s*"
                + createdNamedCaptureGroup(GROUP_NAME_VALUE, "-?[0-9]+"));
            
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
         * .
         * @param operator .
         * @param value .
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
        burntVariables = new HashSet<>();
        
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
        
        LOGGER.logInfo("Burnt variables: " + burntVariables);
//        LOGGER.logInfo("Variables: " + variables);
        
        
        // copy the source_tree to destination, while replacing the approiapte expressions with NonBoolean variables
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
            
            try (BufferedWriter out = new BufferedWriter(new FileWriter(to))) {
                
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
                        line = replaceInLine(line);
                    }
                    
                    out.write(line);
                    out.write("\n");
                }
                
            }
        }
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
    
    private String replaceInLine(String line) {
        String result = line;
        Matcher m;
        
        // replace known constants in result
        Map<String, Integer> constants = getConstants();
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
        
        m = leftSideFinder.matcher(result);
        while (m.find()) {
            String whole = m.group();
            String name = m.group(GROUP_NAME_VARIABLE);
            String op = m.group(GROUP_NAME_OPERATOR);
            long value = Long.parseLong(m.group(GROUP_NAME_VALUE));
            
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
                    value++;
                    // fall through
                case ">=":
                    List<Long> greaterValuesToAdd = new ArrayList<>(var.getConstants().length);
                    
                    for (long c : var.getConstants()) {
                        if (c >= value) {
                            greaterValuesToAdd.add(c);
                        }
                    }
                    
                    replacement = expandComparison(var, greaterValuesToAdd);
                    
                    break;
                    
                case "<":
                    value--;
                    // fall through
                case "<=":
                    List<Long> lesserValuesToAdd = new ArrayList<>(var.getConstants().length);
                    
                    for (long c : var.getConstants()) {
                        if (c <= value) {
                            lesserValuesToAdd.add(c);
                        }
                    }
                    
                    replacement = expandComparison(var, lesserValuesToAdd);
                    
                    break;
                }
                // Only replace if we had no error, i.e., if we could resolve all parts
                result = result.replace(whole, replacement);
            }
        }
        
        // Check if it is a comparison between two variables and try it again
        m = twoVariablesExpression.matcher(result);
        while (m.find()) {
            String whole = m.group();
            String firstVar = m.group(GROUP_NAME_VARIABLE);
            String op = m.group(GROUP_NAME_OPERATOR);
            String secondVar = m.group(GROUP_NAME_VALUE);
            
            NonBooleanVariable var1 = getVariableForced(firstVar);
            NonBooleanVariable var2 = getVariableForced(secondVar);
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
            result = result.replace(whole, replacement);
        }
        
        return result;
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
        
        /*
         * collect "pairs" of values for var1 and var2 that fulfill op
         * 
         * e.g., if op is "==" annd var1 has values {0, 1, 2} and var2 has values {1, 2, 3},
         * pairs would be {(1, 1), (2, 2)}
         * 
         * for op = "<", and same var1 and var2, pairs would be {(0, 1), (0, 2), (0, 3), (1, 2), (1, 3), (2, 3)}
         */
        
        for (long value1 : var1.constants) {
            for (long value2 : var2.constants) {
                
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
    
    private void printErr(String line, int index) {
        System.err.println(line);
        for (int i = 0; i < index; i++) {
            System.err.print(' ');
        }
        System.err.println('^');
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
            
            String name = variableNameMatcher.group();
            
            String right = line.substring(0, index + name.length());
            
            Matcher m = leftSide.matcher(left);
            if (m.matches()) {
                // Expression is in form of: <variable> <operator> <constant>
                putNonBooleanOperation(m.group(GROUP_NAME_VARIABLE), m.group(GROUP_NAME_OPERATOR),
                    Long.parseLong(m.group(GROUP_NAME_VALUE)));
            } else {
                boolean leftMatch = comparisonLeft.matcher(left).matches();
                boolean rightMatch = comparisonRight.matcher(right).matches();
                if (leftMatch || rightMatch) {
                    burntVariables.add(name);
                    printErr(line, index);
                }
                
            }
            
        }
        
    }
    
    /**
     * Returns a map of variables that are constants. The key of the map are the variable names, the values in the map
     * are the constant values. Occurrences of these constant variables will be replaced by their value.
     * 
     * @return A {@link Map} of constant variables.
     */
    protected Map<String, Integer> getConstants() {
        // we don't have any source to get constants from.
        // other preparations may inherit from this class and overwrite this method, if they do have a source of constants.
        return new HashMap<>();
    }

}
