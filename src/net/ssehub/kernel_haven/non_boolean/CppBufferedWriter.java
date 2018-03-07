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
        private int nestingDepth;
        private boolean cppBlock;
        
        /**
         * Sole constructor.
         * @param line The cpp line
         * @param cppBlock <tt>true</tt> if an c preprocessor <tt>if, else, elif, endif</tt>.
         * @param nestingDepth The nesting of the cpp statement within all cpp blocks.
         */
        private CppLine(String line, boolean cppBlock, int nestingDepth) {
            this.line = line;
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
        String trimedLine = line.trim();
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
                cppLines.add(new CppLine(line, true, nestingDepth));
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
            
            if (itr.next().nestingDepth >= nestingDepth) {
                itr.remove();
                if (-1 == firstRemovalIndex) {
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
                out.write(cppLine.line);
                out.write("\n");
            }
        }
        cppLines.clear();
        out.flush();
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        out.write(cbuf, off, len);
    }
}
