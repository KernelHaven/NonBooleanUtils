package net.ssehub.kernel_haven.non_boolean.replacer;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import net.ssehub.kernel_haven.non_boolean.NonBooleanVariable;
import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;

/**
 * Positive parameterized tests for the {@link NonBooleanReplacer} with CPP expressions.
 *
 * @author Adam
 */
@RunWith(Parameterized.class)
public class CppReplacerTest {
    
    /**
     * The set of variables used for all tests.
     * 
     * <code><pre>
     * VAR_A = {0, 1, 2}
     * VAR_B = {0, 1, 2}
     * VAR_C = {0, 1}
     * </pre></code>
     */
    static final Map<String, NonBooleanVariable> DEFAULT_VARS = new HashMap<>();
    
    /**
     * The set of constants used for all tests.
     * 
     * <code><pre>
     * CONST_A = 1
     * CONST_B = 2
     * CONST_C = 0
     * </pre></code>
     */
    static final Map<String, Long> DEFAULT_CONSTANTS = new HashMap<>();
    
    static {
        Set<Long> constants = new HashSet<>();
        
        constants.add(0L);
        constants.add(1L);
        constants.add(2L);
        DEFAULT_VARS.put("VAR_A", new NonBooleanVariable("VAR_A", constants));
        DEFAULT_VARS.put("VAR_B", new NonBooleanVariable("VAR_B", constants));
        
        constants.remove(2L);
        DEFAULT_VARS.put("VAR_C", new NonBooleanVariable("VAR_C", constants));
        
        DEFAULT_CONSTANTS.put("CONST_A", 1L);
        DEFAULT_CONSTANTS.put("CONST_B", 2L);
        DEFAULT_CONSTANTS.put("CONST_C", 0L);
    }
    
    private String input;
    
    private String expected;
    
    /**
     * Creates a new {@link CppReplacerTest}.
     * 
     * @param input The input to pass to the replacer.
     * @param expected The expected output of the replacer.
     * @param name The name of this test.
     */
    public CppReplacerTest(String input, String expected, String name) {
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
                
                /*
                 * Key:
                 *      Var = Known variable
                 *      Constant = Known constant
                 *      Unknown = Unknown variable
                 *      Literal = Literal integer value
                 *      Op = operator
                 *          {equals, not equals, lt, le, gt, ge}
                 */
                
                /*
                 * Var CMP_OP Literal 
                 * + reversed
                 */
                new Object[] {"#if (VAR_A == 1)", "#if defined(VAR_A_eq_1)", "Var equals Literal"},
                new Object[] {"#if (1 == VAR_A)", "#if defined(VAR_A_eq_1)", "Var equals Literal (reversed)"},
                new Object[] {"#if (VAR_A != 1)", "#if !(defined(VAR_A_eq_1))", "Var not equals Literal"},
                new Object[] {"#if (1 != VAR_A)", "#if !(defined(VAR_A_eq_1))", "Var not equals Literal (reversed)"},
                new Object[] {"#if (VAR_A > 1)", "#if defined(VAR_A_eq_2)", "Var gt Literal"},
                new Object[] {"#if (1 < VAR_A)", "#if defined(VAR_A_eq_2)", "Var gt Literal (reversed)"},
                new Object[] {"#if (VAR_A < 1)", "#if defined(VAR_A_eq_0)", "Var lt Literal"},
                new Object[] {"#if (1 > VAR_A)", "#if defined(VAR_A_eq_0)", "Var lt Literal (reversed)"},
                new Object[] {"#if (VAR_A <= 1)", "#if (defined(VAR_A_eq_0)) || (defined(VAR_A_eq_1))", "Var le Literal"},
                new Object[] {"#if (1 >= VAR_A)", "#if (defined(VAR_A_eq_0)) || (defined(VAR_A_eq_1))", "Var le Literal (reversed)"},
                new Object[] {"#if (VAR_A >= 1)", "#if (defined(VAR_A_eq_1)) || (defined(VAR_A_eq_2))", "Var ge Literal"},
                new Object[] {"#if (1 <= VAR_A)", "#if (defined(VAR_A_eq_1)) || (defined(VAR_A_eq_2))", "Var ge Literal (reversed)"},
                
                
                /*
                 * Var CMP_OP Var
                 * + reversed
                 */
                new Object[] {"#if (VAR_A == VAR_C)", "#if ((defined(VAR_A_eq_0)) && (defined(VAR_C_eq_0))) || ((defined(VAR_A_eq_1)) && (defined(VAR_C_eq_1)))", "Var equals Var"},
                new Object[] {"#if (VAR_C == VAR_A)", "#if ((defined(VAR_C_eq_0)) && (defined(VAR_A_eq_0))) || ((defined(VAR_C_eq_1)) && (defined(VAR_A_eq_1)))", "Var equals Var (reveresed)"},
                new Object[] {"#if (VAR_A != VAR_C)", "#if !(((defined(VAR_A_eq_0)) && (defined(VAR_C_eq_0))) || ((defined(VAR_A_eq_1)) && (defined(VAR_C_eq_1))))", "Var not equals Var"},
                new Object[] {"#if (VAR_C != VAR_A)", "#if !(((defined(VAR_C_eq_0)) && (defined(VAR_A_eq_0))) || ((defined(VAR_C_eq_1)) && (defined(VAR_A_eq_1))))", "Var not equals Var (reveresed)"},
                new Object[] {"#if (VAR_A < VAR_C)", "#if (defined(VAR_A_eq_0)) && (defined(VAR_C_eq_1))", "Var lt Var"},
                new Object[] {"#if (VAR_C > VAR_A)", "#if (defined(VAR_A_eq_0)) && (defined(VAR_C_eq_1))", "Var gt Var"},
                new Object[] {"#if (VAR_A <= VAR_C)", "#if (((defined(VAR_A_eq_0)) && (defined(VAR_C_eq_0))) || ((defined(VAR_A_eq_0)) && (defined(VAR_C_eq_1)))) || ((defined(VAR_A_eq_1)) && (defined(VAR_C_eq_1)))", "Var le Var"},
                new Object[] {"#if (VAR_C >= VAR_A)", "#if (((defined(VAR_A_eq_0)) && (defined(VAR_C_eq_0))) || ((defined(VAR_A_eq_0)) && (defined(VAR_C_eq_1)))) || ((defined(VAR_A_eq_1)) && (defined(VAR_C_eq_1)))", "Var ge Var"},
                
                new Object[] {"#if VAR_A + 10 == VAR_B", "#if 0", "Var equals Var with no overlap"},
                
                /*
                 * Var CMP_OP Const
                 * + reversed
                 */
                new Object[] {"#if (VAR_A == CONST_A)", "#if defined(VAR_A_eq_1)", "Var equals Constant"},
                new Object[] {"#if (CONST_A == VAR_A)", "#if defined(VAR_A_eq_1)", "Var equals Constant (reversed)"},
                new Object[] {"#if (VAR_A != CONST_A)", "#if !(defined(VAR_A_eq_1))", "Var not equals Constant"},
                new Object[] {"#if (CONST_A != VAR_A)", "#if !(defined(VAR_A_eq_1))", "Var not equals Constant (reversed)"},
                new Object[] {"#if (VAR_A < CONST_A)", "#if defined(VAR_A_eq_0)", "Var lt Constant"},
                new Object[] {"#if (CONST_A > VAR_A)", "#if defined(VAR_A_eq_0)", "Var lt Constant (reversed)"},
                new Object[] {"#if (VAR_A <= CONST_A)", "#if (defined(VAR_A_eq_0)) || (defined(VAR_A_eq_1))", "Var le Constant"},
                new Object[] {"#if (CONST_A >= VAR_A)", "#if (defined(VAR_A_eq_0)) || (defined(VAR_A_eq_1))", "Var le Constant (reversed)"},
                new Object[] {"#if (VAR_A > CONST_A)", "#if defined(VAR_A_eq_2)", "Var gt Constant"},
                new Object[] {"#if (CONST_A < VAR_A)", "#if defined(VAR_A_eq_2)", "Var gt Constant (reversed)"},
                new Object[] {"#if (VAR_A >= CONST_A)", "#if (defined(VAR_A_eq_1)) || (defined(VAR_A_eq_2))", "Var ge Constant"},
                new Object[] {"#if (CONST_A <= VAR_A)", "#if (defined(VAR_A_eq_1)) || (defined(VAR_A_eq_2))", "Var ge Constant (reversed)"},
                
                /*
                 * defined(Var)
                 */
                new Object[] {"#if defined(VAR_A)", "#if defined(VAR_A)", "Defined with Var"},
                
                /*
                 * Var, Var with calculation, Unknown without defined()
                 */
                new Object[] {"#if VAR_A", "#if !(defined(VAR_A_eq_0))", "Missing defined with Var"},
                new Object[] {"#if VAR_A / 2", "#if !((defined(VAR_A_eq_0)) || (defined(VAR_A_eq_1)))", "Missing defined with Var multiple 0 values"},
                new Object[] {"#if VAR_A + 1", "#if 1", "Missing defined with Var no 0 values"},
                new Object[] {"#if VAR_UNKNOWN", "#if !defined(VAR_UNKNOWN_eq_0)", "Missing defined with Unknown"},
                
                /*
                 * Unknown CMP_OP Literal
                 * +reversed
                 */
                new Object[] {"#if VAR_UNKNOWN == 1", "#if defined(VAR_UNKNOWN_eq_1)", "Unknown equals Literal"},
                new Object[] {"#if VAR_UNKNOWN != 1", "#if !(defined(VAR_UNKNOWN_eq_1))", "Unknown not equals Literal"},
                new Object[] {"#if VAR_UNKNOWN >= 1", "#if defined(VAR_UNKNOWN_ge_1)", "Unknown ge Literal"},
                new Object[] {"#if VAR_UNKNOWN > 1", "#if defined(VAR_UNKNOWN_gt_1)", "Unknown gt Literal"},
                new Object[] {"#if VAR_UNKNOWN <= 1", "#if defined(VAR_UNKNOWN_le_1)", "Unknown le Literal"},
                new Object[] {"#if VAR_UNKNOWN < 1", "#if defined(VAR_UNKNOWN_lt_1)", "Unknown lt Literal"},
                new Object[] {"#if 1 == VAR_UNKNOWN", "#if defined(VAR_UNKNOWN_eq_1)", "Literal equals Unknown"},
                new Object[] {"#if 1 != VAR_UNKNOWN", "#if !(defined(VAR_UNKNOWN_eq_1))", "Literal not equals Unknown"},
                new Object[] {"#if 1 <= VAR_UNKNOWN", "#if defined(VAR_UNKNOWN_ge_1)", "Literal le Unknown"},
                new Object[] {"#if 1 < VAR_UNKNOWN", "#if defined(VAR_UNKNOWN_gt_1)", "Literal le Unknown"},
                new Object[] {"#if 1 >= VAR_UNKNOWN", "#if defined(VAR_UNKNOWN_le_1)", "Literal ge Unknown"},
                new Object[] {"#if 1 > VAR_UNKNOWN", "#if defined(VAR_UNKNOWN_lt_1)", "Literal gt Unknown"},
                
                /*
                 * Var CMP_OP Literal
                 * Literal is out of range for allowed values of Var
                 * +reversed
                 */
                new Object[] {"#if VAR_A == 5", "#if 0", "Var equals Literal out of Range"},
                new Object[] {"#if VAR_A > 5", "#if 0", "Var gt Literal out of Range"},
                new Object[] {"#if VAR_A >= 5", "#if 0", "Var ge Literal out of Range"},
                new Object[] {"#if VAR_A < -1", "#if 0", "Var lt Literal out of Range"},
                new Object[] {"#if VAR_A <= -1", "#if 0", "Var le Literal out of Range"},
                new Object[] {"#if 5 == VAR_A", "#if 0", "Var equals Literal out of Range (reversed)"},
                new Object[] {"#if 5 < VAR_A", "#if 0", "Var gt Literal out of Range (reversed)"},
                new Object[] {"#if 5 <= VAR_A", "#if 0", "Var ge Literal out of Range (reversed)"},
                new Object[] {"#if -1 > VAR_A", "#if 0", "Var lt Literal out of Range (reversed)"},
                new Object[] {"#if -1 >= VAR_A", "#if 0", "Var le Literal out of Range (reversed)"},
                
                /*
                 * Containing boolean operators &&, ||, !
                 */
                new Object[] {"#if VAR_A == 1 || (!(VAR_B==1) && VAR_C==1)", "#if (defined(VAR_A_eq_1)) || ((!(defined(VAR_B_eq_1))) && (defined(VAR_C_eq_1)))", "Boolean Operators"},
                
                /*
                 * Literal CMP_OP Literal
                 */
                new Object[] {"#if 1 == 2", "#if 0", "Literal equals Literal (false)"},
                new Object[] {"#if 1 == 1", "#if 1", "Literal equals Literal (true)"},
                new Object[] {"#if 1 != 2", "#if 1", "Literal not equals Literal (true)"},
                new Object[] {"#if 1 != 1", "#if 0", "Literal not equals Literal (false)"},
                new Object[] {"#if 1 > 2", "#if 0", "Literal gt Literal (false)"},
                new Object[] {"#if 2 > 1", "#if 1", "Literal gt Literal (true)"},
                new Object[] {"#if 1 >= 2", "#if 0", "Literal ge Literal (false)"},
                new Object[] {"#if 1 >= 1", "#if 1", "Literal ge Literal (true)"},
                new Object[] {"#if 1 < 2", "#if 1", "Literal lt Literal (true)"},
                new Object[] {"#if 2 < 1", "#if 0", "Literal lt Literal (false)"},
                new Object[] {"#if 1 <= 2", "#if 1", "Literal le Literal (true)"},
                new Object[] {"#if 3 <= 2", "#if 0", "Literal le Literal (false)"},
                new Object[] {"#if -3 <= 2", "#if 1", "Negative Literal le Literal (true)"},
                
                /*
                 * Literal without comparison
                 */
                new Object[] {"#if 1", "#if 1", "Literal without comparison (true)"},
                new Object[] {"#if 2", "#if 1", "Literal without comparison (true)"},
                new Object[] {"#if -2", "#if 1", "Literal without comparison (true)"},
                new Object[] {"#if 0", "#if 0", "Literal without comparison (false)"},
                new Object[] {"#if 10 + (-3 * 3) - 1", "#if 0", "Calculated Literal without comparison (false)"},
                
                /*
                 * (Literal INT_OP Literal) EQUAL Literal
                 */
                new Object[] {"#if 1 + 2 == 3", "#if 1", "Literal calculation (ADD)"},
                new Object[] {"#if 1 - 2 == -1", "#if 1", "Literal calculation (SUB)"},
                new Object[] {"#if 1 * 2 == 2", "#if 1", "Literal calculation (MUL)"},
                new Object[] {"#if 1 / 2 == 0", "#if 1", "Literal calculation (DIV)"},
                new Object[] {"#if 9 / 2 == 4", "#if 1", "Literal calculation (DIV)"},
                new Object[] {"#if 9 % 2 == 1", "#if 1", "Literal calculation (MOD)"},
                new Object[] {"#if 10 % 2 == 0", "#if 1", "Literal calculation (MOD)"},
                new Object[] {"#if -5 == -1 * 5", "#if 1", "Literal calculation (Unary MINUS)"},
                new Object[] {"#if +5 == 5", "#if 1", "Literal calculation (Unary PLUS)"},
                new Object[] {"#if (5 & 2) == 0", "#if 1", "Literal calculation (Bin AND)"},
                new Object[] {"#if (6 & 2) == 2", "#if 1", "Literal calculation (Bin AND)"},
                new Object[] {"#if (5 | 2) == 7", "#if 1", "Literal calculation (Bin OR)"},
                new Object[] {"#if (6 | 2) == 6", "#if 1", "Literal calculation (Bin OR)"},
                new Object[] {"#if (5 ^ 2) == 7", "#if 1", "Literal calculation (Bin XOR)"},
                new Object[] {"#if (6 ^ 2) == 4", "#if 1", "Literal calculation (Bin XOR)"},
                new Object[] {"#if ~2 == " + (~2L), "#if 1", "Literal calculation (Bin INVERT)"},
                
                /*
                 * (Var INT_OP Literal) EQUAL Literal
                 */
                new Object[] {"#if VAR_A + 2 == 3", "#if defined(VAR_A_eq_1)", "Var calculation (ADD)"},
                new Object[] {"#if VAR_A - 2 == 0", "#if defined(VAR_A_eq_2)", "Var calculation (SUB)"},
                new Object[] {"#if VAR_A * 2 == 4", "#if defined(VAR_A_eq_2)", "Var calculation (MUL)"},
                new Object[] {"#if VAR_A / 3 == 0", "#if ((defined(VAR_A_eq_0)) || (defined(VAR_A_eq_1))) || (defined(VAR_A_eq_2))", "Var calculation (DIV)"},
                new Object[] {"#if VAR_A % 2 == 0", "#if (defined(VAR_A_eq_0)) || (defined(VAR_A_eq_2))", "Var calculation (MOD)"},
                new Object[] {"#if VAR_A % 2 == 1", "#if defined(VAR_A_eq_1)", "Var calculation (MOD)"},
                new Object[] {"#if -VAR_A == -1", "#if defined(VAR_A_eq_1)", "Var calculation (Unary MINUS)"},
                new Object[] {"#if +VAR_A == 1", "#if defined(VAR_A_eq_1)", "Var calculation (Unary PLUS)"},
                new Object[] {"#if (VAR_A & 1) == 1", "#if defined(VAR_A_eq_1)", "Var calculation (Bin AND)"},
                new Object[] {"#if (VAR_A | 1) == 3", "#if defined(VAR_A_eq_2)", "Var calculation (Bin OR)"},
                new Object[] {"#if (VAR_A ^ 1) == 3", "#if defined(VAR_A_eq_2)", "Var calculation (Bin XOR)"},
                new Object[] {"#if ~VAR_A == " + (~1L), "#if defined(VAR_A_eq_1)", "Var calculation (Bin INVERT)"},
                
                /*
                 * (Literal INT_OP Var) EQUAL Literal
                 */
                new Object[] {"#if 2 + VAR_A == 3", "#if defined(VAR_A_eq_1)", "Var calculation (ADD) (reversed)"},
                new Object[] {"#if 2 - VAR_A == 0", "#if defined(VAR_A_eq_2)", "Var calculation (SUB) (reversed)"},
                new Object[] {"#if 2 * VAR_A == 4", "#if defined(VAR_A_eq_2)", "Var calculation (MUL) (reversed)"},
                new Object[] {"#if 2 / (VAR_A + 1) == 1", "#if defined(VAR_A_eq_1)", "Var calculation (DIV) (reversed)"},
                new Object[] {"#if 5 % (VAR_A + 1) == 0", "#if defined(VAR_A_eq_0)", "Var calculation (MOD) (reversed)"},
                new Object[] {"#if (1 & VAR_A) == 1", "#if defined(VAR_A_eq_1)", "Var calculation (Bin AND) (reversed)"},
                new Object[] {"#if (1 | VAR_A) == 3", "#if defined(VAR_A_eq_2)", "Var calculation (Bin OR) (reversed)"},
                new Object[] {"#if (1 ^ VAR_A) == 3", "#if defined(VAR_A_eq_2)", "Var calculation (Bin XOR) (reversed)"},
                
                /*
                 * Var CMP_OP Unknown
                 * + reversed
                 */
                new Object[] {"#if VAR_A == VAR_UNKNOWN", "#if defined(VAR_A_eq_VAR_UNKNOWN)", "Var and Unknown comparison"},
                new Object[] {"#if VAR_UNKNOWN == VAR_A", "#if defined(VAR_A_eq_VAR_UNKNOWN)", "Var and Unknown comparison"},
                new Object[] {"#if VAR_A != VAR_UNKNOWN", "#if !(defined(VAR_A_eq_VAR_UNKNOWN))", "Var and Unknown comparison"},
                new Object[] {"#if VAR_UNKNOWN != VAR_A", "#if !(defined(VAR_A_eq_VAR_UNKNOWN))", "Var and Unknown comparison"},
                new Object[] {"#if VAR_A < VAR_UNKNOWN", "#if defined(VAR_A_lt_VAR_UNKNOWN)", "Var and Unknown comparison"},
                new Object[] {"#if VAR_UNKNOWN < VAR_A", "#if defined(VAR_A_gt_VAR_UNKNOWN)", "Var and Unknown comparison"},
                new Object[] {"#if VAR_A <= VAR_UNKNOWN", "#if defined(VAR_A_le_VAR_UNKNOWN)", "Var and Unknown comparison"},
                new Object[] {"#if VAR_UNKNOWN <= VAR_A", "#if defined(VAR_A_ge_VAR_UNKNOWN)", "Var and Unknown comparison"},
                new Object[] {"#if VAR_A > VAR_UNKNOWN", "#if defined(VAR_A_gt_VAR_UNKNOWN)", "Var and Unknown comparison"},
                new Object[] {"#if VAR_UNKNOWN > VAR_A", "#if defined(VAR_A_lt_VAR_UNKNOWN)", "Var and Unknown comparison"},
                new Object[] {"#if VAR_A >= VAR_UNKNOWN", "#if defined(VAR_A_ge_VAR_UNKNOWN)", "Var and Unknown comparison"},
                new Object[] {"#if VAR_UNKNOWN >= VAR_A", "#if defined(VAR_A_le_VAR_UNKNOWN)", "Var and Unknown comparison"},
                
                /*
                 * Unknown CMP_OP Unknown
                 */
                new Object[] {"#if UNKNOWN1 == UNKNOWN2", "#if defined(UNKNOWN1_eq_UNKNOWN2)", "Unknown and Unknown comparison"},
                new Object[] {"#if UNKNOWN1 != UNKNOWN2", "#if !(defined(UNKNOWN1_eq_UNKNOWN2))", "Unknown and Unknown comparison"},
                new Object[] {"#if UNKNOWN1 < UNKNOWN2", "#if defined(UNKNOWN1_lt_UNKNOWN2)", "Unknown and Unknown comparison"},
                new Object[] {"#if UNKNOWN1 <= UNKNOWN2", "#if defined(UNKNOWN1_le_UNKNOWN2)", "Unknown and Unknown comparison"},
                new Object[] {"#if UNKNOWN1 > UNKNOWN2", "#if defined(UNKNOWN2_lt_UNKNOWN1)", "Unknown and Unknown comparison"},
                new Object[] {"#if UNKNOWN1 >= UNKNOWN2", "#if defined(UNKNOWN2_le_UNKNOWN1)", "Unknown and Unknown comparison"},
                
                /*
                 * (Var INT_OP Var) equals Literal
                 */
                new Object[] {"#if VAR_A + VAR_C == 0", "#if (defined(VAR_A_eq_0)) && (defined(VAR_C_eq_0))", "Var PLUS Var"},
                new Object[] {"#if VAR_A - VAR_C == 0", "#if ((defined(VAR_A_eq_0)) && (defined(VAR_C_eq_0))) || ((defined(VAR_A_eq_1)) && (defined(VAR_C_eq_1)))", "Var MINUS Var"},
                new Object[] {"#if VAR_A * VAR_C == 1", "#if (defined(VAR_A_eq_1)) && (defined(VAR_C_eq_1))", "Var PLUS Var"},
                
                /*
                 * (Var INT_OP Var INT_OP Var) equals Literal
                 */
                new Object[] {"#if VAR_A + VAR_B + VAR_C == 0", "#if ((defined(VAR_A_eq_0)) && (defined(VAR_B_eq_0))) && (defined(VAR_C_eq_0))", "Var PLUS Var PLUS Var"},
                
                /*
                 * (Var INT_OP Var INT_OP Literal) equals Literal
                 * + reversed
                 */
                new Object[] {"#if (VAR_A + VAR_C) + 1 == 1", "#if (defined(VAR_A_eq_0)) && (defined(VAR_C_eq_0))", "(Var PLUS Var) PLUS Literal"},
                new Object[] {"#if VAR_A + (VAR_C + 1) == 1", "#if (defined(VAR_A_eq_0)) && (defined(VAR_C_eq_0))", "Var PLUS (Var PLUS Literal)"},
                new Object[] {"#if 2 + (VAR_A + VAR_C) == 2", "#if (defined(VAR_A_eq_0)) && (defined(VAR_C_eq_0))", "Literal PLUS (Var PLUS Var)"},
                new Object[] {"#if (2 + VAR_A) + VAR_C == 2", "#if (defined(VAR_A_eq_0)) && (defined(VAR_C_eq_0))", "(Literal PLUS Var) PLUS Var"},
                
                /*
                 * prefixes
                 */
                new Object[] {"#if VAR_A", "#if !(defined(VAR_A_eq_0))", "#if prefix with space"},
                new Object[] {"#if(VAR_A)", "#if !(defined(VAR_A_eq_0))", "#if prefix without space"},
                new Object[] {"#elif VAR_A", "#elif !(defined(VAR_A_eq_0))", "#elif prefix with space"},
                new Object[] {"#elif(VAR_A)", "#elif !(defined(VAR_A_eq_0))", "#elif prefix without space"},
                
                /*
                 * From old NonBooleanPreperationTest
                 */
                new Object[] {"#if ((VAR_A & 2) > 0)", "#if defined(VAR_A_eq_2)", "bitOperationHas2"},
                new Object[] {"#if (VAR_A || VAR_B)", "#if (!(defined(VAR_A_eq_0))) || (!(defined(VAR_B_eq_0)))", "booleanNumbersInIf"},
                new Object[] {"#if ((VAR_A) == VAR_C)", "#if ((defined(VAR_A_eq_0)) && (defined(VAR_C_eq_0))) || ((defined(VAR_A_eq_1)) && (defined(VAR_C_eq_1)))", "unnecessaryBrackets"},
                new Object[] {"#if (VAR_A % 2) == 0", "#if (defined(VAR_A_eq_0)) || (defined(VAR_A_eq_2))", "modulo"},
                new Object[] {"#if 1l==1ul", "#if 1", "ifOneUL2"},
                new Object[] {"#if 1L==1UL", "#if 1", "ifOneUL"},
                new Object[] {"#if (defined(VAR_C) || VAR_C)", "#if (defined(VAR_C)) || (!(defined(VAR_C_eq_0)))", "ifdefWeirdCombination"},
                new Object[] {"#if ((VAR_A == VAR_C) && (VAR_B == 2))", "#if (((defined(VAR_A_eq_0)) && (defined(VAR_C_eq_0))) || ((defined(VAR_A_eq_1)) && (defined(VAR_C_eq_1)))) && (defined(VAR_B_eq_2))", "complexExpression"},
                new Object[] {"#if (!VAR1 || !VAR2)", "#if (!(!defined(VAR1_eq_0))) || (!(!defined(VAR2_eq_0)))", "booleanNumbersInIfNegated"}
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
        NonBooleanReplacer replacer = new NonBooleanReplacer(DEFAULT_VARS, DEFAULT_CONSTANTS);
        
        String actual = replacer.replaceCpp(input);
        
        assertEquals(expected, actual);
    }
    
}
