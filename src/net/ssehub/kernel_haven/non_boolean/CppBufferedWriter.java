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
package net.ssehub.kernel_haven.non_boolean;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A special writer, which will automatically omit all preprocessor blocks containing an <tt>&#35;error</tt> statement.
 * @author El-Sharkawy
 *
 */
public class CppBufferedWriter extends Writer {
    public static final String REPLACEMENT = "/********************************************\n"
        + " * Consistency check removed by KernelHaven *\n"
        + " ********************************************/";

    /**
     * Stores a preprocessor line and the nesting depth of it.
     * @author El-Sharkawy
     *
     */
    private class CppLine {
        private String line;
        private String effectiveLine;
        private int nestingDepth;
        private boolean cppBlock;
        
        /**
         * Sole constructor.
         * @param line The cpp line
         * @param cppBlock <tt>true</tt> if an c preprocessor <tt>if, else, elif, endif</tt>.
         * @param nestingDepth The nesting of the cpp statement within all cpp blocks.
         */
        private CppLine(String line, boolean cppBlock, int nestingDepth) {
            this(line, line, cppBlock, nestingDepth);
        }
        
        /**
         * Sole constructor.
         * @param line The cpp line
         * @param effectiveLine Rewritten cpp line if previous line has been removed.
         * @param cppBlock <tt>true</tt> if an c preprocessor <tt>if, else, elif, endif</tt>.
         * @param nestingDepth The nesting of the cpp statement within all cpp blocks.
         */
        private CppLine(String line, String effectiveLine, boolean cppBlock, int nestingDepth) {
            this.line = line;
            this.effectiveLine = effectiveLine;
            this.nestingDepth = nestingDepth;
            this.cppBlock = cppBlock;
        }
        
        @Override
        public String toString() {
            return nestingDepth + ": " + line + "\n";
        }
    }
    
    private List<CppLine> cppLines = new ArrayList<>(500);
    private BufferedWriter out;
    private int nestingDepth = 0;
    private int skipAtNesting = -1;
    private List<String> removedConditions = new ArrayList<>(5);
    
    /**
     * Sole constructor for this class.
     * @param out The writer which saves the transformed output.
     */
    public CppBufferedWriter(BufferedWriter out) {
        this.out = out;
    }

    /**
     * The line to write without a line break at the end.
     * @param line The line to test (if it contains an error statement) and to write
     * @throws IOException If writing the output is not possible.
     */
    @Override
    public void write(String line) throws IOException {
        String trimedLine = line.trim().toLowerCase();
        if (trimedLine.startsWith("#if")) {
            // if, ifndef, ifdef
            nestingDepth++;
            cppLines.add(new CppLine(line, true, nestingDepth));
        } else if (trimedLine.startsWith("#endif")) {
            // endif
            boolean added = false;
            if (skipAtNesting == -1 || skipAtNesting > nestingDepth) {
                added = true;
                cppLines.add(new CppLine(line, true, nestingDepth));
            }
            nestingDepth--;
            
            // Check if this CPP block has contained only (a) CPP block(s), which was/were removed
            if (skipAtNesting >= 0 && added) {
                check(nestingDepth + 1);
            }
            
            // Write code lines if we reached top level of file
            if (nestingDepth <= 0) {
                flush();
                nestingDepth = 0;
                skipAtNesting = -1;
            }
        } else if (trimedLine.startsWith("#el")) {
            // else, elif
            if (skipAtNesting == -1 || skipAtNesting > nestingDepth) {
                // Nothing to remove, continue
                cppLines.add(new CppLine(line, true, nestingDepth));
            } else {
                if (!removedConditions.isEmpty()) {
                    // Rewrite else/elif and check if we should also delete this block or rewrite it.
                    StringBuilder newLine = new StringBuilder();
                    if (trimedLine.startsWith("#elif")) {
                        int start = line.toLowerCase().indexOf("#elif") + 5;
                        newLine.append("#if (!(");
                        newLine.append(getRemovedConditions());
                        newLine.append(") && ");
                        // Attention: condition must not be transformed to lower case
                        newLine.append(line.substring(start).trim());
                        newLine.append(")");
                    } else {
                        newLine.append("#if !(");
                        newLine.append(getRemovedConditions());
                        newLine.append(")");
                    }
                    cppLines.add(new CppLine(line, newLine.toString(), true, nestingDepth));
                    skipAtNesting = -1;
                }
            }
        } else if (trimedLine.startsWith("#error")) {
            // Delete this statement and surrounding block
            removeCppBlock(nestingDepth);
            skipAtNesting = nestingDepth;
        } else {
            if (nestingDepth > 0) {
                // C code or other cpp like define, needs to be stored in list to ensure ordering of output
                cppLines.add(new CppLine(line, false, nestingDepth));
            } else {
                out.write(line);
                out.write("\n");
            }
        }
    }

    /**
     * Removes a cached CPP block at the specified nesting level.
     * @param nestingDepth The nesting level of the block to remove.
     */
    private void removeCppBlock(int nestingDepth) {
        Iterator<CppLine> itr = cppLines.iterator();
        int firstRemovalIndex = -1;
        int index = -1;
        while (itr.hasNext()) {
            index++;
            
            CppLine currentElement = itr.next();
            if (currentElement.nestingDepth >= nestingDepth) {
                itr.remove();
                
                // Store the condition to rewrite else/elif elements if necessary
                if (currentElement.cppBlock) {
                    String function = null;
                    String line = currentElement.line.toLowerCase();
                    int start = line.indexOf("#ifndef");
                    int offset = 0;
                    if (-1 == start) {
                        start = line.indexOf("#ifdef");
                        if (-1 != start) {
                            // length of #ifdef
                            offset += 6;
                            function = "defined(";
                        }
                    } else {
                        // length of #ifndef
                        offset += 7;
                        function = "!defined(";
                    }
                    if (-1 == start) {
                        start = line.indexOf("#if");
                        if (-1 != start) {
                            // length of #if
                            offset += 3;
                        }
                    }
                    if (-1 == start) {
                        start = line.indexOf("#elif");
                        if (-1 != start) {
                            // length of #elif
                            offset += 5;
                        }
                    }
                    
                    // Extract condition
                    if (-1 != start) {
                        String condition = currentElement.line.substring(start + offset).trim();
                        if (null != function) {
                            removedConditions.add(function + condition + ")");
                        } else {
                            removedConditions.add(condition);
                        }
                    }
                }
                
                if (-1 == firstRemovalIndex) {
                    // Mark that we have removed at least one element at this index
                    firstRemovalIndex = index;
                }
            }
        }
        
        if (-1 != firstRemovalIndex) {
            // Added as CPP block = true, to allow removal if parent block is removed and also replaced by this string
            cppLines.add(firstRemovalIndex, new CppLine(REPLACEMENT, true, nestingDepth));
        }
    }
    
    /**
     * Checks (and removes) whether CPP blocks on the specified level should be removed. This is the case if a CPP block
     * contained an error statement or only other CPP blocks which have been removed for this reason.
     * @param nestingToCheck The nesting level of the block to check (and remove).
     */
    private void check(int nestingToCheck) {
        boolean shouldBeRemoved = false;
        
        Iterator<CppLine> itr = cppLines.iterator();
        while (itr.hasNext()) {
            CppLine cppLine = itr.next();
            if (cppLine.nestingDepth == nestingToCheck) {
                shouldBeRemoved = cppLine.cppBlock;
                
                if (!shouldBeRemoved) {
                    break;
                }
            }
        }
        
        if (shouldBeRemoved) {
            removeCppBlock(nestingToCheck);
        }
    }
    
    /**
     * Concatenates all removed CPP conditions via a conjunction.<br/>
     * <b>Attention:</b> check that {@link #removedConditions} is not empty before!
     * @return The composed, removed conditions.
     */
    private String getRemovedConditions() {
        StringBuilder result = new StringBuilder();
        result.append(removedConditions.get(0).trim());
        for (int i = 1; i < removedConditions.size(); i++) {
            result.append(" && ");
            result.append(removedConditions.get(i).trim());
        }
        
        return result.toString();
    }

    @Override
    public void close() throws IOException {
        flush();
        out.close();
    }
    
    /**
     * Flushes the currently processed lines.
     * @throws IOException If writing the output is not possible.
     */
    @Override
    public void flush() throws IOException {
        if (!cppLines.isEmpty()) {
            for (CppLine cppLine : cppLines) {
                out.write(cppLine.effectiveLine);
                out.write("\n");
            }
        }
        removedConditions.clear();
        cppLines.clear();
        out.flush();
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        out.write(cbuf, off, len);
    }
}
