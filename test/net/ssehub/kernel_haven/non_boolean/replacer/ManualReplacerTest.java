package net.ssehub.kernel_haven.non_boolean.replacer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import net.ssehub.kernel_haven.non_boolean.FiniteIntegerVariable;
import net.ssehub.kernel_haven.non_boolean.InfiniteIntegerVariable;
import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;
import net.ssehub.kernel_haven.variability_model.VariabilityModel;
import net.ssehub.kernel_haven.variability_model.VariabilityVariable;

/**
 * Test cases for the {@link NonBooleanReplacer} that are not parameterized.
 * 
 * @author Adam
 */
public class ManualReplacerTest {

    /**
     * Tests that ignoredFunctions are ignored.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testIgnoredFunction() throws ExpressionFormatException {
        NonBooleanReplacer replacer = new NonBooleanReplacer(CppReplacerTest.DEFAULT_VARS,
                CppReplacerTest.DEFAULT_CONSTANTS);
        
        Set<String> ignoredFunctions = new HashSet<>();
        ignoredFunctions.add("i");
        replacer.setIgnoredFunctions(ignoredFunctions);
        
        assertThat(replacer.replaceNonCpp("i(VAR_A) == 2"), is("VAR_A_eq_2"));
    }
    
    /**
     * Tests that different functions can be handled the same as defined.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testDifferentDefinedLikeFunction() throws ExpressionFormatException {
        NonBooleanReplacer replacer = new NonBooleanReplacer(CppReplacerTest.DEFAULT_VARS,
                CppReplacerTest.DEFAULT_CONSTANTS);
        
        Set<String> definedLikeFunctions = new HashSet<>();
        definedLikeFunctions.add("something");
        replacer.setDefinedLikeFunctions(definedLikeFunctions);
        
        assertThat(replacer.replaceCpp("#if something(VAR_A)"), is("#if defined(VAR_A)"));
    }
    
    /**
     * Tests that explicitly registering defined as a definedLikeFunction works, and that a call to replaceCpp does
     * not de-register it again.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testDefinedRegisteredExplicity() throws ExpressionFormatException {
        NonBooleanReplacer replacer = new NonBooleanReplacer(CppReplacerTest.DEFAULT_VARS,
                CppReplacerTest.DEFAULT_CONSTANTS);
        
        Set<String> definedLikeFunctions = new HashSet<>();
        definedLikeFunctions.add("defined");
        replacer.setDefinedLikeFunctions(definedLikeFunctions);
        
        // test that defined is now considered in replaceNonCpp
        assertThat(replacer.replaceNonCpp("defined(VAR_A)"), is("VAR_A"));
        
        // test that a call to replaceCpp does not de-register defined again
        assertThat(replacer.replaceCpp("#if defined(VAR_A)"), is("#if defined(VAR_A)"));
        assertThat(replacer.replaceNonCpp("defined(VAR_A)"), is("VAR_A"));
    }
    
    /**
     * Tests that defined is not registered permanently by a single call to replaceCpp.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testDefinedOnlyRegisteredTemporarily() throws ExpressionFormatException {
        NonBooleanReplacer replacer = new NonBooleanReplacer(CppReplacerTest.DEFAULT_VARS,
                CppReplacerTest.DEFAULT_CONSTANTS);
        
        // test that defined throws an exceptin in in replaceNonCpp
        try {
            replacer.replaceNonCpp("defined(VAR_A)");
            fail("expected exception");
        } catch (ExpressionFormatException e) {
        }
        
        // test that a call to replaceCpp does not register defined
        assertThat(replacer.replaceCpp("#if defined(VAR_A)"), is("#if defined(VAR_A)"));
        try {
            replacer.replaceNonCpp("defined(VAR_A)");
            fail("expected exception");
        } catch (ExpressionFormatException e) {
        }
    }
    
    /**
     * Tests that setConstants correctly overrides constants.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testSetConstants() throws ExpressionFormatException {
        // create with default constants
        NonBooleanReplacer replacer = new NonBooleanReplacer(CppReplacerTest.DEFAULT_VARS,
                CppReplacerTest.DEFAULT_CONSTANTS);
        
        // CONST_A has value 1
        // CONST_D is not defined
        assertThat(replacer.replaceNonCpp("CONST_A == CONST_D"), is("CONST_D_eq_1"));
        
        // now override the constants in the replacer
        Map<String, Long> constants = new HashMap<String, Long>();
        constants.put("CONST_D", 3L);
        replacer.setConstants(constants);
        
        // CONST_A is not defined
        // CONST_D has value 3
        assertThat(replacer.replaceNonCpp("CONST_A == CONST_D"), is("CONST_A_eq_3"));
    }
    
    /**
     * Tests that creating the replacer with a variability model works correctly.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testCreateWithVarModel() throws ExpressionFormatException {
        Set<VariabilityVariable> variables = new HashSet<>();
        variables.add(new FiniteIntegerVariable("MY_OWN_VAR", "bool", new int[] {0, 1}));
        variables.add(new InfiniteIntegerVariable("MY_OWN_INFINITE_VAR", "int"));
        variables.add(new VariabilityVariable("IGNORED", "bool"));
        VariabilityModel varModel = new VariabilityModel(new File(""), variables);
        
        NonBooleanReplacer replacer = new NonBooleanReplacer(varModel, new HashMap<>());

        // MY_OWN_VAR from the varModel can only have the values 0 or 1
        assertThat(replacer.replaceNonCpp("MY_OWN_VAR == 2"), is("0"));
        assertThat(replacer.replaceNonCpp("MY_OWN_INFINITE_VAR == 2"), is("MY_OWN_INFINITE_VAR"));
    }
    
}
