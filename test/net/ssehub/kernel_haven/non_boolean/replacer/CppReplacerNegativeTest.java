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
 * Negative parameterized tests for the {@link CppReplacer}.
 *
 * @author Adam
 */
@RunWith(Parameterized.class)
public class CppReplacerNegativeTest {

    private String input;
    
    /**
     * Creates a new {@link CppReplacerNegativeTest}.
     * 
     * @param input The input value for the replacer.
     * @param name The name of this test.
     */
    public CppReplacerNegativeTest(String input, String name) {
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
                new Object[] {"#ifdef A", "Not #if or #elif"},
                
                /*
                 * defined()
                 */
                new Object[] {"#if defined()", "Empty defined"},
                new Object[] {"#if something(A)", "Invalid function"},
                new Object[] {"#if defined(!A)", "Defined with no simple variable as parameter"},
                
                /*
                 * unsuported operators
                 */
                new Object[] {"#if VAR_A++", "Unsupported operator ++"},
                new Object[] {"#if VAR_A--", "Unsupported operator --"},
                new Object[] {"#if ++VAR_A", "Unsupported operator ++"},
                new Object[] {"#if --VAR_A", "Unsupported operator --"},
                new Object[] {"#if 4 >> 2", "Unsupported operator >>"},
                new Object[] {"#if 4 << 2", "Unsupported operator <<"},
                
                /*
                 * calculating on boolean results
                 */
                // unary
                new Object[] {"#if -(1 == 2)", "Calcating on boolean (Unary MINUS)"},
                new Object[] {"#if ~(1 == 2)", "Calcating on boolean (Bin INVERT)"},
                new Object[] {"#if (1 == 2)++", "Calcating on boolean (INC Post)"},
                new Object[] {"#if (1 == 2)--", "Calcating on boolean (DEC Post)"},
                new Object[] {"#if ++(1 == 2)", "Calcating on boolean (INC Pre)"},
                new Object[] {"#if --(1 == 2)", "Calcating on boolean (DEC Pre)"},
                // binary
                new Object[] {"#if (1 == 2) + 1", "Calcating on boolean (ADD)"},
                new Object[] {"#if (1 == 2) - 1", "Calcating on boolean (SUB)"},
                new Object[] {"#if (1 == 2) * 1", "Calcating on boolean (MUL)"},
                new Object[] {"#if (1 == 2) / 1", "Calcating on boolean (DIV)"},
                new Object[] {"#if (1 == 2) % 1", "Calcating on boolean (MOD)"},
                new Object[] {"#if (1 == 2) & 1", "Calcating on boolean (Bin AND)"},
                new Object[] {"#if (1 == 2) | 1", "Calcating on boolean (Bin OR)"},
                new Object[] {"#if (1 == 2) ^ 1", "Calcating on boolean (Bin XOR)"},
                // binary reversed
                new Object[] {"#if 1 + (1 == 2)", "Calcating on boolean (ADD) (reversed)"},
                new Object[] {"#if 1 - (1 == 2)", "Calcating on boolean (SUB) (reversed)"},
                new Object[] {"#if 1 * (1 == 2)", "Calcating on boolean (MUL) (reversed)"},
                new Object[] {"#if 1 / (1 == 2)", "Calcating on boolean (DIV) (reversed)"},
                new Object[] {"#if 1 % (1 == 2)", "Calcating on boolean (MOD) (reversed)"},
                new Object[] {"#if 1 & (1 == 2)", "Calcating on boolean (Bin AND) (reversed)"},
                new Object[] {"#if 1 | (1 == 2)", "Calcating on boolean (Bin OR) (reversed)"},
                new Object[] {"#if 1 ^ (1 == 2)", "Calcating on boolean (Bin XOR) (reversed)"},
                
                /*
                 * comparing on boolean results
                 */
                new Object[] {"#if (1 == 2) == 1", "Comparing on boolean (EQ)"},
                new Object[] {"#if (1 == 2) != 1", "Comparing on boolean (NQ)"},
                new Object[] {"#if (1 == 2) < 1", "Comparing on boolean (LT)"},
                new Object[] {"#if (1 == 2) <= 1", "Comparing on boolean (LE)"},
                new Object[] {"#if (1 == 2) > 1", "Comparing on boolean (GT)"},
                new Object[] {"#if (1 == 2) >= 1", "Comparing on boolean (GE)"},
                // reversed
                new Object[] {"#if 1 == (1 == 2)", "Comparing on boolean (EQ) (reversed)"},
                new Object[] {"#if 1 != (1 == 2)", "Comparing on boolean (NQ) (reversed)"},
                new Object[] {"#if 1 < (1 == 2)", "Comparing on boolean (LT) (reversed)"},
                new Object[] {"#if 1 <= (1 == 2)", "Comparing on boolean (LE) (reversed)"},
                new Object[] {"#if 1 > (1 == 2)", "Comparing on boolean (GT) (reversed)"},
                new Object[] {"#if 1 >= (1 == 2)", "Comparing on boolean (GE) (reversed)"},
                
                /*
                 * Calculating / comparing boolean result with Var
                 */
                new Object[] {"#if VAR_A == (1 == 2)", "Comparing on boolean with Var (EQ)"},
                new Object[] {"#if VAR_A < (1 == 2)", "Comparing on boolean with Var (LT)"},
                new Object[] {"#if VAR_A > (1 == 2)", "Comparing on boolean with Var (GT)"},
                new Object[] {"#if VAR_A <= (1 == 2)", "Comparing on boolean with Var (LE)"},
                new Object[] {"#if VAR_A >= (1 == 2)", "Comparing on boolean with Var (GE)"},
                new Object[] {"#if VAR_A + (1 == 2)", "Calcuating on boolean with Var"},
                
                /*
                 * Comparing on already "resolved" unknown variable
                 */
                new Object[] {"#if 1 == (UNKNOWN == 1)", "Comparing on resolved unknown (EQ)"},
                new Object[] {"#if 1 < (UNKNOWN == 1)", "Comparing on resolved unknown (LT)"},
                new Object[] {"#if 1 > (UNKNOWN == 1)", "Comparing on resolved unknown (GT)"},
                new Object[] {"#if 1 <= (UNKNOWN == 1)", "Comparing on resolved unknown (LE)"},
                new Object[] {"#if 1 >= (UNKNOWN == 1)", "Comparing on resolved unknown (GE)"},
                // reversed
                new Object[] {"#if (UNKNOWN == 1) == 1", "Comparing on resolved unknown (EQ) (reversed)"},
                new Object[] {"#if (UNKNOWN == 1) < 1", "Comparing on resolved unknown (LT) (reversed)"},
                new Object[] {"#if (UNKNOWN == 1) > 1", "Comparing on resolved unknown (GT) (reversed)"},
                new Object[] {"#if (UNKNOWN == 1) <= 1", "Comparing on resolved unknown (LE) (reversed)"},
                new Object[] {"#if (UNKNOWN == 1) >= 1", "Comparing on resolved unknown (GE) (reversed)"},
                // with Var on left side
                new Object[] {"#if Var_A == (UNKNOWN == 1)", "Comparing on resolved unknown with Var (EQ)"},
                new Object[] {"#if Var_A < (UNKNOWN == 1)", "Comparing on resolved unknown with Var (LT)"},
                new Object[] {"#if Var_A > (UNKNOWN == 1)", "Comparing on resolved unknown with Var (GT)"},
                new Object[] {"#if Var_A <= (UNKNOWN == 1)", "Comparing on resolved unknown with Var (LE)"},
                new Object[] {"#if Var_A >= (UNKNOWN == 1)", "Comparing on resolved unknown with Var (GE)"},
                // with Var on right side
                new Object[] {"#if (UNKNOWN == 1) == Var_A", "Comparing on resolved unknown with Var (EQ) (reversed)"},
                new Object[] {"#if (UNKNOWN == 1) < Var_A", "Comparing on resolved unknown with Var (LT) (reversed)"},
                new Object[] {"#if (UNKNOWN == 1) > Var_A", "Comparing on resolved unknown with Var (GT) (reversed)"},
                new Object[] {"#if (UNKNOWN == 1) <= Var_A", "Comparing on resolved unknown with Var (LE) (reversed)"},
                new Object[] {"#if (UNKNOWN == 1) >= Var_A", "Comparing on resolved unknown with Var (GE) (reversed)"},
                
                /*
                 * Calculating on Unknown
                 */
                // unary
                new Object[] {"#if -UNKNOWN", "Calcating on Unknown (Unary MINUS)"},
                new Object[] {"#if ~UNKNOWN", "Calcating on Unknown (Bin INVERT)"},
                new Object[] {"#if UNKNOWN++", "Calcating on Unknown (INC Post)"},
                new Object[] {"#if UNKNOWN--", "Calcating on Unknown (DEC Post)"},
                new Object[] {"#if ++UNKNOWN", "Calcating on Unknown (INC Pre)"},
                new Object[] {"#if --UNKNOWN", "Calcating on Unknown (DEC Pre)"},
                // binary
                new Object[] {"#if UNKNOWN + 1", "Calcating on Unknown (ADD)"},
                new Object[] {"#if UNKNOWN - 1", "Calcating on Unknown (SUB)"},
                new Object[] {"#if UNKNOWN * 1", "Calcating on Unknown (MUL)"},
                new Object[] {"#if UNKNOWN / 1", "Calcating on Unknown (DIV)"},
                new Object[] {"#if UNKNOWN % 1", "Calcating on Unknown (MOD)"},
                new Object[] {"#if UNKNOWN & 1", "Calcating on Unknown (Bin AND)"},
                new Object[] {"#if UNKNOWN | 1", "Calcating on Unknown (Bin OR)"},
                new Object[] {"#if UNKNOWN ^ 1", "Calcating on Unknown (Bin XOR)"},
                // binary reversed
                new Object[] {"#if 1 + UNKNOWN", "Calcating on Unknown (ADD) (reversed)"},
                new Object[] {"#if 1 - UNKNOWN", "Calcating on Unknown (SUB) (reversed)"},
                new Object[] {"#if 1 * UNKNOWN", "Calcating on Unknown (MUL) (reversed)"},
                new Object[] {"#if 1 / UNKNOWN", "Calcating on Unknown (DIV) (reversed)"},
                new Object[] {"#if 1 % UNKNOWN", "Calcating on Unknown (MOD) (reversed)"},
                new Object[] {"#if 1 & UNKNOWN", "Calcating on Unknown (Bin AND) (reversed)"},
                new Object[] {"#if 1 | UNKNOWN", "Calcating on Unknown (Bin OR) (reversed)"},
                new Object[] {"#if 1 ^ UNKNOWN", "Calcating on Unknown (Bin XOR) (reversed)"},
                
                /*
                 * Comparing unknown with (Var + Var)
                 * + reversed
                 */
                new Object[] {"#if (VAR_A + VAR_C) == UNKNOWN", "Comparing (Var + Var) with Uknown"},
                new Object[] {"#if (VAR_A + VAR_C) != UNKNOWN", "Comparing (Var + Var) with Uknown"},
                new Object[] {"#if (VAR_A + VAR_C) < UNKNOWN", "Comparing (Var + Var) with Uknown"},
                new Object[] {"#if (VAR_A + VAR_C) <= UNKNOWN", "Comparing (Var + Var) with Uknown"},
                new Object[] {"#if (VAR_A + VAR_C) > UNKNOWN", "Comparing (Var + Var) with Uknown"},
                new Object[] {"#if (VAR_A + VAR_C) >= UNKNOWN", "Comparing (Var + Var) with Uknown"},
                new Object[] {"#if UNKNOWN == (VAR_A + VAR_C)", "Comparing (Var + Var) with Uknown (reversed)"},
                new Object[] {"#if UNKNOWN != (VAR_A + VAR_C)", "Comparing (Var + Var) with Uknown (reversed)"},
                new Object[] {"#if UNKNOWN < (VAR_A + VAR_C)", "Comparing (Var + Var) with Uknown (reversed)"},
                new Object[] {"#if UNKNOWN <= (VAR_A + VAR_C)", "Comparing (Var + Var) with Uknown (reversed)"},
                new Object[] {"#if UNKNOWN > (VAR_A + VAR_C)", "Comparing (Var + Var) with Uknown (reversed)"},
                new Object[] {"#if UNKNOWN >= (VAR_A + VAR_C)", "Comparing (Var + Var) with Uknown (reversed)"}
        );
    }
    // CHECKSTYLE:ON
    
    /**
     * Tests that the input given in the constructor produces an {@link ExpressionFormatException}.
     */
    @Test
    public void test() {
        CppReplacer replacer = new CppReplacer(CppReplacerTest.DEFAULT_VARS, CppReplacerTest.DEFAULT_CONSTANTS);
        
        try {
            String result = replacer.replace(input);
            fail("Expected ExpressionFormatException, but got: " + result);
        } catch (ExpressionFormatException e) {
            // expected
        }
    }
    
}
