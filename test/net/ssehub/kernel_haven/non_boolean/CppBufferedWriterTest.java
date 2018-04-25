package net.ssehub.kernel_haven.non_boolean;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests the {@link CppBufferedWriter}.
 * @author El-Sharkawy
 *
 */
@RunWith(Parameterized.class)
public class CppBufferedWriterTest {
    
    private String input;
    private String expected;
    
    /**
     * Creates a new {@link CppBufferedWriterTest}.
     * 
     * @param input The input to pass to the {@link CppBufferedWriter}.
     * @param expected The expected output of the {@link CppBufferedWriter}.
     * @param name The name of this test (won't be used for testing).
     */
    public CppBufferedWriterTest(String input, String expected, String name) {
        this.input = input;
        this.expected = expected;
    }
    
    /**
     * Creates the parameters for this test.
     * 
     * @return The parameters of this test.
     */
    // CHECKSTYLE:OFF
    @Parameters(name = "{2}")
    public static String[][] getParameters() {
        return new String[][]{
            // Blocks to remove
            {"//some code before\n#if A\n#error >>> An error message\n#endif\n//some code after\n",
                "//some code before\n" + CppBufferedWriter.REPLACEMENT + "\n//some code after\n",
                "Removal of toplevel block"},
            {"//some code before\n#if A\n#error >>> An error message\n#else\n#endif\n//some code after\n",
                "//some code before\n" + CppBufferedWriter.REPLACEMENT + "\n//some code after\n",
                "Removal of toplevel block"},
            {"#if OUTER\n//some code before\n#if INNER\n#error >>> An error message\n#endif\n#endif\n",
                "#if OUTER\n//some code before\n" + CppBufferedWriter.REPLACEMENT + "\n#endif\n",
                "Removal of nested block"},
            {"//some code before\n#if OUTER\n#if INNER\n#error >>> An error message\n#endif\n#endif\n",
                "//some code before\n" + CppBufferedWriter.REPLACEMENT + "\n",
                "Removal of outer and inner block"},
            
            // Statements, which should not be changed
            {"#if A\n//A Line\n#endif\n", "#if A\n//A Line\n#endif\n", "Desired CPP-Blocks are kept"},
        };
    }
    
    /**
     * Parameterized test method.
     */
    @Test
    public void test() {
        String result = readCode(input);
        
        Assert.assertEquals(expected, result);
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
