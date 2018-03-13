package net.ssehub.kernel_haven.non_boolean.replacer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import net.ssehub.kernel_haven.non_boolean.NonBooleanPreperation.NonBooleanVariable;
import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;

/**
 * Tests the {@link CppReplacer}.
 * 
 * @author Adam
 */
public class CppReplacerTest {

    /**
     * <code><pre>
     * VAR_A = {0, 1, 2}
     * VAR_B = {0, 1, 2}
     * VAR_C = {0, 1}
     * </pre></code>
     */
    private static final Map<String, NonBooleanVariable> DEFAULT_VARS = new HashMap<>();
    
    /**
     * <code><pre>
     * CONST_A = 1
     * CONST_B = 2
     * CONST_C = 0
     * </pre></code>
     */
    private static final Map<String, Long> DEFAULT_CONSTANTS = new HashMap<>();
    
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
    
    /**
     * Tests that a line not starting with #if or #elif correctly throws an exception.
     * 
     * @throws ExpressionFormatException wanted.
     */
    @Test(expected = ExpressionFormatException.class)
    public void testNonIfElif() throws ExpressionFormatException {
        CppReplacer replacer = new CppReplacer(DEFAULT_VARS, DEFAULT_CONSTANTS);
        replacer.replace("#ifdef A");
    }
    
    /**
     * Tests that a simple comparison with variable and literal is replaced correctly.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testVarEqLiteral() throws ExpressionFormatException {
        CppReplacer replacer = new CppReplacer(DEFAULT_VARS, DEFAULT_CONSTANTS);
        
        assertThat(replacer.replace("#if (VAR_A == 1)"), is("#if defined(VAR_A_eq_1)"));
        assertThat(replacer.replace("#if (1 == VAR_A)"), is("#if defined(VAR_A_eq_1)"));
    }
    
    /**
     * Tests that a simple comparison with two variables is replaced correctly.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testVarEqVar() throws ExpressionFormatException {
        CppReplacer replacer = new CppReplacer(DEFAULT_VARS, DEFAULT_CONSTANTS);
        
        assertThat(replacer.replace("#if (VAR_A == VAR_C)"),
                is("#if ((defined(VAR_A_eq_0)) && (defined(VAR_C_eq_0))) || ((defined(VAR_A_eq_1)) && (defined(VAR_C_eq_1)))"));
        assertThat(replacer.replace("#if (VAR_C == VAR_A)"),
                is("#if ((defined(VAR_C_eq_0)) && (defined(VAR_A_eq_0))) || ((defined(VAR_C_eq_1)) && (defined(VAR_A_eq_1)))"));
    }
    
    /**
     * Tests that a simple comparison with variable and literal is replaced correctly.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testVarEqConst() throws ExpressionFormatException {
        CppReplacer replacer = new CppReplacer(DEFAULT_VARS, DEFAULT_CONSTANTS);
        
        assertThat(replacer.replace("#if (VAR_A == CONST_A)"), is("#if defined(VAR_A_eq_1)"));
        assertThat(replacer.replace("#if (CONST_A == VAR_A)"), is("#if defined(VAR_A_eq_1)"));
    }
    
}
