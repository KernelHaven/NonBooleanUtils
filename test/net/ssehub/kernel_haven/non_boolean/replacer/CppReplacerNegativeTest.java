package net.ssehub.kernel_haven.non_boolean.replacer;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;

/**
 * Negative parameterized tests for the {@link CppReplacer}.
 *
 * @author Adam
 */
@RunWith(Parameterized.class)
public class CppReplacerNegativeTest {

    @Parameters(name = "{1}: {0}")
    public static Collection<Object[]> getParameters() {
        return Arrays.asList(
                // input, name
                new Object[] {"#ifdef A", "Not #if or #elif"},
                new Object[] {"#if defined()", "Empty defined"},
                new Object[] {"#if something(A)", "Invalid function"},
                new Object[] {"#if defined(!A)", "Defined with no simple variable as parameter"},
                new Object[] {"#if VAR_A++", "Unsupported operator"}
        );
    }
    
    private String input;
    
    public CppReplacerNegativeTest(String input, String name) {
        this.input = input;
    }
    
    @Test(expected = ExpressionFormatException.class)
    public void test() throws ExpressionFormatException {
        CppReplacer replacer = new CppReplacer(CppReplacerTest.DEFAULT_VARS, CppReplacerTest.DEFAULT_CONSTANTS);
        
        replacer.replace(input);
    }
    
}
