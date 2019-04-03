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
package net.ssehub.kernel_haven.non_boolean.replacer;

import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.and;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.not;
import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.or;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import net.ssehub.kernel_haven.util.logic.False;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.logic.Variable;
import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;

/**
 * Tests the {@link NonBooleanReplacer#nonCppToFormula(String)} method.
 * 
 * @author Adam
 */
public class ToFormulaTest {

    /**
     * Tests that a simple variable is translated correctly.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testSimpleVariable() throws ExpressionFormatException {
        NonBooleanReplacer replacer = new NonBooleanReplacer(CppReplacerTest.DEFAULT_VARS,
                CppReplacerTest.DEFAULT_CONSTANTS);
        
        Formula result = replacer.nonCppToFormula("VAR_A == 0");
        
        assertThat(result, is(new Variable("VAR_A_eq_0")));
    }
    
    /**
     * Tests that a simple conjunction is translated correctly.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testConjunction() throws ExpressionFormatException {
        NonBooleanReplacer replacer = new NonBooleanReplacer(CppReplacerTest.DEFAULT_VARS,
                CppReplacerTest.DEFAULT_CONSTANTS);
        
        Formula result = replacer.nonCppToFormula("VAR_A == 0 && VAR_B == 1");
        
        assertThat(result, is(and("VAR_A_eq_0", "VAR_B_eq_1")));
    }
    
    /**
     * Tests that a simple conjunction is translated correctly.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testDisjunction() throws ExpressionFormatException {
        NonBooleanReplacer replacer = new NonBooleanReplacer(CppReplacerTest.DEFAULT_VARS,
                CppReplacerTest.DEFAULT_CONSTANTS);
        
        Formula result = replacer.nonCppToFormula("VAR_A == 0 || VAR_B == 1");
        
        assertThat(result, is(or("VAR_A_eq_0", "VAR_B_eq_1")));
    }
    
    /**
     * Tests that a simple negation is translated correctly.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testNegation() throws ExpressionFormatException {
        NonBooleanReplacer replacer = new NonBooleanReplacer(CppReplacerTest.DEFAULT_VARS,
                CppReplacerTest.DEFAULT_CONSTANTS);
        
        Formula result = replacer.nonCppToFormula("!(VAR_A == 0)");
        
        assertThat(result, is(not("VAR_A_eq_0")));
    }
    
    /**
     * Tests that an expression resulting in a boolean true value is translated correctly.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testTrue() throws ExpressionFormatException {
        NonBooleanReplacer replacer = new NonBooleanReplacer(CppReplacerTest.DEFAULT_VARS,
                CppReplacerTest.DEFAULT_CONSTANTS);
        
        Formula result = replacer.nonCppToFormula("0 == 0");
        
        assertThat(result, is(True.INSTANCE));
    }
    
    /**
     * Tests that an expression resulting in a boolean false value is translated correctly.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testFalse() throws ExpressionFormatException {
        NonBooleanReplacer replacer = new NonBooleanReplacer(CppReplacerTest.DEFAULT_VARS,
                CppReplacerTest.DEFAULT_CONSTANTS);
        
        Formula result = replacer.nonCppToFormula("0 == 1");
        
        assertThat(result, is(False.INSTANCE));
    }
    
    /**
     * Tests that an integer literal resulting in a boolean true value is translated correctly.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testIntTrue() throws ExpressionFormatException {
        NonBooleanReplacer replacer = new NonBooleanReplacer(CppReplacerTest.DEFAULT_VARS,
                CppReplacerTest.DEFAULT_CONSTANTS);
        
        Formula result = replacer.nonCppToFormula("2");
        
        assertThat(result, is(True.INSTANCE));
    }
    
    /**
     * Tests that an integer literal resulting in a boolean false value is translated correctly.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testIntFalse() throws ExpressionFormatException {
        NonBooleanReplacer replacer = new NonBooleanReplacer(CppReplacerTest.DEFAULT_VARS,
                CppReplacerTest.DEFAULT_CONSTANTS);
        
        Formula result = replacer.nonCppToFormula("0");
        
        assertThat(result, is(False.INSTANCE));
    }
    
    /**
     * Tests that an unknown variable without a comparison is translated correctly.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testUnknownVariableWithoutComparison() throws ExpressionFormatException {
        NonBooleanReplacer replacer = new NonBooleanReplacer(CppReplacerTest.DEFAULT_VARS,
                CppReplacerTest.DEFAULT_CONSTANTS);
        
        Formula result = replacer.nonCppToFormula("UNKNOWN");
        
        assertThat(result, is(not("UNKNOWN_eq_0")));
    }
    
    /**
     * Tests that a variable without a comparison is translated correctly.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testVariableWithoutComparison() throws ExpressionFormatException {
        NonBooleanReplacer replacer = new NonBooleanReplacer(CppReplacerTest.DEFAULT_VARS,
                CppReplacerTest.DEFAULT_CONSTANTS);
        
        Formula result = replacer.nonCppToFormula("VAR_A");
        
        assertThat(result, is(not("VAR_A_eq_0")));
    }
    
}
