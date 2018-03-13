package net.ssehub.kernel_haven.non_boolean;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
import net.ssehub.kernel_haven.non_boolean.replacer.CppReplacer;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Util;
import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;
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
    private static final boolean REMOVE_CONSISTENCY_CHECKS = true;
    
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

    public static final class NonBooleanVariable {
        
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
        checkIfNonBooleanVarModelIsAvailable();
        
        // walk through all *.c and *.h files in the source_tree, and collect non boolean operations.
        // this fills this.nonBooleanOperations
        if (!nonBooleanModelRead) {
            new PreprocessorConditionVisitor() {
                
                @Override
                public void visit(File file, String line) {
                    try {
                        collectNonBooleanFromLine(file, line);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.visitAllFiles(originalSourceTree);
        }
        
        variables = new HashMap<>();
        
        // convert the nonBooleanOperations we found earlier into NonBooleanVariables (this is our heuristic)
        // if the variability model contains FiniteIntegerVariables, use them instead of our heuristic
        // this fills this.variables
        gatherConstantValues();
        
        // copy the source_tree to destination, while replacing the relational expressions with NonBoolean variables
        LOGGER.logDebug("Copying from " + originalSourceTree.getAbsolutePath() + " to " + copiedSourceTree.getAbsolutePath());
        copy(originalSourceTree, copiedSourceTree);
    }

    private void checkIfNonBooleanVarModelIsAvailable() {
        VariabilityModel varModel = PipelineConfigurator.instance().getVmProvider().getResult();
        if (null != varModel) {
            for (VariabilityVariable variable : varModel.getVariables()) {
                if (null != variable && variable instanceof FiniteIntegerVariable) {
                    nonBooleanModelRead = true;
                    break;
                }
            }
        }
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
    
    private String replaceInLine(String line, File from) {
        
        CppReplacer replacer = new CppReplacer(variables, getConstants());
        
        String result = line;
        try {
            result = replacer.replace(line);
        } catch (ExpressionFormatException e) {
            LOGGER.logException("Error whilte replacing the following line in " + from + ": " + line, e);
        }
        return result;
    }
    
    private Long parseConstant(String constant) {
        return parseConstant(null, constant);
    }
    
    private Long parseConstant(File file, String constant) {
        while (!constant.isEmpty() && (constant.endsWith("L") || constant.endsWith("l")
                || constant.endsWith("u") || constant.endsWith("U"))) {
            
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
    private void collectNonBooleanFromLine(File file, String line) throws IOException {
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
     * @param expression The expression to expression of the preprocessor block to test
     * @param variableValuePattern The expression to identify a {@link NonBooleanOperation}, one of {@link #leftSide}
     *     or {@link #rightSide}
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
