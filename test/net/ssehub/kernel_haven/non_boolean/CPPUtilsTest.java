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


import org.junit.Assert;
import org.junit.Test;


/**
 * Tests the {@link CPPUtils}.
 * @author El-Sharkawy
 *
 */
public class CPPUtilsTest {
    
    /**
     * Tests the {@link CPPUtils#isIfOrElifStatement(String)} method.
     */
    @Test
    public void testIsIfOrElifStatement() {
        String[] validIfs = {"#if (Var > Something)", "#if(Var > Something)", "#elif (Var > Something)",
            "#elif(Var > Something)", "#if (Var > Something) \\"};
        String[] inValidIfs = {"#ifdef Var", "#ifndef Var", "#ifdef(Var)", "#ifndef(Var)", ""};
        
        // Check desired statements
        for (String validIf : validIfs) {
            Assert.assertTrue(validIf + " was not detected as valid #if.", CPPUtils.isIfOrElifStatement(validIf));
        }
        
        // Check undesired statements
        for (String invalidIf : inValidIfs) {
            Assert.assertFalse(invalidIf + " was detected as valid #if, but should not!",
                CPPUtils.isIfOrElifStatement(invalidIf));
        }
    }

}
