package net.ssehub.kernel_haven.non_boolean.replacer;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;

/**
 * Positive parameterized tests for the {@link NonBooleanReplacer} with non-CPP expressions.
 *
 * @author Adam
 */
@RunWith(Parameterized.class)
public class NonCppReplacerTest {

    private String input;
    
    private String expected;
    
    /**
     * Creates a new {@link NonCppReplacerTest}.
     * 
     * @param input The input to pass to the replacer.
     * @param expected The expected output of the replacer.
     * @param name The name of this test.
     */
    public NonCppReplacerTest(String input, String expected, String name) {
        this.input = input;
        this.expected = expected;
    }

    /**
     * Creates the parameters for this test.
     * 
     * @return The parameters of this test.
     */
    // CHECKSTYLE:OFF
    @Parameters(name = "{2}: {0}")
    public static Collection<Object[]> getParameters() {
        return Arrays.asList(
                // input, expected, name
                new Object[] {"VAR_A == 1", "VAR_A_eq_1", "Var equals Literal"},
                new Object[] {"VAR_A", "!(VAR_A_eq_0)", "Var without comparison"},
                new Object[] {"UNKNOWN", "!UNKNOWN_eq_0", "Unknown without comparison"},
                new Object[] {"2", "1", "Literal (true)"},
                new Object[] {"0", "0", "Literal (false)"},
                new Object[] {"VAR_A == 5", "0", "Boolean result"},
                new Object[] {"VAR_C != 0 || (VAR_A == 1 && !(VAR_B == 1))", "(!(VAR_C_eq_0)) || ((VAR_A_eq_1) && (!(VAR_B_eq_1)))", "Boolean operators"},
                new Object[] {"!(1 == 0)", "1", "Boolean: !false"},
                new Object[] {"!(1 == 1)", "0", "Boolean: !true"}
        );
    }
    // CHECKSTYLE:ON
    
    /**
     * Checks that the input given in the constructor produces the expected output given in the constructor.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void test() throws ExpressionFormatException {
        NonBooleanReplacer replacer = new NonBooleanReplacer(CppReplacerTest.DEFAULT_VARS,
                CppReplacerTest.DEFAULT_CONSTANTS);
        
        String actual = replacer.replaceNonCpp(input);
        
        assertEquals(expected, actual);
    }
    
}
