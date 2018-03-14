package net.ssehub.kernel_haven.non_boolean.replacer;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;

/**
 * Negative parameterized tests for the {@link NonBooleanReplacer} with non-CPP expressions.
 *
 * @author Adam
 */
@RunWith(Parameterized.class)
public class NonCppReplacerNegativeTest {

    private String input;
    
    /**
     * Creates a new {@link NonCppReplacerNegativeTest}.
     * 
     * @param input The input value for the replacer.
     * @param name The name of this test.
     */
    public NonCppReplacerNegativeTest(String input, String name) {
        this.input = input;
    }
    
    /**
     * Creates the parameters for this test.
     * 
     * @return The parameters of this test.
     */
    // CHECKSTYLE:OFF
    @Parameters(name = "{1}: {0}")
    public static Collection<Object[]> getParameters() {
        return Arrays.asList(
                // input, name
                new Object[] {"defined(VAR_A)", "Defined function"},
                new Object[] {"func()", "Other function"}
        );
    }
    // CHECKSTYLE:ON
    
    /**
     * Tests that the input given in the constructor produces an {@link ExpressionFormatException}.
     */
    @Test
    public void test() {
        NonBooleanReplacer replacer = new NonBooleanReplacer(CppReplacerTest.DEFAULT_VARS,
                CppReplacerTest.DEFAULT_CONSTANTS);
        
        try {
            String result = replacer.replaceNonCpp(input);
            fail("Expected ExpressionFormatException, but got: " + result);
        } catch (ExpressionFormatException e) {
            // expected
        }
    }
    
}
