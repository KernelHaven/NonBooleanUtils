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
package net.ssehub.kernel_haven.non_boolean.heuristic;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.non_boolean.NonBooleanSettings;
import net.ssehub.kernel_haven.non_boolean.NonBooleanVariable;
import net.ssehub.kernel_haven.test_utils.TestConfiguration;

/**
 * Tests the {@link NonBooleanHeuristic}.
 * 
 * @author Adam
 */
public class NonBooleanHeuristicTest {
    
    private NonBooleanHeuristic heuristic;
    
    /**
     * Creates the heuristic.
     * 
     * @throws SetUpException unwanted.
     */
    @Before
    public void createHeuristic() throws SetUpException {
        TestConfiguration config = new TestConfiguration(new Properties());
        config.registerSetting(NonBooleanSettings.VARIABLE_REGEX);
        config.setValue(NonBooleanSettings.VARIABLE_REGEX, Pattern.compile("\\p{Alpha}+\\w*"));
        
        this.heuristic = new NonBooleanHeuristic(config);
    }
    
    /**
     * Tests that a single variable with a single equals comparison is detected correctly.
     */
    @Test
    public void testEquals() {
        heuristic.addSingleCppLine("#if (VAR_A == 3)");
        
        Map<String, NonBooleanVariable> result = heuristic.getResult();
        
        assertThat(result.get("VAR_A"), notNullValue());
        assertThat(result.get("VAR_A").getConstants(), is(new long[] {3}));
        
        assertThat(result.size(), is(1));
    }
    
    /**
     * Tests that a single variable with a single unequal comparison is detected correctly.
     */
    @Test
    public void testUnequal() {
        heuristic.addSingleCppLine("#if (VAR_A != 3)");
        
        Map<String, NonBooleanVariable> result = heuristic.getResult();
        
        assertThat(result.get("VAR_A"), notNullValue());
        assertThat(result.get("VAR_A").getConstants(), is(new long[] {3}));
        
        assertThat(result.size(), is(1));
    }
    
    /**
     * Tests that a single variable with a single less than comparison is detected correctly.
     */
    @Test
    public void testLessThan() {
        heuristic.addSingleCppLine("#if (VAR_A < 3)");
        
        Map<String, NonBooleanVariable> result = heuristic.getResult();
        
        assertThat(result.get("VAR_A"), notNullValue());
        assertThat(result.get("VAR_A").getConstants(), is(new long[] {2}));
        
        assertThat(result.size(), is(1));
    }
    
    /**
     * Tests that a single variable with a single greater than comparison is detected correctly.
     */
    @Test
    public void testGreaterThan() {
        heuristic.addSingleCppLine("#if (VAR_A > 3)");
        
        Map<String, NonBooleanVariable> result = heuristic.getResult();
        
        assertThat(result.get("VAR_A"), notNullValue());
        assertThat(result.get("VAR_A").getConstants(), is(new long[] {4}));
        
        assertThat(result.size(), is(1));
    }
    
    /**
     * Tests that a single variable with a single less than comparison is detected correctly.
     */
    @Test
    public void testLessOrEqual() {
        heuristic.addSingleCppLine("#if (VAR_A <= 3)");
        
        Map<String, NonBooleanVariable> result = heuristic.getResult();
        
        assertThat(result.get("VAR_A"), notNullValue());
        assertThat(result.get("VAR_A").getConstants(), is(new long[] {3}));
        
        assertThat(result.size(), is(1));
    }
    
    /**
     * Tests that a single variable with a single greater than comparison is detected correctly.
     */
    @Test
    public void testGreaterOrEqual() {
        heuristic.addSingleCppLine("#if (VAR_A >= 3)");
        
        Map<String, NonBooleanVariable> result = heuristic.getResult();
        
        assertThat(result.get("VAR_A"), notNullValue());
        assertThat(result.get("VAR_A").getConstants(), is(new long[] {3}));
        
        assertThat(result.size(), is(1));
    }
    
    /**
     * Tests that a constant with a suffix is detected correctly.
     */
    @Test
    public void testConstantWithSuffixLowerCase() {
        heuristic.addSingleCppLine("#if (VAR_A >= 3ul)");
        
        Map<String, NonBooleanVariable> result = heuristic.getResult();
        
        assertThat(result.get("VAR_A"), notNullValue());
        assertThat(result.get("VAR_A").getConstants(), is(new long[] {3}));
        
        assertThat(result.size(), is(1));
    }
    
    /**
     * Tests that a constant with a suffix is detected correctly.
     */
    @Test
    public void testConstantWithSuffixUpperCase() {
        heuristic.addSingleCppLine("#if (VAR_A >= 3UL)");
        
        Map<String, NonBooleanVariable> result = heuristic.getResult();
        
        assertThat(result.get("VAR_A"), notNullValue());
        assertThat(result.get("VAR_A").getConstants(), is(new long[] {3}));
        
        assertThat(result.size(), is(1));
    }
    
    /**
     * Tests that a negative constant is detected correctly.
     */
    @Test
    public void testNegativeConstant() {
        heuristic.addSingleCppLine("#if (VAR_A == -3)");
        
        Map<String, NonBooleanVariable> result = heuristic.getResult();
        
        assertThat(result.get("VAR_A"), notNullValue());
        assertThat(result.get("VAR_A").getConstants(), is(new long[] {-3}));
        
        assertThat(result.size(), is(1));
    }
    
    /**
     * Tests that comparisons with two variables are handled correctly.
     */
    @Test
    public void testVariableEqualsVariable() {
        heuristic.addSingleCppLine("#if (VAR_A == VAR_B)");
        
        Map<String, NonBooleanVariable> result = heuristic.getResult();
        
        assertThat(result.size(), is(0));
    }
    
    /**
     * Tests that a multiple comparisons in multiple lines are detected correctly.
     */
    @Test
    public void testMultipleValuesInMultipleLines() {
        heuristic.addSingleCppLine("#if (VAR_A == 3)");
        heuristic.addSingleCppLine("#if (VAR_A == 5)");
        
        Map<String, NonBooleanVariable> result = heuristic.getResult();
        
        assertThat(result.get("VAR_A"), notNullValue());
        assertThat(result.get("VAR_A").getConstants(), is(new long[] {3, 5}));
        
        assertThat(result.size(), is(1));
    }
    
    /**
     * Tests that a multiple comparisons in the same line are detected correctly.
     */
    @Test
    public void testMultipleValuesInSameLine() {
        heuristic.addSingleCppLine("#if (VAR_A == 3) || (VAR_A == 5)");
        
        Map<String, NonBooleanVariable> result = heuristic.getResult();
        
        assertThat(result.get("VAR_A"), notNullValue());
        assertThat(result.get("VAR_A").getConstants(), is(new long[] {3, 5}));
        
        assertThat(result.size(), is(1));
    }

}
