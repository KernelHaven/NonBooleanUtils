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
        final String error = "#error >>> errMessage\n";
        return new String[][]{
            
            // Blocks to remove
            {"//Code: Before\n#if A\n" + error + "#endif\n//Code: After\n",
                "//Code: Before\n" + CppBufferedWriter.REPLACEMENT + "\n//Code: After\n",
                "Unnested Removal"},
            {"#if OUTER\n//Code: Before\n#if INNER\n" + error + "#endif\n#endif\n",
                "#if OUTER\n//Code: Before\n" + CppBufferedWriter.REPLACEMENT + "\n#endif\n",
                "Nested Removal"},
            {"//Code: Before\n#if OUTER\n#if INNER\n" + error + "#endif\n#endif\n",
                "//Code: Before\n" + CppBufferedWriter.REPLACEMENT + "\n",
                "(Un)Nested Removal"},
            
            // Statements, which should not be changed
            {"#if A\n//A Line\n#endif\n", "#if A\n//A Line\n#endif\n", "Desired CPP-Blocks are kept"},
            {"//Code\n", "//Code\n", "No CPP"},
            
            // Mixed statements
            {"//before\n#if A\n" + error + "#elif B\n//code to keep\n#endif\n//after\n",
                "//before\n" + CppBufferedWriter.REPLACEMENT + "\n#if (!(A) && B)\n//code to keep\n#endif\n//after\n",
                "Partial removal - 1"},
            {"//before\n#ifdef A\n" + error + "#elif B\n//code to keep\n#endif\n//after\n",
                "//before\n" + CppBufferedWriter.REPLACEMENT
                    + "\n#if (!(defined(A)) && B)\n//code to keep\n#endif\n//after\n",
                "Partial removal - 2"},
            {"//before\n#ifndef A\n" + error + "#elif B\n//code to keep\n#endif\n//after\n",
                "//before\n" + CppBufferedWriter.REPLACEMENT
                    + "\n#if (!(!defined(A)) && B)\n//code to keep\n#endif\n//after\n",
                "Partial removal - 3"},
            {"//before\n#if A\n" + error + "#elif B\n" + error + "#elif C\n//code to keep\n#endif\n//after\n",
                "//before\n" + CppBufferedWriter.REPLACEMENT + "\n#if (!(A && B) && C)\n"
                    + "//code to keep\n#endif\n//after\n",
                "Partial removal - 4"},
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
