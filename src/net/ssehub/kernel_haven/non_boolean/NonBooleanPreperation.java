package net.ssehub.kernel_haven.non_boolean;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.ssehub.kernel_haven.IPreparation;
import net.ssehub.kernel_haven.PipelineConfigurator;
import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.config.Configuration;
import net.ssehub.kernel_haven.config.DefaultSettings;
import net.ssehub.kernel_haven.non_boolean.heuristic.NonBooleanHeuristic;
import net.ssehub.kernel_haven.non_boolean.replacer.NonBooleanReplacer;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Util;
import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;
import net.ssehub.kernel_haven.variability_model.VariabilityModel;
import net.ssehub.kernel_haven.variability_model.VariabilityVariable;

/**
 * A {@link IPreparation} that replaces all non-boolean operations in C preprocessor lines in a given project.
 * This copies the complete source_tree to make the necessary replacements. This uses either
 * {@link FiniteIntegerVariable}s from the variability model, or if these are not available it uses a heuristic to
 * detect possible ranges for non-boolean variables.
 * 
 * @author Adam
 * @author El-Sharkawy
 */
public class NonBooleanPreperation implements IPreparation {
    
    private static final Logger LOGGER = Logger.get();
    
    private static final boolean REMOVE_CONSISTENCY_CHECKS = true;
    
    private File originalSourceTree;
    
    private File copiedSourceTree;
    
    private NonBooleanReplacer replacer;
    
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
        
        try {
            prepare(config);
        } catch (IOException e) {
            throw new SetUpException(e);
        }
        
        config.setValue(DefaultSettings.SOURCE_TREE, copiedSourceTree);
        PipelineConfigurator.instance().getCmProvider().setConfig(config);
    }

    /**
     * Prepare the source tree. This first gathers the {@link NonBooleanVariable}s (either from
     * {@link VariabilityModel} or from the {@link NonBooleanHeuristic}) and then copies {@link #originalSourceTree}
     * to {@link #copiedSourceTree} while doing the replacements.
     * 
     * @param config The configuration to use.
     * 
     * @throws IOException If reading or writing files fails.
     * @throws SetUpException If creating the heuristic fails.
     */
    private synchronized void prepare(Configuration config) throws IOException, SetUpException {
        LOGGER.logDebug("Starting preperation...");
        
        // make sure that the destination is empty
        if (copiedSourceTree.exists()) {
            for (File oldFile : copiedSourceTree.listFiles()) {
                if (oldFile.isDirectory()) {
                    Util.deleteFolder(oldFile);                    
                } else {
                    if (!oldFile.delete()) {
                        LOGGER.logWarning2("Could not delete ", oldFile.getAbsolutePath());
                    }
                }
            }
        } else {
            try {
                boolean success = copiedSourceTree.mkdir();
                if (!success) {
                    LOGGER.logWarning2("Could not create ", copiedSourceTree.getName(), " in ",
                            copiedSourceTree.getParentFile().getAbsolutePath());
                } else {
                    LOGGER.logError2("Created ", copiedSourceTree.getName(), " in ",
                            copiedSourceTree.getParentFile().getAbsolutePath());
                }
            } catch (SecurityException exc) {
                LOGGER.logException("Cannot create " + copiedSourceTree.getName() + " in "
                        + copiedSourceTree.getParentFile().getAbsolutePath(), exc);
                throw new SetUpException(exc);
            }            
        }
        
        
        Map<String, NonBooleanVariable> variables = new HashMap<>();
        boolean nonBooleanModelRead = false;
        
        // Try to use information of variability model -> exact approach
        VariabilityModel varModel = PipelineConfigurator.instance().getVmProvider().getResult();
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
                    
                } else if (variable instanceof InfiniteIntegerVariable) {
                    nonBooleanModelRead = true;
                    variables.put(variable.getName(), 
                            new NonBooleanVariable(variable.getName(), new HashSet<>(), true));
                }
            }
        }
        
        if (!nonBooleanModelRead) {
            // walk through all *.c and *.h files in the source_tree, and collect non boolean operations.
            NonBooleanHeuristic heuristic = new NonBooleanHeuristic(config);
            heuristic.addAllSourceFiles(originalSourceTree);
            variables = heuristic.getResult();
        }
        
        this.replacer = new NonBooleanReplacer(variables, getConstants());
        
        // copy the source_tree to destination, while replacing the relational expressions with NonBoolean variables
        LOGGER.logDebug("Copying from " + originalSourceTree.getAbsolutePath() + " to "
                + copiedSourceTree.getAbsolutePath());
        copy(originalSourceTree, copiedSourceTree);
    }

    
    /**
     * Copies the given file. If the file is a .c or .h file, then replacements are done. If from is a directory
     * then this recursively copies the files inside it.
     * 
     * @param from The file to copy.
     * @param to The destination.
     * 
     * @throws IOException If copying the file fails.
     */
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
    
    /**
     * Copies a source file (.c or .h) while doing replacements.
     * 
     * @param from The file to copy.
     * @param to The destination.
     * 
     * @throws IOException If copying the file fails.
     */
    private void copySourceFile(File from, File to) throws IOException {
        try (LineNumberReader in = new LineNumberReader(new FileReader(from))) {
            
            try (Writer out = createWriter(to)) {
                
                String line;
                // CHECKSTYLE:OFF // TODO inner assignment
                while (continueReading(line = in.readLine())) {
                // CHECKSTYLE:ON
                    
                    // get line number here, so that we get the first line if any continuation is appended below
                    int currentLineNumber = in.getLineNumber();
                    
                    // Replace variable occurrences of #if's and #elif's
                    if (CPPUtils.isIfOrElifStatement(line)) {
                        
                        // Consider continuation
                        while (line.charAt(line.length() - 1) == '\\') {
                            line = line.substring(0, line.length() - 1); // remove trailing \
                            String next = in.readLine();
                            if (null != next) {
                                line += next;
                            } else {
                                break;
                            }
                        }
                        
                        line = line.trim();
                        line = replaceInLine(line, from, currentLineNumber);
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
     * Whether we should keep continue reading and do replacements in the source file.
     *  
     * @param line The line that was just read.
     * 
     * @return Whether this line should be handled, too. Must return false if line is <code>null</code>.
     */
    protected boolean continueReading(String line) {
        return line != null;
    }
    
    /**
     * Creates the writer to use (either {@link BufferedWriter} or {@link CppBufferedWriter}.
     * 
     * @param to The destination where to save the copied file.
     * @return The writer to use.
     * 
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
     * Replaces non-boolean operations in the given CPP #if or #elif line. This uses the {@link NonBooleanReplacer}.
     * 
     * @param line The line to do replacements in.
     * @param from The file we are currently in (used for error messages).
     * @param lineNumber The current line number (used for error messages).
     * 
     * @return The line with replacements done; or the original line if the replacer failed.
     */
    private String replaceInLine(String line, File from, int lineNumber) {
        String result = removeComments(line);
        try {
            result = replacer.replaceCpp(result);
        } catch (ExpressionFormatException e) {
            LOGGER.logException("Error whilte replacing line " + lineNumber + " in " + from + ": " + line, e);
        }
        return result;
    }
    
    /**
     * Removes inline (/* ... *&#47;)  and line (//) comments from the given line.
     * Package visibility for test cases.
     * 
     * @param original The line to remove the comments from.
     * @return The line with comments removed.
     */
    static String removeComments(String original) {
        String replaced = original;
        
        if (original.indexOf('/') != -1) {
            StringBuilder result = new StringBuilder();
            
            char[] chars = original.toCharArray();
            boolean inlineComment = false;
            
            for (int i = 0; i < chars.length; i++) {
                if (inlineComment) {
                    if (chars[i] == '/' && chars[i - 1] == '*') {
                        inlineComment = false;
                    }
                    
                } else {
                    if (chars[i] == '/' && i + 1 < chars.length && chars[i + 1] == '/') {
                        break; // line comment, everything from now on is removed
                    } else if (chars[i] == '/' && i + 1 < chars.length && chars[i + 1] == '*') {
                        inlineComment = true;
                    } else {
                        result.append(chars[i]);
                    }
                }
            }
            
            replaced = result.toString();
        }
        
        return replaced;
    }
    
    /**
     * Returns a map of variables that are constants. The key of the map are the variable names, the values in the map
     * are the constant values. Occurrences of these constant variables will be replaced by their value.
     * 
     * @return A {@link Map} of constant variables.
     */
    protected Map<String, Long> getConstants() {
        // we don't have any source to get constants from.
        // other preparations may inherit from this class and overwrite this method,
        // if they do have a source of constants.
        return new HashMap<>();
    }

}
