package net.ssehub.kernel_haven.non_boolean;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link CppBufferedWriter}.
 * @author El-Sharkawy
 *
 */
public class CppBufferedWriterTest {
    
    /**
     * Tests C code with a CPP block, which shall not be removed.
     */
    @Test
    public void testWriteCppBlock() {
        String input = "#if A\n//A Line\n#endif\n";
        String result = readCode(input);
        
        Assert.assertEquals(input, result);
    }
    
    /**
     * Tests C code with a CPP block, which shall not be removed.
     */
    @Test
    public void testSimpleRemoval() {
        String input = "//some code before\n#if A\n#error >>> An error message\n#endif\n//some code after\n";
        String result = readCode(input);
        
        Assert.assertEquals("//some code before\n" + CppBufferedWriter.REPLACEMENT + "\n//some code after\n", result);
    }
    
    /**
     * Tests C code with a CPP block, which shall not be removed.
     */
    @Test
    public void testSimpleRemovalWithElse() {
        String input = "//some code before\n#if A\n#error >>> An error message\n#else\n#endif\n//some code after\n";
        String result = readCode(input);
        
        Assert.assertEquals("//some code before\n" + CppBufferedWriter.REPLACEMENT + "\n//some code after\n", result);
    }
    
    /**
     * Tests a removal of a nested, but not surrounding cpp block.
     */
    @Test
    public void testRemovalOfNestedIf() {
        String input = "#if OUTER\n//some code before\n#if INNER\n#error >>> An error message\n#endif\n#endif\n";
        String result = readCode(input);
        
        Assert.assertEquals("#if OUTER\n//some code before\n" + CppBufferedWriter.REPLACEMENT + "\n#endif\n", result);
    }
    
    /**
     * Tests a removal of a nested and also surrounding cpp block.
     */
    @Test
    public void testRemovalOfStructure() {
        String input = "//some code before\n#if OUTER\n#if INNER\n#error >>> An error message\n#endif\n#endif\n";
        String result = readCode(input);
        
        Assert.assertEquals("//some code before\n" + CppBufferedWriter.REPLACEMENT + "\n", result);
    }

    /**
     * Helper method, uses the {@link CppBufferedWriter} and returns the result what it has written.
     * @param input The input (multiple lines of C and CPP code).
     * @return The output what the {@link CppBufferedWriter} writes.
     */
    private String readCode(String input) {
        String[] inputLines = input.split("\n");
        StringWriter sWriter = new StringWriter();
        BufferedWriter out = new BufferedWriter(sWriter);
        CppBufferedWriter writer = new CppBufferedWriter(out);
        try {
            for (String line : inputLines) {
                writer.write(line);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
        String result = sWriter.toString();
        return result;
    }

}
